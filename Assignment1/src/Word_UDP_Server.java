import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;
/**
 * Word_UDP_Server
 * 
 * List<String> game_map: map for the game. index 0: contain the verticle word 
 *  					  the rest horizontal words
 *  
 *  GENERATE_MAP, ADD_WORD, REMOVE_WORD, CHECK_WORD:
 *  each command represent a int. the packet must contain a int 
 *  number. base on the number to do the right request.
 *  Eg: when read 0, the request is GENERATE_MAP so on and so for
 */
public class Word_UDP_Server implements Runnable {

	private List<String> game_map = null;
	
	private static final int GENERATE_MAP = 0; 

	private static final int ADD_WORD = 1;
	
	private static final int REMOVE_WORD= 2;
	
	private static final int CHECK_WORD = 3;

	private DatagramSocket socket = null;
		
	DatagramPacket udp_receive_packet = null;
	
	public Word_UDP_Server(int port) throws IOException {
		
		socket = new DatagramSocket(port);
	}
	
	/**
	 * Method: main
	 * @param args the server has to be run with input args to indicated which port
	 * to bind
	 * @throws IOException
	 * @ author Stanley
	 */
	public static void main(String[] args) throws IOException {
		
      
		Word_UDP_Server server = check_connection_validity(args);
		
		new Word ("words.txt"); // activate word class to generate a word list from words.txt
		
		server.serve();
		
		server.socket.close();
    
	}
	
	/**
	 * Method: check_connection_validity
	 * check if there is port input when running the server
	 * also check if the port bind
	 * 
	 * @param args
	 * @return return a Word_UDP_Server if the connection is valid
	 * @ author Stanley
	 */
	private static Word_UDP_Server check_connection_validity(String[] args) {
		
		int port = 0;
		
		Word_UDP_Server server = null;
				
		if (args.length != 1) {
	        
        	System.err.println("Usage: java Word_UDP_Server [port]");
           
        	System.exit(1);
        }
		
		try {
		    
			port = Integer.parseInt(args[0]);
			
			server = new Word_UDP_Server(port);
       
		} catch (NumberFormatException e) {
		
			System.err.println("Invalid port number: " + port + ".");
			
			System.exit(1);
			
		} catch (IOException e) {
        
			System.out.println("Exception caught when trying to listen on port "
                + port);
           
			System.out.println(e.getMessage());
        }
		
		return server;
		
	}
	/**
	 * Method: read_receive_request
	 * 
	 * receive a packet and extract it to data Stream type then return it
	 * @return  data Stream with info
	 * @throws IOException
	 * @ author Stanley
	 */
	private DataInputStream read_receive_request () throws IOException {
		
		
		 byte[] input_buf = new byte[1024];
         
		 udp_receive_packet = new DatagramPacket(input_buf, input_buf.length);
         
         socket.receive(udp_receive_packet);
         
         ByteArrayInputStream byteStream = new ByteArrayInputStream(udp_receive_packet.getData(), 0, udp_receive_packet.getLength());
         
         DataInputStream dataStream = new DataInputStream(byteStream);
         
		System.out.println("receive_request...");

		return dataStream;

	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Method: process_request
	 * 
	 * read a int from the data stream into request
	 * 
	 * then base on the request read on the data Stream
	 * 
	 * if request if GENERATE_MAP it follow another int which indicate the len 
	 * of a word for game map
	 * 
	 * if other it follows with a int that indicate the size of the string in the
	 * packet and base on the size to read the string
	 * 
	 * then base on the request calling the corresponding method to operated the 
	 * request
	 * 
	 * when doing any operation on the request, these have to be lock to avoid 
	 * race condition
	 * 
	 * 
	 * @param dataStream the info from receive packet
	 * @return if requesting a game map return -1
	 * 			else if the other request successfully completed return 1
	 * 			else return 0
	 * @throws IOException
	 * @ author Stanley
	 */
	private int process_request (DataInputStream dataStream) throws IOException{
		
		System.out.println("processing request");
		
		byte[] string_bytes = null;

		String target_word = null;

		int request = dataStream.readInt();   		
		
		int word_num = 0, string_len = 0;

		Object result;
		
		if (request == GENERATE_MAP) 
			
			word_num = dataStream.readInt();

		 else {
			
			string_len = dataStream.readInt();
			
			string_bytes = new byte [string_len];
			
			dataStream.readFully(string_bytes);
			
			target_word = new String (string_bytes);
			
		}
		
		synchronized (new Object()) {
			 
			result = switch (request) {
			
				case GENERATE_MAP -> Word.generate_map(word_num);
			
				case ADD_WORD -> Word.add_word(target_word);
			   
				case REMOVE_WORD -> Word.remove_word(target_word);
			   
				case CHECK_WORD -> Word.check_word(target_word);
			   
				default -> throw new IllegalArgumentException("Unexpected value: " + request);
			   
				};		
		}
	
		if (result instanceof List)	{
			
			game_map = (List<String>) result;
			
			return -1;
		} 
	
		return (boolean) result ? 1 : 0;
	}
	/**
	 * Method: produce_out_stream_data
	 * prepare a out stream data to send back 
	 * depend on the request calling the corresponding method to pack the out stram data
	 * 
	 * if result = -1 only a int will be send to inform if the request successfully completed
	 * 
	 * @param result result of the request
	 * @return byte array with out put data
	 * @throws IOException
	 * @ author Stanley
	 */
	private byte[] produce_out_stream_data (int result) throws IOException {
		
		byte[] respond_buf = null;
		
		ByteArrayOutputStream byteStream_out = new ByteArrayOutputStream();
			
        DataOutputStream dataStream_out = new DataOutputStream(byteStream_out);
        
        if (result != -1) 
        	
        	dataStream_out.writeInt(result);
        
         else 
        	
        	dataStream_out = packed_word_stem (byteStream_out);
        	
        
    	respond_buf = byteStream_out.toByteArray();
        
        return respond_buf;

	
	}
	
	/**
	 * Method: packed_word_stem
	 * this is called when request if generating a map
	 * pack the words in to data stream. in order of int , string .....
	 * int indicated the size of the string
	 * 
	 * @param byteStream_out
	 * @return datastream with packed info
	 * @throws IOException
	 * @ author Stanley
	 */
	private DataOutputStream packed_word_stem(ByteArrayOutputStream byteStream_out) throws IOException {

		DataOutputStream dataStream_out = new DataOutputStream(byteStream_out);
		
		for (String word : game_map) {
			
			dataStream_out.writeInt(word.length());            

			dataStream_out.writeBytes(word); 
            
		}
		
		
		return dataStream_out;
		
	}
/**
 * Method: send_result
 * 
 * send back the result of the request.
 * 
 * @param respond_buf
 * @throws IOException
 * @ author Stanley
 */
	private void send_result (byte[] respond_buf) throws IOException {
		
        int port = udp_receive_packet.getPort();
        
		System.out.println("Sending to port: " + port);

		InetAddress address = udp_receive_packet.getAddress();
        
        DatagramPacket udp_reply_packet = 
        		
        		new DatagramPacket(respond_buf, respond_buf.length, address, port);
        
        socket.send(udp_reply_packet);
        
        return;
	}
	
	/**
	 * a high level view to process_request 
	 * first read_recives_request
	 * then process_request
	 * then produce_out_stream_data
	 * last send back the result
	 * 
	 * 
	 * Method: serve
	 * @ author Stanley
	 */
	void serve() {
		
		while(true) {
			
			try {
				
				System.out.println("Listening for incoming requests...");

				DataInputStream dataStream = read_receive_request ();
                
                int result = process_request (dataStream);
                                
                byte[] respond_buf = produce_out_stream_data (result);
                
                send_result (respond_buf);
                
                
			} catch (SocketException e) {
				
				System.out.println(e.getMessage());
				
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}	
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		serve();
	}
	
    

}