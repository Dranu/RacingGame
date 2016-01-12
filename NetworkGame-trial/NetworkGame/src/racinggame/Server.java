/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package racinggame;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.ImageIO;
/**
 *
 * @author Jiri
 */
public class Server {
    public static void main(String[] args) throws IOException {
        new ServerThread().start();
    }
}




class ServerThread extends Thread {
   private BufferedImage track;
   protected DatagramSocket socket = null;
   protected List<InetAddress> addresses = new ArrayList();
   protected List<Integer> ports = new ArrayList();
   //public ArrayList<Car> carlist = new ArrayList<Car>();
   private Car car;
  // public static ArrayList<String[]> Carlist = new ArrayList<String[]>();
   private int i = 0;
    //list needs to have: Port, IP address, Username, PlayerID, Coordinates
    
    public ServerThread() throws IOException {
        // Create UDP socket to listen to port 7777
        socket = new DatagramSocket(7777);
    }
    
    /*
    public static ArrayList<String[]> getList(){
        return Carlist;
    }*/
    
    
    
    
    
    @Override
    public void run() {
        try {
            //removed AsStream because it produced an error - Jiri
            track = ImageIO.read(getClass().getResource("/resources/track3.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server started.");
        byte[] receivebuffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(receivebuffer, receivebuffer.length);
        //Car car = new Car(null, null, 0);
        car = new Car(Car.gamesurface, track, 0);
        RacingGame.start(car);
        //car.setX(3);
        while(true) {
            try {
                socket.receive(packet); // Wait for packets
                System.out.println("Packet from: " + packet.getAddress());
                //Car.setX(3);
                handlePacket(packet); // Packet caught --> figure out response
                packet.setLength(receivebuffer.length); // Reset length of packet
            } catch (IOException e) {
                e.printStackTrace();
                socket.close();
                break;
            }
        }
    }
    
    private void handlePacket(DatagramPacket packet) throws IOException {
        
        boolean clientExists = false;
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        byte[] sendbuffer = new byte[256];
        String data = new String(packet.getData(), 0, packet.getLength());
        //System.out.println("Contains: " + data);
        String[] dataArray = data.split(":");
        
        
        /*
        
        
        for(i=0;i<Carlist.size();i++){
            String[] CarlistArray = Carlist.get(i);
            //for (j=0;j<CarlistArray.length;j++){
                    if(CarlistArray[1].equals(dataArray[1])){
                        //client exists
                        clientExists = true;
                        Carlist.set(i, dataArray);
                    }
                    else{
                        //client does not exist
                        clientExists = false;
                    }
                //}
        } */
                        
       // if(clientExists == false){Carlist.add(dataArray);}
        
        
        //carlist.add(new Car(Car.gamesurface, track, 1));
        if (dataArray[0].equals("00")) {
            addresses.add(address);
            ports.add(port);
            //carlist.add(new Car(Car.gamesurface, Car.track, i));
            //RacingGame.start(cars[0]);
            //String response = data;//"Hello " + dataArray[1] + "!";
            //sendbuffer = response.getBytes();
            //packet = new DatagramPacket(sendbuffer, sendbuffer.length, address, port);
            //socket.send(packet);
            GameSurface.Carlist.add(dataArray);
        }
            //i++;
        //}
        else if (dataArray[0].equals("01")) {
            for(i=0;i<GameSurface.Carlist.size();i++){
            String[] CarlistArray = GameSurface.Carlist.get(i);
            //for (j=0;j<CarlistArray.length;j++){
                    if(CarlistArray[1].equals(dataArray[1])){
                        //client exists
                    //    clientExists = true;
                        GameSurface.Carlist.set(i, dataArray);
                        break;
                    }
                    //else{
                        //client does not exist
                      //  clientExists = false;
                    //}
                //}
        } 
            String response = data;//dataArray[1] + ": " + dataArray[2]+ ": " + dataArray[3];
            sendbuffer = response.getBytes();
            int i = 0;
            for (InetAddress a : addresses) {
                packet = new DatagramPacket(sendbuffer, sendbuffer.length, a, ports.get(i));
                socket.send(packet);
                i++;
            }
        }
        //Cars[] cars = carlist.toArray();
        //RacingGame.start(cars[0]);
        //Car[] cars = carlist.toArray();
    } //end handlepacket
 
    
    
    
} //end class ServerThread