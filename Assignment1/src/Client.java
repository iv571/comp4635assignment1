/**
 * The Client class serves as the client-side application for connecting to the GameServer.
 * It establishes a socket connection to the server, facilitates user input, sends requests to the server,
 * and displays responses from the server.
 *
 * <p>
 * Key Responsibilities:
 * <ul>
 *     <li>Establishing a connection to the GameServer using TCP sockets.</li>
 *     <li>Handling user input from the console.</li>
 *     <li>Sending user commands to the server and displaying server responses.</li>
 *     <li>Managing the client-server communication lifecycle.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Usage: java Client [host] [port] [request]

 
 * Example: java Client localhost 5599 start
 *
 * @author Iyan Velji
 */

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Scanner;
import java.io.IOException;
import java.io.PrintWriter;  

public class Client {
	private static InetAddress host;
	private static final int PORT = 5599;
	 private static final int ACCOUNT_PORT = 5700;
	private static final String USAGE = "Usage: java Client [host] [port] [request]"; // [request] is not used - redundant code
	
	 /**
     * The entry point of the Client application.
     *
     * 
     * Validates command-line arguments and initiates the connection to the {@code GameServer}.
     * 
     *
     * @param args Command-line arguments. Expects exactly three arguments: host, port, and request.
     * @throws IOException If an I/O error occurs when creating the client instance.
     */
	public static void main(String[] args) throws IOException {
		Client instance = new Client();
		
		 if (args.length != 3) {
				System.out.println(USAGE);
				System.exit(1);
			}
		
		
		instance.accessServer(args[0], Integer.parseInt(args[1]));
		
	}
	
	/**
     * Establishes a connection to the {@code GameServer}, handles user input, sends requests,
     * and processes server responses.
     *
     * 
     * This method performs the following steps:
     * <ol>
     *     <li>Creates a socket connection to the specified server host and port.</li>
     *     <li>Initializes input and output streams for communication.</li>
     *     <li>Prompts the user to start and enters a loop to handle user commands.</li>
     *     <li>Sends user commands to the server and displays responses.</li>
     *     <li>Terminates the connection upon receiving the "CLOSE" command.</li>
     * </ol>
     * 
     *
     * @param serverHost The hostname or IP address of the GameServer.
     * @param port       The port number on which the GameServer is listening.
     */
	static void accessServer(String args, int port) {
		Socket link = null; //Step 1.
//		Socket accountSocket = null;
		try {
			host = InetAddress.getLocalHost(); // This might be not neccessary 
			link = new Socket(args, port); //Step 1.
//			accountSocket = new Socket(host, ACCOUNT_PORT);
//			
			Scanner input = new Scanner(link.getInputStream()); //Step 2.
			PrintWriter output = new PrintWriter(link.getOutputStream(),true); //Step 2.
//			 PrintWriter accountOut = new PrintWriter(accountSocket.getOutputStream(), true);
//	            Scanner accountIn = new Scanner(accountSocket.getInputStream());
//			
			Scanner userEntry = new Scanner(System.in);
			
			boolean authenticated = false;
            String username = "";
			
			String message, response;
			
		
	        
	        
	        

           System.out.println("Press Enter to start: ");
			
			do {
				message = userEntry.nextLine();
				output.println(message); //Step 3.
				
//	System.out.println("\nGAMESERVER> " + response);
				
				
	            while (input.hasNextLine() && !(response = input.nextLine()).isEmpty()) {
	                System.out.println("\nGAMESERVER> " + response);
	            }
			} while (!message.equals("CLOSE"));
		} catch(IOException ioEx) { 
			// handle exception
		} finally {
			try {
				link.close(); //Step 4.
			} catch(IOException ioEx) { 
				// handle exception
			}
		}
	}
	
	
	
}