/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;


import java.io.*;
import java.net.*;
import java.lang.*;

public class Server {
	public static void main(String[] args) throws IOException {
		final int port = 4444;
		System.out.println("Server waiting for connection on port "+port);
		ServerSocket ss = new ServerSocket(port);
		Socket clientSocket = ss.accept();
		System.out.println("Recieved connection from "+clientSocket.getInetAddress()+" on port "+clientSocket.getPort());
		//create two threads to send and recieve from client
		RecieveFromClientThread recieve = new RecieveFromClientThread(clientSocket);
		Thread thread = new Thread(recieve);
		thread.start();
		//SendToClientThread send = new SendToClientThread(clientSocket);
		//Thread thread2 = new Thread(send);
		//thread2.start();
	}
}
