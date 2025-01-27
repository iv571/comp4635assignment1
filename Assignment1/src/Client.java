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
	private static final String USAGE = "Usage: java Client [host] [port] [request]"; // [request] is not used - redundant code
	
	public static void main(String[] args) throws IOException {
		Client instance = new Client();
		
		 if (args.length != 3) {
				System.out.println(USAGE);
				System.exit(1);
			}
		
		
		instance.accessServer(args[0], Integer.parseInt(args[1]));
		
	}
	
	static void accessServer(String args, int port) {
		Socket link = null; //Step 1.
		try {
			host = InetAddress.getLocalHost(); // This might be not neccessary 
			link = new Socket(args, port); //Step 1.
			
			Scanner input = new Scanner(link.getInputStream()); //Step 2.
			PrintWriter output = new PrintWriter(link.getOutputStream(),true); //Step 2.
			
			Scanner userEntry = new Scanner(System.in);
			
			String message, response;
			
			System.out.println("Enter username: ");
			String username = userEntry.nextLine();
			System.out.println("Enter password: ");
			String password = userEntry.nextLine();
			System.out.println("Press enter to start: ");
			
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