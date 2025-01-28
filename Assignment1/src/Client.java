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