package racinggameclient;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GameSurface extends JPanel implements Runnable {
    
    private Car car;
    private DatagramSocket socket;
    private InetAddress address;
    public static BufferedImage track;
    public static Map<Integer,Car> carList = new HashMap();
    public static Object ready = new Object(); // flag for thread communication
    private long millis;
    private Prediction predict;
    private double newx = 0;
    private double newy = 0;
    private double newa = 0;
    
    private static int gameover = -1;
    private JLabel winnerlabel;
    
    GameSurface(String host) {
        int id = -1;
        try {
            id = initNetworking(host); // we get player id from server
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (id != -1) {
            initObjects(id);
        }
        predict = new Prediction(); // We initialize a prediction object for this gamesurface / car
        initWindow();
    }
    
    public static synchronized int getGameover() {
        return gameover;
    }
    
    public static synchronized void setGameover(int winnerid) {
        gameover = winnerid;
    }
    
    public int getCarID() {
        return car.getID();
    }
    
    @Override
    public void run() {
        
        // Start receiving input from server in another thread
        new InputThread(socket).start();
        new ChatThread(socket, car.getID(), address).start();
        
        // wait if we are not the last person joined i.e. id = 3
        if (car.getID() < 3) {
            synchronized (ready) {
                try {
                    ready.wait();
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }
        
        /* THE GAMELOOP */
        // Note! This section needs to be modified for client prediction
        /*
            Client prediction:
            1) Get user input (steering, throttle)
            2) Do the prediction in client side (calculate coordinates/angle)
            3) Create and send input and coordinate -packet to server
            4) Render predicted car position
                4-1) Add the inputs and calculated position to the prediction list
            5) Receive real position from server
            6) Compare to calculated position
            7) Fix position if needed
                7-1) Set correct coordinates from the server to that point in the past
                7-2) Iterate through inputs and recalculate current position
        */
        
        while (true) {
            
            int winnerid = getGameover();
            if (winnerid != -1) {
                // Show winner message
                if (winnerid == car.getID()) {
                    winnerlabel.setText("You win!");
                }
                else {
                    winnerid++;
                    winnerlabel.setText("Player " + winnerid + " wins!");
                }
                break;
            }
            
            // 1) Get inputs
            int my_id = car.getID();
            int steer = car.getSteering();
            int throttle = car.getThrottle();
            
            //2) Do the prediction in client side (calculate coordinates/angle)
            car.move(-5,-5);
            int x = car.getX();
            int y = car.getY();
            double a = car.getAngle();
            
            millis = System.currentTimeMillis(); //Get a timestamp
            car.setTimestamp(millis);
            
            //4-1) Add the inputs and calculated position to the prediction list
            predict.addHashInput(millis, (Double.toString(a)
                    + ":" + Integer.toString(x)
                    + ":" + Integer.toString(y) 
                    + ":" + Integer.toString(steer) 
                    + ":" + Integer.toString(throttle)));
            
            // 3) Create and send input and coordinate -packet to server
            byte[] sendbuffer = new byte[256];
            String data = "01:" + Integer.toString(my_id) + ":" + Integer.toString(steer) 
                    + ":" + Integer.toString(throttle) + ":" + Double.toString(a)+ ":" 
                    + Integer.toString(x)+ ":" + Integer.toString(y) + ":" +Long.toString(millis);
            sendbuffer = data.getBytes();
            DatagramPacket packet = new DatagramPacket(sendbuffer, sendbuffer.length, address, 7777);
            Random rand = new Random();
            try {
                socket.send(packet);
            } catch (IOException e) {
                //e.printStackTrace();
                System.out.println("Lost connection to server.");
                System.exit(0);
            }

            // 4) Render the scene
            repaint();
            
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }   
        }
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        render(g);
    }
    
    private void render(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        AffineTransform old = g2d.getTransform();
        g2d.drawImage(track, 0, 0, null);
        // Render our car
        int x = car.getX();
        int y = car.getY(); 
        int w = car.getImage().getWidth();
        int h = car.getImage().getHeight();
        double a = car.getAngle() + Math.PI/2;
        g2d.rotate(a, x+w/2, y+h/2);
        g2d.drawImage(car.getImage(), x, y, null);
        g2d.setTransform(old); // reset old rotation
        g2d.drawString("LAP " + car.getLap(),50,20); //Display the current lap
        
        // Render cars in carList
        // carList is also accessed by InputThread so we need to synchronize
        synchronized (carList) {
            for (Car c : carList.values()) {
                if (c.getID() != car.getID()) { // DO NOT RE-RENDER OUR CAR!
                    x = c.getX();
                    y = c.getY();
                    w = c.getImage().getWidth();
                    h = c.getImage().getHeight();
                    a = c.getAngle() + Math.PI/2;
                    g2d.rotate(a, x+w/2, y+h/2);
                    g2d.drawImage(c.getImage(), x, y, null);
                    g2d.setTransform(old); // reset old rotation
                }
                else 
                { 
                    //Iterate through hashmap to find the calculated result from client-side
                    if(predict.iterate(c.getTimestamp()) == true){ //found the result
                        String[] dataArray = predict.getInput(c.getTimestamp()).split(":"); //split the data
                        
                        //6) Compare to calculated position
                        if((dataArray[0]+":"+dataArray[1]+":"+dataArray[2]).equals(c.getAngle()+":"+c.getX()+":"+c.getY())){
                            predict.removeTime(c.getTimestamp());   //Because calculation is correct, we can remove previous inputs from the prediction list up to this point 
                        } 
                        else //7) Fix position if needed
                        {
                            //7-1) Set correct coordinates from the server to that point in the past
                            //7-2) Iterate through inputs and recalculate current position
                            long startTime = c.getTimestamp(); //Save the timestamp we just got
                            newx = c.getX();
                            newy = c.getY();
                            newa = c.getAngle();
                            predict.addHashInput(startTime,(Double.toString(newa)
                                    + ":" + Integer.toString((int)newx)
                                    + ":" + Integer.toString((int)newy) 
                                    + ":" + dataArray[3] 
                                    + ":" + dataArray[4]));
                            do{
                                
                                startTime = predict.getNextTime(startTime); //Get the next time to recalculate
                                dataArray = predict.getInput(startTime).split(":"); 
                                //Recalculate the coords with the steering and throttle
                                int steer = (Integer.parseInt(dataArray[3]));
                                int throt = (Integer.parseInt(dataArray[4]));
                                
                                recalcMove(steer,throt);
                                                       
                                //Add the new inputs to hash, replacing the old values
                                predict.addHashInput(startTime,(Double.toString(newa)
                                    + ":" + Integer.toString((int)newx)
                                    + ":" + Integer.toString((int)newy) 
                                    + ":" + dataArray[3] 
                                    + ":" + dataArray[4]));        
                            }while(startTime < car.getTimestamp());
  
                            car.setX(newx);
                            car.setY(newy);
                            car.setAngle(newa);
                          
                        } 
                    }             
                }
            }
        }
        Toolkit.getDefaultToolkit().sync();
    }
    
    
    public void recalcMove(int steer, int throt){
        double vx = 0;
        double vy = 0;
        double drag = 0.9;
        double angle = -Math.PI/2;
        double angVel = 0;
        double angDrag = 0.9;
        double power = 0.2;
        double turnSpeed = 0.0087;

        if (throt != 0) {
            if (throt == 1) { // forward
                vx += Math.cos(angle)*power;
                vy += Math.sin(angle)*power;
            }
            if (throt == -1) { // reverse
                vx -= Math.cos(angle)*power*0.5;
                vy -= Math.sin(angle)*power*0.5;
            }
            if (steer == -1) { // left
                angVel -= turnSpeed;
            }
            if (steer == 1) { // right
                angVel += turnSpeed;
            }
        }

        // Update position
        newx += vx;
        newy += vy;
        newa += angVel;

        switch (car.terraindetect(((int)newx), ((int)newy))) {
            case -1:
                // on track image border
                newx -= vx;
                newy -= vy;
                newa -= angVel;
                vx = 0;
                vy = 0;
                angVel = 0;
                break;
            case 0:
                // on track
                vx *= drag;
                vy *= drag;
                angVel *= angDrag;
                break;
            case 1:
                // on redline
                vx *= drag*0.9;
                vy *= drag*0.9;
                angVel *= angDrag*0.9;
                break;
            case 2:
                // on grass
                vx *= drag*0.6;
                vy *= drag*0.6;
                angVel *= angDrag*0.6;
                break;
        }
    
    }
    
    
    private class TAdapter extends KeyAdapter {
        
        @Override
        public void keyReleased(KeyEvent e) {
            car.keyReleased(e);
        }
        
        @Override
        public void keyPressed(KeyEvent e) {
            car.keyPressed(e);
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="Initialize-methods">
    private int initNetworking(String host) throws IOException {
        // For localhost testing we have to bind the socket to random port
        // If clients were on separate computers, we could use same port for each
        Random rand = new Random();
        int port = rand.nextInt(7790-7778) + 7778;
        System.out.println("Creating socket to listen to port " + port);
        socket = new DatagramSocket(port);
        byte[] sendbuffer = new byte[256];
        address = InetAddress.getByName(host); // Set host IP here
        
        // Bind to specific host to detect socket state during send/receive
        socket.connect(address, 7777);
        
        // Create login packet
        /*Scanner sc = new Scanner(System.in);
        System.out.print("What is your name?: ");
        String name = sc.nextLine();*/
        String data = "00:" + "Name";
        sendbuffer = data.getBytes();
        
        // Send login packet to server
        DatagramPacket packet = new DatagramPacket(sendbuffer, sendbuffer.length, address, 7777);
        socket.send(packet);
        
        // Wait for server response
        int id = -1;
        while (true) {
            socket.receive(packet);
            data = new String(packet.getData(), 0, packet.getLength());
            System.out.println(data);
            String[] dataArray = data.split(":");
            // react only to login response, ignore all other packets for now
            if (dataArray[0].equals("00")) {
                if(dataArray[1].equals("-1")){
                    System.out.println("Server refused connection");
                    System.exit(0);
                }
                id = Integer.parseInt(dataArray[1]);
                break;
            }
        }
        
        return id;
    }
    
    private void initWindow() {
        addKeyListener(new TAdapter());
        setFocusable(true);
        setLayout(new GridBagLayout());
        winnerlabel = new JLabel("");
        winnerlabel.setHorizontalAlignment(JLabel.LEFT);
        winnerlabel.setVerticalAlignment(JLabel.CENTER);
        winnerlabel.setVerticalTextPosition(JLabel.CENTER);
        winnerlabel.setFont(new Font(Font.DIALOG, Font.PLAIN, 40));
        winnerlabel.setForeground(Color.white);
        add(winnerlabel);
    }
    
    private void initObjects(int id) {
        // create track
        try {
            track = ImageIO.read(getClass().getResourceAsStream("/resources/track3.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        car = new Car(track, id); // create our car
    }
    //</editor-fold>   
}



// <editor-fold defaultstate="collapsed" desc="Input thread"> 
class InputThread extends Thread {
    
    private DatagramSocket socket;
    private String data;
    
    public InputThread(DatagramSocket s) {
        this.socket = s;
    }
    
    @Override
    @SuppressWarnings("empty-statement")
    public void run() {
        byte[] receivebuffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(receivebuffer, receivebuffer.length);
        
        while (true) {
            try {
                socket.receive(packet);
                data = new String(packet.getData(), 0, packet.getLength());
                //System.out.println(data);
                String[] dataArray = data.split(":");
                //5) Receive real position from server
                if (dataArray[0].equals("01")) { // coordinate/angle packet
                    synchronized(GameSurface.carList) {
                        int playerid = Integer.parseInt(dataArray[1]);
                        Car c = GameSurface.carList.get(playerid);
                        if (c == null) {
                            // not found --> create new
                            c = new Car(GameSurface.track, playerid);
                            GameSurface.carList.put(playerid, c);
                        }
                        // set new variable values for that player
                        c.setX(Integer.parseInt(dataArray[2]));
                        c.setY(Integer.parseInt(dataArray[3]));
                        c.setAngle(Double.parseDouble(dataArray[4]));
                        c.setTimestamp(Long.parseLong(dataArray[5]));
                    }
                }
                else if (dataArray[0].equals("02")) { // chat message packet
                    dataArray = data.split(":", 3);
                    int senderID = Integer.parseInt(dataArray[1])+1;
                    //Print even if there are ":" in the middle
                    System.out.println("Player " + senderID + ": " + dataArray[2]);
                }
                else if (dataArray[0].equals("03")) { // start game packet
                    synchronized (GameSurface.ready) {
                        GameSurface.ready.notifyAll();
                    }
                }
                else if (dataArray[0].equals("04")){
                    GameSurface.setGameover(Integer.parseInt(dataArray[1]));
                    //GameSurface.gameover = true;
                }
                // reset packet length
                packet.setLength(receivebuffer.length);
            } catch (IOException e) {
                //e.printStackTrace();
                socket.close();
                break;
            }
        }
    }
}
//</editor-fold>

// <editor-fold defaultstate="collapsed" desc="Chat thread">
class ChatThread extends Thread {
    
    private DatagramSocket socket;
    private String data;
    private Scanner sc;
    private int playerid;
    byte[] sendbuffer = new byte[256];
    private InetAddress address;
    
    public ChatThread(DatagramSocket s, int id, InetAddress a) {
        this.socket = s;
        this.sc = new Scanner(System.in);
        this.playerid = id;
        this.address = a;
    }
    
    @Override
    public void run() {
        while (true) {
            String msg = sc.nextLine();
            if(msg.length()>0){
                String data = "02:" + playerid + ":" + msg;
                sendbuffer = data.getBytes();
                DatagramPacket packet = new DatagramPacket(sendbuffer, sendbuffer.length, address, 7777);
                try {
                    socket.send(packet);
                } catch (IOException ex) {
                    //ex.printStackTrace();
                    System.out.println("Chat has been terminated.");
                    System.exit(0);
                }
            }
        }
    }
}
//</editor-fold>
