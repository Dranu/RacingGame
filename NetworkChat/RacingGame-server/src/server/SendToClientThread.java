/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.*;
import java.net.*;
import java.lang.*;

public class SendToClientThread implements Runnable {
    private Socket sock=null;
    private BufferedReader brBufferedReader = null;
    public SendToClientThread(Socket sock){
        this.sock=sock;
    }
    public void run(){
        
    }
}


