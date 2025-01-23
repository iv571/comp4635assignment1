import java.io.IOException;

public class Test_Game_2_Word {

	public static void main(String[] args) throws IOException {

		int request = 0;
	
		int word_len = 5;
		
		String word = "above";

		Request_UDP_Game_2_Word.send_request (request, word, word_len);
		
		
		
	}

}
