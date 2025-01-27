import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Word {
	
	private static List<String> words;
		
	public Word (String file_name){
		
		read_words_info(file_name);

	}

	private static void read_words_info(String file_name){

		BufferedReader br;
		
		words = new ArrayList<>();

		try {
			
			br = new BufferedReader(new FileReader(file_name));
			
			for (String line = br.readLine(); line != null; line = br.readLine()) {
                words.add(line.toLowerCase());
            }
		
		} catch (IOException e) {
			
			System.err.println("\nCouldn't read from file...");
		}
		
		return;

	}

	
	public static boolean add_word (String target_word) {
		
		int index = 0;
		
		int compare_result = 0;
		
		boolean added = false;

		while (index < words.size()) {
			
			compare_result = words.get(index).compareTo(target_word);
			
			if (compare_result >= 0)
				
				break;
			
			index++;
			
		}
		
		if (compare_result != 0) {
			
		 words.add(index, target_word);
		
		 added = true;
		 
		}
		
		return added;
	}
	
	public static boolean remove_word (String target_word) {
		
		boolean removed = false;
		
		int index = words.indexOf(target_word);
		
		if (index != -1) {
			
			words.remove(index);
		
			removed = true;
		}
		
		return removed;
		
	}
	
	public static boolean check_word (String target_word) {
		
		 if (words.contains(target_word)) 
			 
			 return true;
			
		return false;
	}
	
	public static List<String> generate_map(int word_len) {

	
		String verticle_stem = find_vertical_stem (word_len - 1);
		
		List<String> horizontal_stem = find_horizontal_stem (verticle_stem, word_len - 1);
		
		while (horizontal_stem == null)
			
			horizontal_stem = generate_map(word_len);
	
		horizontal_stem.add(0, verticle_stem);
		
		System.out.println("horizontal_stem: \n" + horizontal_stem);

		
		return horizontal_stem;
	}
	
	

	private static String find_vertical_stem (int word_len) {
		
		List<String> filtered_words = new ArrayList<>();
		
        for (String word : words) 
        
        	if (word.length() == word_len) 
        		
                filtered_words.add(word);
            
        if (filtered_words.isEmpty()) 
        		 
        	return null;
		
        int random_index = new Random().nextInt(filtered_words.size());

        return filtered_words.get(random_index);
        	 
        	 		
	}
	
	
	private static List<String> find_horizontal_stem(String verticle_stem, int word_len) {

		List<String> horizontal_stem = new ArrayList<>();

		System.out.println("verticle_stem: " + verticle_stem);
		
		for (int word_char_position = 0;  word_char_position < word_len;  word_char_position++) {
			
            char target_letter = verticle_stem.charAt(word_char_position);
            
            String horizontal_word = find_constrained_word(target_letter, word_len - 1);
                        
            if (horizontal_word == null)
            	
            	return null;
            	
            horizontal_stem.add(horizontal_word);
        }
		
		return horizontal_stem;
	}

	private static String find_constrained_word(char target_letter, int word_len) {

		List<String> candidate = new ArrayList<>();
		
		for (String word : words) 
							
                if (word.indexOf(target_letter) != -1) {
                 
                	candidate.add(word);
			
                }
		
		if (candidate.isEmpty())
			
			return null;
		
	
		return candidate.get(new Random().nextInt(candidate.size()));

	}
	
}