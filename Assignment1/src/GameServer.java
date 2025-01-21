/**
 * Title: COMP4635 Assignment 1 Game Server
 * Usage: java MultithreadReverseEchoServer [port] 
 */


import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import java.io.*;


public class GameServer {
	private static final String USAGE = "Usage: java GameServer [port]";
	private int count = 0;
	
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println(USAGE);
			System.exit(1);
		}

		int port = 0;
		ServerSocket server = null;

		try {
			port = Integer.parseInt(args[0]);
			server = new ServerSocket(port);
			System.out.println("The game server is running...");
			ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);
			while (true) {
				fixedThreadPool.execute(new ReverseEchoClientHandler(server.accept()));
			}
		} catch (IOException e) {
			System.out.println(
					"Exception caught when trying to listen on port " + port + " or listening for a connection");
			System.out.println(e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private static String getRandomWordFromFile(int minLength) {
		List<String> words = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("words.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                // Skip empty lines and lines that do not meet the minimum length.
                if (!line.isEmpty() && line.length() >= minLength) {
                    words.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading words.txt: " + e.getMessage());
        }
        if (words.isEmpty()) {
            return "";
        }
        Random rand = new Random();
        System.out.println(rand.nextInt(words.size()));
        return words.get(rand.nextInt(words.size()));
	  }
	
	private static String getConstrainedRandomWord(char constraint, int minLength) {
		List<String> words = new ArrayList<>();
	    try (BufferedReader br = new BufferedReader(new FileReader("words.txt"))) {
	        String line;
	        while ((line = br.readLine()) != null) {
	            line = line.trim();
	            // Check if the line is not empty, meets the minimum length,
	            // and starts with the specified constraint character.
	            if (!line.isEmpty() && line.length() >= minLength && line.charAt(0) == constraint) {
	                words.add(line);
	            }
	        }
	    } catch (IOException e) {
	        System.err.println("Error reading words.txt: " + e.getMessage());
	    }

	    // If no matching word is found, fall back to a general random word from the file.
	    if (words.isEmpty()) {
	        return getRandomWordFromFile(minLength);
	    }
	    
	    Random rand = new Random();
	    return words.get(rand.nextInt(words.size()));
      
    }
	
	private static char[][] constructPuzzle(String verticalStem, String[] horizontalWords) {
        // Determine puzzle dimensions.
        int numRows = verticalStem.length();
        int numCols = 18;
        char[][] grid = new char[numRows][numCols];

        // Initialize the grid with '.' to denote empty spaces.
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                grid[i][j] = '.';
            }
        }

        // Place the vertical stem in a fixed column (e.g., column 4).
        int colForStem = 4;
        for (int row = 0; row < verticalStem.length(); row++) {
            grid[row][colForStem] = verticalStem.charAt(row);
        }

        // Place horizontal words in the corresponding rows.
        // Each horizontal word is placed starting at the vertical stem column,
        // ensuring that the word overlaps with the vertical letter.
        for (int row = 0; row < horizontalWords.length && row < numRows; row++) {
            String word = horizontalWords[row];
            for (int j = 0; j < word.length() && (colForStem + j) < numCols; j++) {
                grid[row][colForStem + j] = word.charAt(j);
            }
        }
        return grid;
    }
	
	/**
     * Counts the total number of characters in the puzzle that are not the empty placeholder ('.').
     * Author: Iyan Velji
     * @param puzzle The puzzle grid.
     * @return The count of filled letters.
     */
	private static int countPuzzleLetters(char[][] puzzle) {
        int count = 0;
        for (char[] row : puzzle) {
            for (char c : row) {
                if (c != '.') {
                    count++;
                }
            }
        }
        return count;
    }
	
	
	/**
     * Formats the puzzle grid into a string.
     *
     * Each row is terminated with a '+' character and a newline, emulating the provided format.
     *
     * Author: Iyan Velji
     * @param puzzle The puzzle grid.
     * @return A string representing the formatted puzzle.
     */
    private static String formatPuzzle(char[][] puzzle) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < puzzle.length; i++) {
            for (int j = 0; j < puzzle[i].length; j++) {
            	char c = puzzle[i][j];
//                sb.append(puzzle[i][j]);
                sb.append(c == '.' ? '.' : '_');
            }
            sb.append("+\n"); // Denote end of row with a '+' followed by a newline.
        }
        return sb.toString();
    }
    
    private static String formatPuzzleWithRevealed(char[][] solution, boolean[][] revealed) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < solution.length; i++) {
            for (int j = 0; j < solution[i].length; j++) {
                if (solution[i][j] == '.') {
                    // Always show the placeholder.
                    sb.append('.');
                } else {
                    // Show the letter if it has been revealed; otherwise, underscore.
                    sb.append(revealed[i][j] ? solution[i][j] : '_');
                }
            }
            sb.append("+\n");
        }
        return sb.toString();
    }
    
    /**
     * Formats the puzzle grid into a string.
     *
     * Each row is terminated with a '+' character and a newline, emulating the provided format.
     *
     * Author: Iyan Velji
     * @param puzzle The puzzle grid.
     * @return A string representing the formatted puzzle.
     */
    private static String revealPuzzle(char[][] puzzle) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < puzzle.length; i++) {
            for (int j = 0; j < puzzle[i].length; j++) {
            	char c = puzzle[i][j];
//                sb.append(puzzle[i][j]);
                sb.append(c);
            }
            sb.append("+\n"); // Denote end of row with a '+' followed by a newline.
        }
        return sb.toString();
    }


	
	private static class ReverseEchoClientHandler implements Runnable {
		private Socket clientSocket;

		ReverseEchoClientHandler(Socket socket) {
			this.clientSocket = socket;
		}

		@Override
		public void run() {
			System.out.println("Connected, handling new client: " + clientSocket);
		
			
			
			try {
				PrintStream out = new PrintStream(clientSocket.getOutputStream());
				
				
				
				
				Scanner in = new Scanner(new InputStreamReader(clientSocket.getInputStream()));
				
				
				

				// Read the request, reverse it, and echo it back

				while (in.hasNextLine()) {
					String inputLine = in.nextLine();
					System.out.println("Received the following message from" + clientSocket + ":" + inputLine);
					
					
					
					
					
					if (inputLine.matches("start\\s+\\d+\\s+\\d+")) {
						
						
						String[] parts = inputLine.split("\\s+");
						
						int i = Integer.parseInt(parts[1]);
					    int f = Integer.parseInt(parts[2]);
					    
					    
					    
					    
					   
						
						String verticalStem = getRandomWordFromFile(i - 1);
						int numHorizontalWords = i - 1;
				        String[] horizontalWords = new String[numHorizontalWords];
				        for (int j = 0; j < numHorizontalWords; j++) {
				            // Constraint: horizontal word starts with the corresponding letter of the vertical stem.
				            char constraint = verticalStem.charAt(j % verticalStem.length());
				            horizontalWords[j] = getConstrainedRandomWord(constraint, i - 1);
				            System.out.println("Horizontal word for letter '" + constraint + "': " + horizontalWords[j]);
				        }
				        
				        char[][] puzzle = constructPuzzle(verticalStem, horizontalWords);
				        
				        int numPuzzleLetters = 0;
				        
				        numPuzzleLetters += verticalStem.length();
				        
				        for (String word : horizontalWords) {
				            numPuzzleLetters += word.length();
				        }
				        
				        int failAttempts = f * numPuzzleLetters;
				        out.println("Level " + i + " selected");
				    
				        
				        int letterCount = countPuzzleLetters(puzzle);
//				        int allowedAttempts = letterCount * f;
				        
				        String formattedPuzzle = formatPuzzle(puzzle);
				        String revealedPuzzle = revealPuzzle(puzzle);
				        
				        System.out.println(revealedPuzzle);
				        
				        
				        boolean gameOn = true;
				        while (gameOn) {
				        		
				        	 if (!formattedPuzzle.contains("_")) {
						            out.println("Congratulations! You have completed the puzzle.");
						            gameOn = false;
						        }
						        
						        if (failAttempts <= 0) {
						            out.println("Game over! You have used all your attempts.");
						            out.println("The solution was:");
						            out.println(revealedPuzzle);
						            gameOn = false;
						        }
				        	
				        	
				        	if (gameOn) {
				        		out.println("Fail attempts remaining: " + failAttempts);
					        	out.println(formattedPuzzle);
				        	} else {
								out.print("start [level] [failed attempts factor]: \n");
								out.println();
				        	}
				    
				        
				        	
							String guess = in.nextLine().trim();
					        if (guess.isEmpty()) {
					            continue;
					        } else if (guess.length() == 1) {
					        	
					        	// Process a single letter guess.
					            char guessedLetter = guess.charAt(0);
					            boolean found = false;
					            
					            out.println("Guessed letter: " + guessedLetter);
					            // Loop over the solution grid (the "revealedPuzzle") to search for all instances.
					            for (int x = 0; x < revealedPuzzle.length(); x++) {
					               
					                	
					                    // If the cell contains the guessed letter and it hasn't already been revealed...
					                    if (revealedPuzzle.charAt(x) == guessedLetter && formattedPuzzle.charAt(x) == '_') {
//					                    	puzzle[i][j] = true;
					                        found = true;
					                        char [] formattedPuzzleChar = formattedPuzzle.toCharArray();
					                        formattedPuzzleChar[x] = guessedLetter;
					                        formattedPuzzle = String.valueOf(formattedPuzzleChar);
					                        System.out.println(guessedLetter + " found");
					                    }
					             
					                
					            }
					            if (found) {
					                out.println("Letter " + guessedLetter + " Correct");
					            } else {
					                out.println("Sorry, letter '" + guessedLetter + "' is not in the puzzle (or already revealed).");
					                failAttempts = failAttempts - 1;
					            }
					        } else if (guess.length() > 1) {
					        	String lowerRevealed = revealedPuzzle.toLowerCase();
					            String lowerGuess = guess.toLowerCase();
					            boolean foundWord = false;
					            int searchFrom = 0;
					            
					            
					            int colForStem = 4;  // Make sure this matches your constructPuzzle setting.
					            StringBuilder verticalStemBuilder = new StringBuilder();
					            int numRows = puzzle.length;
					            for (int row = 0; row < numRows; row++) {
					                verticalStemBuilder.append(puzzle[row][colForStem]);
					            }
					            String verticalStemString = verticalStemBuilder.toString().toLowerCase();
					            
					            if (verticalStemString.equals(lowerGuess)) {
					                // The guess matches the vertical stem.
					                // We update formattedPuzzle for each row to reveal the vertical letter.
					                char[] fpChars = formattedPuzzle.toCharArray();
					                int numCols = puzzle[0].length;
					                // We have built the puzzle string row by row, and each row ends with "+\n".
					                // Determine the total length per row (numCols characters + 2 extra for '+' and newline).
					                int rowLength = numCols + 2;  // adjust if your formatting changes.
					                for (int row = 0; row < numRows; row++) {
					                    int rowStart = row * rowLength;   // Starting index of the row in formattedPuzzle.
					                    int index = rowStart + colForStem;  // The index corresponding to the vertical letter.
					                    fpChars[index] = puzzle[row][colForStem];
					                }
					                formattedPuzzle = String.valueOf(fpChars);
					                out.println("Vertical stem \"" + guess + "\" is correct!");
					                foundWord = true;
					            }
					            
					            while ((searchFrom = lowerRevealed.indexOf(lowerGuess, searchFrom)) != -1) {
					                // For each occurrence, update the corresponding part in formattedPuzzle.
					                char[] formattedPuzzleChars = formattedPuzzle.toCharArray();
					                for (int w = searchFrom; w < searchFrom + guess.length(); w++) {
					                    formattedPuzzleChars[w] = revealedPuzzle.charAt(w);
					                }
					                formattedPuzzle = String.valueOf(formattedPuzzleChars);
					                out.println("Found the word at position: " + searchFrom);
					                foundWord = true;
					                // Move search index beyond this occurrence.
					                searchFrom += guess.length();
					            }
					            
					            if (foundWord) {
					                out.println("Word \"" + guess + "\" is correct!");
					            } else {
					                out.println("Sorry, the word \"" + guess + "\" is not in the puzzle (or already revealed).");
					                failAttempts = failAttempts - 1;
					            }
					        }
					        
					       
				        }
				        	
				        
				        
					} else {
						out.print("Connected to the game server \n");
						out.print("start [level] [failed attempts factor]: \n");
						out.println();
					}
					
				}
			} catch (SocketException e) {
				System.out.println("Error: " + e.getMessage());
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					clientSocket.close();
				} catch (IOException e) {
				}
				System.out.println("Closed: " + clientSocket);
			}
		}
	}

}
