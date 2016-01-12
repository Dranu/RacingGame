/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package racinggame;

import java.awt.EventQueue;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.ImageIO;
/**
 *
 * @author Jiri
 */
public class Client {
    //private BufferedImage track;
    static private String name;
    //private Car car;
    static public BufferedImage track;
    //public ArrayList<String[]> carlist = new ArrayList<String[]>();
 
    
    public static void main(String[] args) throws IOException {

        // For localhost testing we have to bind the socket to random port
        // If clients were on separate computers, we could use same port for each
        Random rand = new Random();
        int port = rand.nextInt(7790-7778) + 7778;
        
        System.out.println("Creating socket to listen to port " + port);
        DatagramSocket socket = new DatagramSocket(port);
        byte[] sendbuffer = new byte[256];
        InetAddress address = InetAddress.getByName("localhost"); // Set host IP here
        
        // Create login message
        Scanner sc = new Scanner(System.in);
        System.out.print("What is your name?: ");
        name = sc.nextLine();
        String data = "00:" + name;
        sendbuffer = data.getBytes();
        // Send message to server
        DatagramPacket packet = new DatagramPacket(sendbuffer, sendbuffer.length, address, 7777);
        socket.send(packet);
        
        // Start receiving input
        new InputThread(socket).start();
        //RacingGame.start(); //this works
         try {
            //removed AsStream because it produced an error - Jiri
            track = ImageIO.read(Client.class.getResource("/resources/track3.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        Car car = new Car(Car.gamesurface, track, 1);
        

        RacingGame.start(car); //this works
        
        Integer carX;
        Integer carY;
        double carAngle;
        BufferedImage carImg;
        carX = car.getX();
        carY = car.getY();
        carAngle = car.getAngle();
        carImg = car.getImage();
        data = "01:" + name + ":" + carX+ ":" + carY + ":" + carAngle+":"+carImg;
        String[] dataArray = data.split(":");
        
        //GameSurface.Carlist.add(dataArray);
        
        // Start output loop
        while (true) {
            carX = car.getX();
            carY = car.getY();
            carAngle = car.getAngle();
            carImg = car.getImage();
            //We send random number 1-10 to server on every second
            //Integer x = rand.nextInt(10-1) + 1;
            //String messageX = carX.toString();
            //String messageY = carY.toString();
            data = "01:" + name + ":" + carX+ ":" + carY + ":" + carAngle+":"+carImg;
            //System.out.println(data);
            sendbuffer = data.getBytes();
            packet = new DatagramPacket(sendbuffer, sendbuffer.length, address, 7777);
            socket.send(packet);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}


class InputThread extends Thread {
  //  public ArrayList<String[]> carlist = new ArrayList<String[]>();
    private DatagramSocket socket;
    private String message;
    
    public InputThread(DatagramSocket s) {
        this.socket = s;
    }
    
    @Override
    public void run() {
        int i = 0;
        boolean clientExists = false;
        byte[] receivebuffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(receivebuffer, receivebuffer.length);
        
        while (true) {
            try {
                socket.receive(packet);
                message = new String(packet.getData(), 0, packet.getLength());
                String[] dataArray = message.split(":");
                //System.out.println(message);
                //System.out.println("I: " + i);
                //System.out.println("Client size Size: "+ GameSurface.Carlist.size());
                for(i=0;i<GameSurface.Carlist.size();i++){
                    String[] CarlistArray = GameSurface.Carlist.get(i);
            //for (j=0;j<CarlistArray.length;j++){
            //System.out.println("I: " + i);
            //System.out.println(dataArray[1]);
            //System.out.println(GameSurface.Carlist);
            //System.out.println("Tulostatko mitään?: " +CarlistArray[1]);// arrayssa vikaa
            //System.out.println("PERKELE!");
                    if(CarlistArray[1].equals(dataArray[1])){
                        //client exists
                        clientExists = true;
                        GameSurface.Carlist.set(i, dataArray);
                        break;
                    }
                    else{
                        //client does not exist
                        //System.out.println("Client tuntematon!");
                        clientExists = false;
                    }
                //}
        } 
                        
            if(clientExists == false){GameSurface.Carlist.add(dataArray);}
                packet.setLength(receivebuffer.length);
            } catch (IOException e) {
                e.printStackTrace();
                socket.close();
                break;
            }
        }
    }
}
