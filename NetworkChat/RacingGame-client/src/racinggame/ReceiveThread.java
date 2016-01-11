/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package racinggame;
import java.util.Scanner;
import java.io.*;
import java.net.*;

public class ReceiveThread implements Runnable {
    private Socket sock=null;
    private BufferedReader receive=null;
    public ReceiveThread(Socket sock){
        this.sock=sock;
    }
    public void run(){
        
    }
    
}
