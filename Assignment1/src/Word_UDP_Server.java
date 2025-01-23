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

public class Word_UDP_Server {

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
	
	public static void main(String[] args) throws IOException {
		
      
		Word_UDP_Server server = check_connection_validity(args);
		
		new Word ("words.txt");
		
		server.serve();
		
		server.socket.close();
    
	}
	
	private static Word_UDP_Server check_connection_validity(String[] args) {
		
		int port = 0;
		
		Word_UDP_Server server = null;
				
		if (args.length != 1) {
	        
        	System.err.println("Usage: java BasicUDPTimeServer [port]");
           
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
	private int process_request (DataInputStream dataStream) throws IOException{
		
		System.out.println("processing request");
		
		byte[] string_bytes = null;

		String target_word = null;

		int request = dataStream.readInt();   		
		
		int word_num = 0, string_len = 0;
		
		
		if (request == GENERATE_MAP) {
			
			word_num = dataStream.readInt();
						
		} else {
			
			string_len = dataStream.readInt();
			
			string_bytes = new byte [string_len];
			
			dataStream.readFully(string_bytes);
			
			target_word = new String (string_bytes);
			
		}
		
		Object result = 
				
				switch (request) {
					
					case GENERATE_MAP -> Word.generate_map(word_num);
				
					case ADD_WORD -> Word.add_word(target_word);
					
					case REMOVE_WORD -> Word.remove_word(target_word);
					
					case CHECK_WORD -> Word.check_word(target_word);
					
					default -> throw new IllegalArgumentException("Unexpected value: " + request);
				
				};
	
		if (result instanceof List)	{
			
			game_map = (List<String>) result;
			
			return -1;
		} 
	
		return (boolean) result ? 1 : 0;
	}
	
	private byte[] produce_out_stream_data (int result) throws IOException {
		
		byte[] respond_buf = null;
		
		ByteArrayOutputStream byteStream_out = new ByteArrayOutputStream();
			
        DataOutputStream dataStream_out = new DataOutputStream(byteStream_out);
        
        if (result != -1) {
        	
        	dataStream_out.writeInt(result);
        
        	respond_buf = byteStream_out.toByteArray();
        
        } else {
        	
        	// send the game map
        }
	
        return respond_buf;

	
	}
	
	private void send_result (byte[] respond_buf) throws IOException {
		
        int port = udp_receive_packet.getPort();
        
		System.out.println("Sending to port: " + port);

		InetAddress address = udp_receive_packet.getAddress();
        
        DatagramPacket udp_reply_packet = 
        		
        		new DatagramPacket(respond_buf, respond_buf.length, address, port);
        
        socket.send(udp_reply_packet);
        
        return;
	}
	
	private void serve() {
		
		while(true) {
			
			try {
				
				System.out.println("Listening for incoming requests...");

				DataInputStream dataStream = read_receive_request ();
                
                int result = process_request (dataStream);
                                
                byte[] respond_buf = produce_out_stream_data (result);
                
                send_result (respond_buf);
                
				System.out.println("Result has been sent!");

                
			} catch (SocketException e) {
				
				System.out.println(e.getMessage());
				
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}	
	}
	
    

}
