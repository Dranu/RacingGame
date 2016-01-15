/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/*Source: https://www.daniweb.com/programming/software-development/threads/392710/basic-udp-chat-system*/
package racinggame;
import java.util.Scanner;
import java.io.*;
import java.net.*;

public class ChatClient{
    private int clientport = 7777;
    private String host = "localhost";
    
    public void run(){
        try {
            InetAddress ia = InetAddress.getByName(host);
 
            SendThread sender = new SendThread(ia, clientport);
            sender.start();
            ReceiveThread receiver = new ReceiveThread(sender.getSocket());
            receiver.start();
        }catch (Exception e) {System.out.println(e.getMessage());
        
        }
    }
}
