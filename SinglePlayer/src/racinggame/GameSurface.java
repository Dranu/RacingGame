package racinggame;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class GameSurface extends JPanel implements Runnable {
    
    private Car car;
    private DatagramSocket socket;
    private InetAddress address;
    public static BufferedImage track;
    public static List<Car> carList = new ArrayList();
    
    GameSurface() {
        int id = -1;
        try {
            id = initNetworking(); // we get player id from server
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (id != -1) {
            initObjects(id);
        }
        initWindow();
    }
    
    @Override
    public void run() {
        
        // Start receiving input from server in another thread
        new InputThread(socket).start();
        
        
        /* THE GAMELOOP */
        // Note! This section needs to be modified for client prediction
        /*
            Client prediction:
            1) Get user input (steering, throttle)
            2) Create and send input-packet to server
            3) Do the prediction in client side (calculate coordinates/angle)
            4) Render predicted car position
            5) Receive real position from server
            6) Compare to current position
            7) Fix position if needed
        */
        
        while (true) {
            
            // 1) Get coordinates and angle
            int my_id = car.getID();
            int x = car.getX();
            int y = car.getY();
            double a = car.getAngle();
            
            // 2) Create and send coordinate/angle packet to server
            byte[] sendbuffer = new byte[256];
            String data = "01:" + Integer.toString(my_id) + ":" + Integer.toString(x) 
                    + ":" + Integer.toString(y) + ":" + Double.toString(a);
            sendbuffer = data.getBytes();
            DatagramPacket packet = new DatagramPacket(sendbuffer, sendbuffer.length, address, 7777);
            try {
                socket.send(packet);
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            // 3) Move our car
            car.move();
            
            // 4) Render the scene
            repaint();
            
            try {
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            
        }
    }
    
    private int initNetworking() throws IOException {
        // For localhost testing we have to bind the socket to random port
        // If clients were on separate computers, we could use same port for each
        Random rand = new Random();
        int port = rand.nextInt(7790-7778) + 7778;
        System.out.println("Creating socket to listen to port " + port);
        socket = new DatagramSocket(port);
        byte[] sendbuffer = new byte[256];
        address = InetAddress.getByName("localhost"); // Set host IP here
        
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
                id = Integer.parseInt(dataArray[1]);
                break;
            }
        }
        
        return id;
    }
    
    private void initWindow() {
        addKeyListener(new TAdapter());
        setFocusable(true);
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
        
        // Render cars in carList
        // carList is also accessed by InputThread so we need to synchronize
        synchronized (carList) {
            for (Car c : carList) {
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
            }
        }
        Toolkit.getDefaultToolkit().sync();
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
    
}

class InputThread extends Thread {
    
    private DatagramSocket socket;
    private String data;
    
    public InputThread(DatagramSocket s) {
        this.socket = s;
    }
    
    @Override
    public void run() {
        byte[] receivebuffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(receivebuffer, receivebuffer.length);
        
        while (true) {
            try {
                socket.receive(packet);
                data = new String(packet.getData(), 0, packet.getLength());
                //System.out.println(data);
                String[] dataArray = data.split(":");
                if (dataArray[0].equals("01")) { // coordinate/angle packet
                    synchronized(GameSurface.carList) {
                        int playerid = Integer.parseInt(dataArray[1]);
                        Car c = null;
                        try {
                            c = GameSurface.carList.get(playerid);
                        } catch (IndexOutOfBoundsException e) {
                            // car not found on list --> create new
                            c = new Car(GameSurface.track, playerid);
                            // Add new car to correct location on the list
                            boolean added = false;
                            for (Car car : GameSurface.carList) {
                                if (playerid < car.getID()) {
                                    GameSurface.carList.add(car.getID(), c);
                                    added = true;
                                    break;
                                }
                            }
                            if (!added) GameSurface.carList.add(c);
                        }
                        // set new variable values for that player
                        c.setX(Double.parseDouble(dataArray[2]));
                        c.setY(Double.parseDouble(dataArray[3]));
                        c.setAngle(Double.parseDouble(dataArray[4]));
                    }
                }
                // reset packet length
                packet.setLength(receivebuffer.length);
            } catch (IOException e) {
                e.printStackTrace();
                socket.close();
                break;
            }
        }
    }
}
