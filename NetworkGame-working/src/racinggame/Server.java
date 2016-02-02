package racinggame;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.imageio.ImageIO;

public class Server {
    public static void main(String[] args) throws IOException {
        new ServerThread().start();
    }
}

class ServerThread extends Thread {
    
    protected DatagramSocket socket = null;
    protected List<InetAddress> addresses = new ArrayList();
    protected List<Integer> ports = new ArrayList();
    private int idcounter;
    private List<Car> carList = new ArrayList();
    
    public ServerThread() throws IOException {
        // Create UDP socket to listen to port 7777
        socket = new DatagramSocket(7777);
    }
    
    @Override
    public void run() {
        
        System.out.println("Server started.");
        byte[] receivebuffer = new byte[256];
        DatagramPacket packet = new DatagramPacket(receivebuffer, receivebuffer.length);
        // Initialize id counter
        idcounter = 0;
        
        while(true) {
            try {
                socket.receive(packet); // Wait for packets
                //System.out.println("Packet from: " + packet.getAddress());
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
        InetAddress address = packet.getAddress();
        int port = packet.getPort();
        byte[] sendbuffer = new byte[256];
        String data = new String(packet.getData(), 0, packet.getLength());
        String[] dataArray = data.split(":");
        String response;
        
        if (dataArray[0].equals("00")) { // Login packet
            if (idcounter == 4) { // max 4 players
                response = "00:-1";
                sendbuffer = response.getBytes();
                packet = new DatagramPacket(sendbuffer, sendbuffer.length, address, port);
                socket.send(packet);
                return;
            } else {
                response = "00:" + idcounter;
            }
            addresses.add(address);
            ports.add(port);

            // Create new car for the car list
            BufferedImage track = null;
            try {
                track = ImageIO.read(getClass().getResourceAsStream("/resources/track3.png"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            Car c = new Car(track, idcounter);
            carList.add(c);
            idcounter++;
            
            // Send player's id as response
            sendbuffer = response.getBytes();
            packet = new DatagramPacket(sendbuffer, sendbuffer.length, address, port);
            socket.send(packet);
            
            // If we have 4 players --> send start message to players
            if (idcounter == 4) {
                response = "03:start";
                sendbuffer = response.getBytes();
                int i = 0;
                for (InetAddress a : addresses) {
                    if (a.equals(address) && (ports.get(i) == port)) { // don't send to last joined player
                        i++;
                    }
                    else {
                        //System.out.println("Sending start");
                        packet = new DatagramPacket(sendbuffer, sendbuffer.length, a, ports.get(i));
                        socket.send(packet);
                        i++;
                    }
                }
            }
        }
        else if (dataArray[0].equals("01")) { // Coordinate/angle packet
            // Update server's game state (this section not needed until client predicition)
            int id = Integer.parseInt(dataArray[1]);
            Car c = carList.get(id);
            c.setX(Double.parseDouble(dataArray[2]));
            c.setY(Double.parseDouble(dataArray[3]));
            c.setAngle(Double.parseDouble(dataArray[4]));
            
            // Send packet to all clients
            response = data;
            sendbuffer = response.getBytes();
            int i = 0;
            for (InetAddress a : addresses) {
                packet = new DatagramPacket(sendbuffer, sendbuffer.length, a, ports.get(i));
                socket.send(packet);
                i++;
            }
        }
        else if (dataArray[0].equals("02")) { // Chat message packet
            response = data;
            sendbuffer = response.getBytes();
            int i = 0;
            for (InetAddress a : addresses) {
                packet = new DatagramPacket(sendbuffer, sendbuffer.length, a, ports.get(i));
                socket.send(packet);
                i++;
            }
        }
    }
}