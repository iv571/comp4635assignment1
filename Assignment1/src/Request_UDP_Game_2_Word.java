import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

public class Request_UDP_Game_2_Word {
	
	private final int SERVER_PORT = 5600;
	
	private final int BUFFER_LIMIT = 1024;
	
	private DatagramSocket socket = null;
		
	private char[][] game_map = null;
	
	private int request = 2;

	public Request_UDP_Game_2_Word(int port) throws IOException {
		
		socket = new DatagramSocket(port);
	}
	
	public static void main(String[] args) throws IOException {
		
		Request_UDP_Game_2_Word client_server = check_connection_validity(args);
		
        try {
        
        	byte[] request_buf = client_server.pack_request_data (args, "aa", 0, 0);

        	client_server.send_request (request_buf, args);
        	       
        	int result = client_server.receive_respond ();

	        if (client_server.game_map == null) {
	        	
	        	
	        	if (result == 1)
	        		System.err.println("TRUE");
	        	else
	        		System.err.println("FALSE");
	        }
        	
        } catch (NumberFormatException e) {
        	
			System.err.println("Invalid port number: " + args[1] + ".");
			
			System.err.println(e.getMessage());
			
			System.exit(1);
			
		} catch (Exception e) {
			
			System.err.println(e.getMessage());
			
			System.exit(1);
			
		}
        
	}
	
	private static Request_UDP_Game_2_Word check_connection_validity(String[] args) {
		
		int port = 0;
		
		Request_UDP_Game_2_Word client_server = null;
				

        if (args.length != 2) {
        	
			System.out.println("java SingleRequestUDPClient [host] [port]");
			
			System.exit(1);
		}
        
		try {
		    
			port = Integer.parseInt(args[1]);
			
			client_server = new Request_UDP_Game_2_Word(port);
       
		} catch (NumberFormatException e) {
		
			System.err.println("Invalid port number: " + port + ".");
			
			System.exit(1);
	
		} catch (IOException e) {
        
			System.out.println("Exception caught when trying to listen on port "
                + port);
           
			System.out.println(e.getMessage());
        }
		
		return client_server;
		
	}
	
	private byte[] pack_request_data (String[] args, String target_word, int word_num, int failed_attempts_num) throws IOException {
		
		final int GENERATE_MAP = 0;
		
		ByteArrayOutputStream byteStream_out = new ByteArrayOutputStream();
		
        DataOutputStream dataStream_out = new DataOutputStream(byteStream_out);
		
        dataStream_out.writeInt(request);
        
        if (request != GENERATE_MAP) {
        
        	dataStream_out.writeInt(target_word.length());
        
        	dataStream_out.writeBytes(target_word);
      
        } else {
        	
        	dataStream_out.writeInt(word_num);
        	
        	dataStream_out.writeInt(failed_attempts_num);

        }
        
       return byteStream_out.toByteArray();

		
	}
	
	private void send_request (byte[] request_buf, String[] args) throws IOException {
		
		String host = args[0];
    	    	  
        InetAddress address = InetAddress.getByName(host);
        
        DatagramPacket packet = new DatagramPacket(request_buf, request_buf.length, address, SERVER_PORT);
        
        socket.send(packet);
		
        return;
		
	}
	
	private int receive_respond () throws IOException {
		
		int result = 0;
		
        byte[] receive_buf = new byte[BUFFER_LIMIT];

        DatagramPacket udp_receive_packet = new DatagramPacket(receive_buf, receive_buf.length);
        
        socket.receive(udp_receive_packet);
        
        ByteArrayInputStream byteStream = new ByteArrayInputStream(udp_receive_packet.getData(), 0, udp_receive_packet.getLength());
        
        DataInputStream dataStream = new DataInputStream(byteStream);
        
    	socket.close();

        if (request != 0)
        	
        	result = dataStream.readInt();

        else {}
        	// read the game map
        	
        	return result;
        
        
        
	}
	
	
}
