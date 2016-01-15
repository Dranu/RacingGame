/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package racinggame;
import java.io.*;
import java.net.*;


class SendThread extends Thread {
 
    private InetAddress serverIPAddress;
    private DatagramSocket udpClientSocket;
    private boolean stopped = false;
    private int serverport;
 
    public SendThread(InetAddress address, int serverport) throws SocketException {
        this.serverIPAddress = address;
        this.serverport = serverport;
        // Create client DatagramSocket
        this.udpClientSocket = new DatagramSocket();
        this.udpClientSocket.connect(serverIPAddress, serverport);
    }
    public void halt() {
        this.stopped = true;
    }
    public DatagramSocket getSocket() {
        return this.udpClientSocket;
    }
 
    public void run() {       
        try {    
        	//send blank message
        	byte[] data = new byte[1024];
        	data = "".getBytes();
        	DatagramPacket blankPacket = new DatagramPacket(data,data.length , serverIPAddress, serverport);
            udpClientSocket.send(blankPacket);
            
        	// Create input stream
            BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
            while (true) 
            {
                if (stopped)
                    return;
 
                // Message to send
                String clientMessage = inFromUser.readLine();
 
                if (clientMessage.equals("."))
                    break;
 
                // Create byte buffer to hold the message to send
                byte[] sendData = new byte[1024];
 
                // Put this message into our empty buffer/array of bytes
                sendData = clientMessage.getBytes();
 
                // Create a DatagramPacket with the data, IP address and port number
                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIPAddress, serverport);
 
                // Send the UDP packet to server
                //System.out.println("I just sent: "+clientMessage);
                udpClientSocket.send(sendPacket);
 
                Thread.yield();
            }
        }
        catch (IOException ex) {
            System.err.println(ex);
        }
    }
} 
