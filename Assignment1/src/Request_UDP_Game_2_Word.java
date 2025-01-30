import java.io.ByteArrayInputStream;
import java.net.SocketTimeoutException;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.Arrays;
/**
 * 
 *  this class mainly operate sending request and receive result from word server
 *  
 */
public class Request_UDP_Game_2_Word {
	
	private static final int WORD_SERVER_PORT = 5600;
	
	private static final String WORD_SERVER_host = "localhost";

	private static final int BUFFER_LIMIT = 1024;
	
	private DatagramSocket socket = null;
		
	private String[] game_map = null;
	/**
	 * create the socket to send out
	 * @param local_host it should be the same as game server
	 * @throws IOException
	 */
	public Request_UDP_Game_2_Word(int local_host_port) throws IOException {
		
		socket = new DatagramSocket(local_host_port);
		
		game_map = new String[1];

	}
	
	/**
	 * Method: send_request
	 * a high level view of how sending request operated
	 * 
	 * first pack the request data
	 * then send the request
	 * recive_respond from word server
	 * return the result
	 * 
	 * @param request  request as int 
	 * @param word     depend of the request the word can be null
	 * @param word_len depend of the request the word_len can be null
	 * @param local_host_port same port as game server
	 * @return the result of the request
	 * @ author Stanley
	 */
	public static String[] send_request (int request, String word, int word_len, int local_host_port) {
		
		try {
	        
			Request_UDP_Game_2_Word client_server = new Request_UDP_Game_2_Word(local_host_port);

        	byte[] request_buf = client_server.pack_request_data (word, request, word_len);

        	client_server.send_request (request_buf);
        	       
        	int result = client_server.receive_respond (word_len, request);

        	client_server.print_result(result, request);

			return client_server.game_map;
        
        } catch (NumberFormatException e) {
        	
			System.err.println("Invalid port number: " + local_host_port + ".");
			
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
		
		final int TIME_ERR = -2;
		
		if (result == TIME_ERR) {
			
			System.err.println("Timeout reached! No packet received.");
    		
    		game_map[0] = "TIME";
			
    		return;
		}
			
		
		if (request != GENERATE_MAP) {
        	
        	if (result == 1) { // 1 = true
        		
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
	
	/**
	 * Method: pack_request_data
	 * 
	 * depend on the request pack in the request info into packet
	 * 
	 * when the request is GENERATE_MAP only two int need to be packed in packet 
	 * 
	 * the fisrt int is request and the 2nd int is word len
	 * 
	 * when the request is other then generate_map two int and 1 string has to be packed
	 * into packet 
	 * the 1st int is request, 2nd int is the size the string so the server can correctly 
	 * read the string
	 * 
	 * @param target_word
	 * @param request
	 * @param word_num
	 * @return the packed info with byte array
	 * @throws IOException
	 * @ author Stanley
	 */
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
	/*
	 * Method: send_request
	 * 
	 * send the request to word server
	 * 
	 * @param request_buf  the packed request info
	 * 
	 * @throws IOException
	 * @ author Stanley
	 */
	private void send_request (byte[] request_buf) throws IOException {
		
    	    	  
        InetAddress address = InetAddress.getByName(WORD_SERVER_host);
        
        DatagramPacket packet = new DatagramPacket(request_buf, request_buf.length, address, WORD_SERVER_PORT);
        
        socket.send(packet);
		
        return;
		
	}
	
	/**
	 * Method: receive_respond
	 * 
	 * receive the result of request.
	 * 
	 * if the request is GENERATE_MAP then store the result in game map
	 *
	 * 
	 * 
	 * @param word_len
	 * @param request
	 * @return                  if GENERATE_MAP return -1 
	 * 							else if return result 
	 * @throws IOException
	 * @ author Stanley
	 */
	
	private int receive_respond (int word_len, int request) throws IOException {
		
		int result = 0;
		
		final int GENERATE_MAP = 0;
		
        byte[] receive_buf = new byte[BUFFER_LIMIT];
        
        socket.setSoTimeout(5000);

        DatagramPacket udp_receive_packet = new DatagramPacket(receive_buf, receive_buf.length);
        
        try {
        	
        	socket.receive(udp_receive_packet);
        
        	}	
        	
        catch(SocketTimeoutException e) {
        	
            
            return -2;
            
        	}
        
        ByteArrayInputStream byteStream = new ByteArrayInputStream(udp_receive_packet.getData(), 0, udp_receive_packet.getLength());
        
        DataInputStream dataStream = new DataInputStream(byteStream);
        
    	socket.close();

        if (request != GENERATE_MAP)
        	
        	result = dataStream.readInt();

        else {
        	
        	game_map = extract_word_stem(dataStream, word_len);
        
        	result = -1;
        	
        }
        	
        	return result;
        
    
	}

	/**
	 * Method: extract_word_stem
	 * a array of string which index 0 = verticle word the rest are horizontal word
		store them it to game_map
	 * @param dataStream
	 * @param word_len
	 * @return
	 * @throws IOException
	 * @ author Stanley
	 */
	
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
