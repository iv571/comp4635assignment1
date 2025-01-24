import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;

public class Request_UDP_Game_2_Word {
	
	private static final int WORD_SERVER_PORT = 5600;
	
	private static final String WORD_SERVER_host = "localhost";

	private static final int BUFFER_LIMIT = 1024;
	
	private DatagramSocket socket = null;
		
	private String[] game_map = null;
	
	public Request_UDP_Game_2_Word(int local_host) throws IOException {
		
		socket = new DatagramSocket(local_host);
		
		game_map = new String[1];

	}
	
	public static String[] send_request (int request, String word, int word_len, int local_host) {
		
		try {
	        
			Request_UDP_Game_2_Word client_server = new Request_UDP_Game_2_Word(local_host);

        	byte[] request_buf = client_server.pack_request_data (word, request, word_len);

        	client_server.send_request (request_buf);
        	       
        	int result = client_server.receive_respond (word_len, request);

        	client_server.print_result(result, request);

			return client_server.game_map;
        
        } catch (NumberFormatException e) {
        	
			System.err.println("Invalid port number: " + local_host + ".");
			
			System.err.println(e.getMessage());
			
			System.exit(1);
			
		} catch (Exception e) {
			
			System.err.println(e.getMessage());
			
			System.exit(1);
			
		}
        
		return null;
		
	}
	
	private void print_result (int result, int request) {
		
		final int GENERATE_MAP = 0;
		
		if (request != GENERATE_MAP) {
        	
        	if (result == 1) {
        		
        		System.err.println("Request processed");
        		
        		game_map[0] = "T";
        	
        	}else{
        		
        		System.err.println("Request fail");
    			
        		game_map[0] = "F";
        	}
        	
        	
        } else {
        	
    		System.err.println("Game Map generated\n" + Arrays.toString(game_map));
    		
        }
		
	}
	
	
	private byte[] pack_request_data (String target_word, int request ,int word_num) throws IOException {
		
		final int GENERATE_MAP = 0;
		
		ByteArrayOutputStream byteStream_out = new ByteArrayOutputStream();
		
        DataOutputStream dataStream_out = new DataOutputStream(byteStream_out);
		
        dataStream_out.writeInt(request);
        
        if (request != GENERATE_MAP) {
        
        	dataStream_out.writeInt(target_word.length());
        
        	dataStream_out.writeBytes(target_word);
      
        } else 
        	
        	dataStream_out.writeInt(word_num);
        	
        
        
       return byteStream_out.toByteArray();

		
	}
	
	private void send_request (byte[] request_buf) throws IOException {
		
    	    	  
        InetAddress address = InetAddress.getByName(WORD_SERVER_host);
        
        DatagramPacket packet = new DatagramPacket(request_buf, request_buf.length, address, WORD_SERVER_PORT);
        
        socket.send(packet);
		
        return;
		
	}
	
	private int receive_respond (int word_len, int request) throws IOException {
		
		int result = 0;
		
        byte[] receive_buf = new byte[BUFFER_LIMIT];

        DatagramPacket udp_receive_packet = new DatagramPacket(receive_buf, receive_buf.length);
        
        socket.receive(udp_receive_packet);
        
        ByteArrayInputStream byteStream = new ByteArrayInputStream(udp_receive_packet.getData(), 0, udp_receive_packet.getLength());
        
        DataInputStream dataStream = new DataInputStream(byteStream);
        
    	socket.close();

        if (request != 0)
        	
        	result = dataStream.readInt();

        else {
        	
        	game_map = extract_word_stem(dataStream, word_len);
        
        	result = -1;
        	
        }
        	
        	return result;
        
    
	}

	private String[] extract_word_stem(DataInputStream dataStream, int word_len) throws IOException {

		int string_len;
		
		byte[] string_bytes;

		String[] word_stem = new String[word_len];
		
		for (int index = 0; index < word_len; index++) {
			
			string_len = dataStream.readInt();
			
			string_bytes = new byte [string_len];
			
			dataStream.readFully(string_bytes);
			
			word_stem[index] = new String (string_bytes);
						
		}
		
		return word_stem;

	}
	
	
}
