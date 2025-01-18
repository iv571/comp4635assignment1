/**
 * Title: COMP4635 Assignment 1 Game Server
 * Usage: java MultithreadReverseEchoServer [port] 
 */


import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Scanner;

import java.io.*;


public class GameServer {
	private static final String USAGE = "Usage: java GameServer [port]";
	private int count = 0;
	
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println(USAGE);
			System.exit(1);
		}

		int port = 0;
		ServerSocket server = null;

		try {
			port = Integer.parseInt(args[0]);
			server = new ServerSocket(port);
			System.out.println("The game server is running...");
			ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);
			while (true) {
				fixedThreadPool.execute(new ReverseEchoClientHandler(server.accept()));
			}
		} catch (IOException e) {
			System.out.println(
					"Exception caught when trying to listen on port " + port + " or listening for a connection");
			System.out.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static class ReverseEchoClientHandler implements Runnable {
		private Socket clientSocket;

		ReverseEchoClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		@Override
		public void run() {
			System.out.println("Connected, handling new client: " + clientSocket);
		
			
			
			try {
				PrintStream out = new PrintStream(clientSocket.getOutputStream());
				
				
				
				
				Scanner in = new Scanner(new InputStreamReader(clientSocket.getInputStream()));
				
				
			

				// Read the request, reverse it, and echo it back

				while (in.hasNextLine()) {
					String inputLine = in.nextLine();
					System.out.println("Received the following message from" + clientSocket + ":" + inputLine);
					
					if (inputLine.equals("1")) {
						out.println("Level 1 selected");
					}
					
					out.print("Connected to the game server \n");
					out.print("Select a game level to play (1-5): \n");
					out.println();
				}
			} catch (SocketException e) {
				System.out.println("Error: " + e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					clientSocket.close();
				} catch (IOException e) {
				}
				System.out.println("Closed: " + clientSocket);
			}
		}
	}

}
