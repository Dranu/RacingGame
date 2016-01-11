/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package racinggame;
import java.util.Scanner;
import java.io.*;
import java.net.*;

public class ChatClient{
    
    
    public void run(){
        try {
            Socket sock = new Socket("localhost",4444);
            SendThread sendThread = new SendThread(sock);
            Thread thread = new Thread(sendThread);thread.start();
            ReceiveThread receiveThread = new ReceiveThread(sock);
            Thread thread2 =new Thread(receiveThread);thread2.start();
        }catch (Exception e) {System.out.println(e.getMessage());
        
        }
    }
}
