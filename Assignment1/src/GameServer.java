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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
	
	private static String getRandomWordFromFile(int minLength) {
		List<String> words = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("words.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and lines that do not meet the minimum length.
                if (!line.isEmpty() && line.length() >= minLength) {
                    words.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading words.txt: " + e.getMessage());
        }
        if (words.isEmpty()) {
            return "";
        }
        Random rand = new Random();
        System.out.println(rand.nextInt(words.size()));
        return words.get(rand.nextInt(words.size()));
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
						out.println(getRandomWordFromFile(2));
						out.println();
					} else if (inputLine.equals("2")) {
						out.println("Level 2 selected");
						out.println();
					} else if (inputLine.equals("3")) {
						out.println("Level 3 selected");
						out.println();
					} else if (inputLine.equals("4")) {
						out.println("Level 4 selected");
						out.println();
					} else if (inputLine.equals("5")) {
						out.println("Level 5 selected");
						out.println();
					} else {
						out.print("Connected to the game server \n");
						out.print("Select a game level to play (1-5): \n");
						out.println();
					}
					
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
