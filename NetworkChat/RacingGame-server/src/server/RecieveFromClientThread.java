/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import java.io.*;
import java.net.*;
import java.lang.*;

public class RecieveFromClientThread implements Runnable {
    private Socket sock=null;
    private BufferedReader brBufferedReader = null;
    
    public RecieveFromClientThread(Socket sock){
        this.sock=sock;
    }
    public void run(){
        try{
		brBufferedReader = new BufferedReader(new InputStreamReader(this.sock.getInputStream()));
                String messageString;
		while(true){
		while((messageString = brBufferedReader.readLine())!= null){//assign message from client to messageString
			if(messageString.equals("EXIT"))
			{
				break;//break to close socket if EXIT
			}
			System.out.println("From Client: " + messageString);//print the message from client
		}
		this.sock.close();
		System.exit(0);
	}
		
	}
	catch(Exception ex){System.out.println(ex.getMessage());}
	}
}//end class RecieveFromClientThread
    
    



