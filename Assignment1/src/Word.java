import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
/**
 * The Word class responds to any operation related to words, such as adding, 
 * checking, removing, and generating the game's crossword map.
 */
public class Word {
	
	private static List<String> words; //list of words that would output for game map
	/**
	 * 
	 * start by reading words in given file name
	 * 	
	 * @param file_name path that read in words and store in the List<String> words
	 */
	public Word (String file_name){
		
		read_words_info(file_name);

	}

	/**
	 * Method: read_words_info
	 * read and stores given file name in to List <String> words
	 * 
	 * @param file_name the list of words usually are stored in word.txt file
	 * @ author Stanley
	 */
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

	/**
	 * Method: add_word 
	 * 
	 * add a new word to the list.
	 * 
	 * if the word already exist, the new word would not add again.
	 * else add to the list
	 *  
	 * this method find the alphabet position and insert the 
	 * target word
	 * 
	 * the code uses of Lexicographical order to located the 
	 * correct alphabetical order
	 * 
	 * @param target_word the word that need to be added
	 * @return if the word already exist return false 
	 * 			else return true
	 * @ author Stanley
	 */
	public static boolean add_word (String target_word) {
		
		int index = 0;
		
		int compare_result = 0;
		
		boolean added = false;

		while (index < words.size()) {
			
			compare_result = words.get(index).compareTo(target_word);
			
			// lexicographically smaller or equal to the current word, 
			// the target word is inserted at the current index.
			if (compare_result >= 0) 
				
				break;
			
			index++;
			
		}
		
		if (compare_result != 0) { // check if the word exist in the list
			
		 words.add(index, target_word);
		
		 added = true;
		 
		}
		
		return added;
	}
	/**
	 * Method: remove_word
	 * 
	 * remove a word form the list
	 *  
	 * @param target_word the word that need to be removed from the list 
	 * 
	 * @return if the word exist then removed the word return true
	 * 		   else return false
	 * @ author Stanley
	 */
	public static boolean remove_word (String target_word) {
		
		boolean removed = false;
		
		int index = words.indexOf(target_word);
		
		if (index != -1) {
			
			words.remove(index);
		
			removed = true;
		}
		
		return removed;
		
	}
	/**
	 * Method: check_word
	 * 
	 * check if the given word contain in the list
	 * 
	 * @param target_word the word to check
	 * @return true if exist
	 * 			else false
	 * @ author Stanley
	 */
	public static boolean check_word (String target_word) {
		
		 if (words.contains(target_word)) 
			 
			 return true;
			
		return false;
	}
	/**
	 * Method: generate_map
	 * 
	 * first it find the vertical word by calling the method find_vertical_stem
	 * then base on this vertical word to generate word len - 1 horizontal word
	 * 
	 * if find_horizontal_stem can't find the matching horizontal word that correspond 
	 * to vertical word, this method genertae_map will be called again until it find all the words.
	 * we assume that the server read in words from words.txt every time, so it less likely
	 * to result in inf loop.
	 * 
	 * @param word_len number of word length need to find for words
	 * @return a list of string. index 0 is stored vertical word the rest contain horizontal words
	 * @ author Stanley
	 */
	public static List<String> generate_map(int word_len) {

	
		String verticle_stem = find_vertical_stem (word_len - 1);
		
		List<String> horizontal_stem = find_horizontal_stem (verticle_stem, word_len - 1);
		
		while (horizontal_stem == null) // if cant find the matching horizontal words
			
			horizontal_stem = generate_map(word_len);
	
		horizontal_stem.add(0, verticle_stem);
		
		
		return horizontal_stem;
	}
	
	
/**
 * Method: find_vertical_stem
 * find all the match word lenght and store them into a list string
 * then randomly choose one of the word from the list
 * 
 * @param word_len
 * @return random word from filtered_words
 * @ author Stanley
 */
	private static String find_vertical_stem (int word_len) {
		
		List<String> filtered_words = new ArrayList<>();
		
        for (String word : words) 
        
        	if (word.length() == word_len) 
        		
                filtered_words.add(word);
            
        if (filtered_words.isEmpty()) // no matching words
        		 
        	return null;
		
        int random_index = new Random().nextInt(filtered_words.size());

        return filtered_words.get(random_index);
        	 
        	 		
	}
	
	/**
	 * Method: find_horizontal_stem
	 * 
	 * check every char at vertical word and find a horizontal word that match
	 * with the char 
	 * 
	 * @param verticle_stem selected vertical word
	 * @param word_len number of horizontal word need to found
	 * @return a string list of horizontal words
	 * @ author Stanley
	 */
	private static List<String> find_horizontal_stem(String vertical_stem, int word_len) {

		List<String> horizontal_stem = new ArrayList<>();
		
		for (int word_char_position = 0;  word_char_position < word_len;  word_char_position++) {
			
            char target_letter = vertical_stem.charAt(word_char_position);
            
            String horizontal_word = find_constrained_word(target_letter, word_len - 1);
                        
            if (horizontal_word == null)
            	
            	return null;
            	
            horizontal_stem.add(horizontal_word);
        }
		
		return horizontal_stem;
	}
/**
 * Method: find_constrained_word
 * find a horizontal word that contains the target letter from vertical word
 * and randomly select one of the word as horizontal word
 * 
 * @param target_letter
 * @param word_len
 * @return a single horizontal word that contain the target_letter
 * @ author Stanley
 */
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