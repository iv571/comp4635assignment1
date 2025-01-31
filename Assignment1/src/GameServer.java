/**
 * Title: COMP4635 Assignment 1 Game Server
 * 
 * The {@code GameServer} class serves as the main server application for a multiplayer crossword puzzle game.
 * It handles client connections, manages game sessions, interacts with auxiliary services such as account management
 * and word services, and orchestrates the game logic.
 * 
 * Key Responsibilities:
 * Networking: Listens for incoming client connections on a specified port using TCP and UDP.
 * Concurrency: Utilizes a thread pool to handle multiple client connections simultaneously.
 * Game Management: Constructs crossword puzzles, processes client guesses, and manages game state.
 * Account Management: Facilitates user authentication and score tracking by communicating with an AccountServer.
 * Word Management: Interacts with a Word_UDP_Server to manage the word repository for the game.
 * 
 * @author Iyan Velji
 * 
 * Usage: java MultithreadReverseEchoServer [port] 
 */


import java.net.ServerSocket;
import java.util.concurrent.Executors;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import java.io.*;


public class GameServer {
	/**
     * The usage message displayed when incorrect arguments are provided.
     */
	private static final String USAGE = "Usage: java GameServer [port]";
	/**
     * A flag indicating whether the server is running. It's declared volatile to ensure visibility across threads.
     */
	private static volatile boolean isRunning = true;
	
	 /**
     * The entry point of the GameServer application.
     *
     * It validates command-line arguments, initializes the TCP server socket, starts auxiliary servers (Word UDP Server and Account Server),
     * and sets up a thread pool to handle incoming client connections.
     *
     * @param args Command-line arguments. Expects exactly one argument specifying the port number.
     * @throws IOException If an I/O error occurs when opening the socket.
     */
	public static void main(String[] args) throws IOException {
		if (args.length != 1) {
			System.err.println(USAGE);
			System.exit(1);
		}

		int port = 0;
		int udpPort = 5600;
		ServerSocket server = null;

		try {
			port = Integer.parseInt(args[0]);
			server = new ServerSocket(port);
			System.out.println("The game server is running...");
			
			Thread udpServerThread = new Thread(() -> {
	            try {
	                // Initialize the Word UDP Server
	                Word_UDP_Server udpServer = new Word_UDP_Server(udpPort);
	                System.out.println("Word UDP Server is running on port " + udpPort + "...");
	                // Initialize Word with words.txt
	                new Word("words.txt");
	                // Start serving
	                udpServer.serve();
	               
	            } catch (IOException e) {
	                System.err.println("Failed to start Word UDP Server on port " + udpPort + ": " + e.getMessage());
	                e.printStackTrace();
	                
	                
	            }
	            
		  });
			
			udpServerThread.start();
			
			// Start AccountServer in a new thread
	        Thread accountServerThread = new Thread(() -> {
	            try {
	                AccountServer accountServer = new AccountServer(5700);
	                Runtime.getRuntime().addShutdownHook(new Thread(() -> accountServer.stopServer()));
	                accountServer.startServer();
	            } catch (IOException e) {
	                System.err.println("Failed to start Account Server: " + e.getMessage());
	                e.printStackTrace();
	            }
	        });

	        accountServerThread.start();
			
			ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);
			while (isRunning) {
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
	
	/**
     * Retrieves a random word from the "words.txt" file that meets or exceeds the specified minimum length.
     *
     * @param minLength The minimum length of the word to retrieve.
     * @return A randomly selected word as a {@code String}. Returns an empty string if no suitable word is found.
     */
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
	
	/**
     * Retrieves a random word from "words.txt" that contains a specific constraint character and meets the minimum length requirement.
     * If no such word exists, it falls back to retrieving any random word that meets the minimum length.
     *
     * @param constraint The character that the word must contain.
     * @param minLength  The minimum length of the word.
     * @return A randomly selected word that meets the constraints. Returns an empty string if no suitable word is found.
     */
	private static String getConstrainedRandomWord(char constraint, int minLength) {
		List<String> words = new ArrayList<>();
	    try (BufferedReader br = new BufferedReader(new FileReader("words.txt"))) {
	        String line;
	        while ((line = br.readLine()) != null) {
	            line = line.trim();
	            // Check if the line is not empty, meets the minimum length,
	            // and starts with the specified constraint character.
	            if (!line.isEmpty() && line.length() >= minLength && line.toLowerCase().indexOf(Character.toLowerCase(constraint)) >= 0) {
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
	
	/**
     * Constructs a crossword puzzle grid by placing a vertical stem and corresponding horizontal words.
     *
     * @param verticalStem   The central vertical word in the puzzle.
     * @param horizontalWords An array of horizontal words to be placed intersecting the vertical stem.
     * @return A 2D character array representing the crossword puzzle grid.
     */
	private static char[][] constructPuzzle(String verticalStem, String[] horizontalWords) {
        // Determine puzzle dimensions.
        int numRows = verticalStem.length();
        int numCols = 20;
        char[][] grid = new char[numRows][numCols];

        // Initialize the grid with '.' to denote empty spaces.
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                grid[i][j] = '.';
            }
        }

        // Place the vertical stem in a fixed column (e.g., column 4).
        int colForStem = 10;
        for (int row = 0; row < verticalStem.length(); row++) {
            grid[row][colForStem] = verticalStem.charAt(row);
        }

        // Place horizontal words in the corresponding rows.
        // Each horizontal word is placed starting at the vertical stem column,
        // ensuring that the word overlaps with the vertical letter.
        for (int row = 0; row < horizontalWords.length && row < numRows; row++) {
            String hWord = horizontalWords[row];
            char constraint = verticalStem.charAt(row);
            
            int constraintIndex = hWord.toLowerCase().indexOf(Character.toLowerCase(constraint));
            
            
            
            if (constraintIndex < 0) {
                // The word doesn't actually contain the letter? skip
                continue;
            }
            
            int startCol = colForStem - constraintIndex;
            
            if (startCol < 0) {
                // Shift everything to the right so it starts at col 0
                startCol = 0;
            } else if (startCol + hWord.length() > numCols) {
                startCol = numCols - hWord.length();
            }
            
            for (int j = 0; j < hWord.length() && (colForStem + j) < numCols; j++) {
            	int currentCol = startCol + j;
            	
          
         
                    
                    if (currentCol >= 0 && currentCol < numCols) {
                        char existingChar = grid[row][currentCol];
                        char newChar = hWord.charAt(j);

                        if (existingChar == '.' || existingChar == newChar) {
                            grid[row][currentCol] = newChar;
                        } else {
                            // Handle conflicts (overlapping letters must match)
                            System.err.println("Conflict detected at row " + row + ", col " + currentCol + ". Existing: '" + existingChar + "', New: '" + newChar + "'.");
                            // Decide how to handle: skip, overwrite, or adjust
                            // For simplicity, we'll skip placing this word
                            grid[row][currentCol] = existingChar; // Keep existing
                        }
                    }
              
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
    
    private static void shutdownServer() {
        isRunning = false;
    }


	
    /**
     * The {@code ReverseEchoClientHandler} class is responsible for handling individual client connections.
     * It manages user authentication, processes game commands, and maintains the game state for each client.
     */
	private static class ReverseEchoClientHandler implements Runnable {
		private Socket clientSocket;
		private AccountClient accountClient;

		ReverseEchoClientHandler(Socket socket) {
			this.clientSocket = socket;
			
			try {
	            // Initialize AccountClient with appropriate parameters
	           
	            this.accountClient = new AccountClient("localhost", 5700);
	        } catch (Exception e) {
	            System.err.println("Failed to initialize AccountClient: " + e.getMessage());
	            e.printStackTrace();
	            // Optionally, you might want to close the clientSocket here or handle the error appropriately
	        }
		}
		
		 /**
         * Runs the client handler thread, managing the interaction between the server and the connected client.
         *
         * 
         * This includes handling authentication, processing various game commands, and managing the game loop.
         *
         */
		@Override
		public void run() {
			System.out.println("Connected, handling new client: " + clientSocket);
		
			
			
			try {
				PrintStream out = new PrintStream(clientSocket.getOutputStream());
				
				Scanner in = new Scanner(new InputStreamReader(clientSocket.getInputStream()));
				
				boolean authenticated = false;
				String username = null;

				// Read the request, reverse it, and echo it back

				while (in.hasNextLine()) {
					String inputLine = in.nextLine();
					System.out.println("Received the following message from" + clientSocket + ":" + inputLine);
//					BufferedReader inB = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
					
					// Authentication Loop
					while (!authenticated) {
						displayAuthenticationMenu(out);
						
					    String inLine = in.nextLine().trim();
					    
					    
					    String[] tokens = inLine.split("\\s+");
					    System.out.println(inLine);
					    
					    
					    if (tokens.length == 0) {
			                System.out.println("Invalid command.");
			            }
					    
					    String command = tokens[0].toUpperCase();

			            if (command.equals("CREATE") || command.equals("LOGIN")) {
			            	if (tokens.length != 3) {
		                        out.println("ERROR: " + command + " requires a username and password.");
		                        continue;
		                    }
			            	
			            	String user = tokens[1];
		                    String pass = tokens[2];
		                    
		                    String accountCommand = command + " " + user + " " + pass;
		                    
		                    String response;

	                        try {
	                            response = accountClient.sendCommand(accountCommand);
	                        } catch (IOException e) {
	                            out.println("ERROR: Unable to communicate with Account Server.");
	                            System.err.println("AccountServer communication error: " + e.getMessage());
	                            continue;
	                        }

	                        if (response == null) {
	                            out.println("ERROR: No response from Account Server.");
	                            continue;
	                        }

	                        if (response.startsWith("ERROR")) {
	                            out.println(response);
	                        } else {
	                            out.println(response);
	                            if (command.equals("LOGIN") && response.equalsIgnoreCase("Login successful.")) {
	                                authenticated = true;
	                                username = user;
	                                out.println("You are now logged in as " + username + ".");
	                                continue;
	                            } else if (command.equals("CREATE") && response.equalsIgnoreCase("Account created successfully.")) {
	                                out.println("You can now log in with your credentials.");
	                                continue;
	                            }
	                        }
	                        
	                        
	                    out.println(); // Add an empty line for readability
		                    
		                    
			            } else {
			                out.println("Please create an account or login first.");
			                out.println();
			            }
			     

					    
					}
					
					
					if (inputLine.matches("start\\s+\\d+\\s+\\d+")) {
						
						String[] parts = inputLine.split("\\s+");
						
						int i = Integer.parseInt(parts[1]);
					    int f = Integer.parseInt(parts[2]);
						
						String verticalStem = getRandomWordFromFile(i - 1);
						System.out.println("Vertical Stem: " + verticalStem);
						String [] wordServiceRequest = Request_UDP_Game_2_Word.send_request(0, verticalStem, verticalStem.length(), 0);
						String[] gameMapArray = Arrays.copyOfRange(wordServiceRequest, 1, i);
						 List<String> gameMap = Arrays.asList(wordServiceRequest);
						
				        
				        char[][] puzzle = constructPuzzle(gameMap.get(0), gameMapArray);
				        
				        int numPuzzleLetters = 0;
				        
				        numPuzzleLetters += verticalStem.length();
				        
				        for (String word : gameMapArray) {
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
						            
						            String response = increaseScore(username);
						            
						            out.println(response);
						            
						            out.println("Press enter: ");
					            	out.println();
					            	gameOn = false;
					            	continue;
						        }
						        
						        if (failAttempts <= 0) {
						            out.println("Game over! You have used all your attempts.");
						            out.println("The solution was:");
						            out.println("Press Enter: ");
						            out.println(revealedPuzzle);
						           
						            
						            String response = decreaseScore(username);
						            
						            out.println(response);
						            
						            gameOn = false;
						            continue;
						        }
				        	
				        	
				        	if (gameOn) {
				        		out.println("Fail attempts remaining: " + failAttempts);
					        	out.println(formattedPuzzle);
				        	} else {
								out.print("start [level] [failed attempts factor]: \n");
								out.println();
				        	}
				    
				        
				        	
							String guess = in.nextLine().trim();
							char guessedLetter = guess.charAt(0);
							
							if (guessedLetter == '$') {
				            	out.println("Displaying score: ");
				            	String response = displayScore(username);
				            	out.println(response);
				            	continue;
				            } else if (guessedLetter == '!') {
				            	out.println("Starting new game ...");
				            	out.println("Press enter: ");
				            	out.println();
				            	gameOn = false;
				            	continue;
				            } else if (guessedLetter == '#') {
				            	out.println("Ending the game ...");
				            	shutdownServer();
				            	break;
				            
				            }
							
							
					        if (guess.isEmpty()) {
					            continue;
					        } else if (guess.length() == 1) {
					        	
					        	// Process a single letter guess.
					            
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
					           
					        } else if (guess.length() > 1) {
					        	String lowerRevealed = revealedPuzzle.toLowerCase();
					            String lowerGuess = guess.toLowerCase();
					            boolean foundWord = false;
					            int searchFrom = 0;
					            
					            
					            int colForStem = 10;  // Make sure this matches the constructPuzzle setting.
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
				
				        
					} else if (inputLine.matches("add\\s+\\S+")) {
						out.println("Adding word ...");
						
						 // Split the input line by whitespace to extract the command and the word
					    String[] parts = inputLine.split("\\s+");
					    
					    // Ensure that the command has a word following it
					    
					        String word = parts[1]; // Extract the word to add
					        int word_len = word.length(); // Calculate the length of the word
					        
					      addWord(word, word_len, out);
						
					} else if (inputLine.matches("remove\\s+\\S+")) {
					    // e.g. "remove apple"
					    // handle remove command
						out.println("Removing word ...");
						
						
						 // Split the input line by whitespace to extract the command and the word
					    String[] parts = inputLine.split("\\s+");
					    
					    // Ensure that the command has a word following it
					    
					        String word = parts[1]; // Extract the word to add
					        int word_len = word.length(); // Calculate the length of the word
					    
					        removeWord(word, word_len, out);
					    
					} else if (inputLine.matches("check\\s+score")) {
					    // e.g. "check score"
					    // handle check score command
						String response = displayScore(username);

		                if (response == null) {
		                    out.println("ERROR: No response from Account Server.");
		                    continue;
		                }

		                if (response.startsWith("ERROR")) {
		                    out.println(response);
		                } else {
		                    out.println("Your score: " + response);
		                }

		                out.println(); // Add an empty line for readability
		                continue; // Continue to the next command
		        
					    
					} else if (inputLine.matches("check\\s+\\S+")) {
					    // e.g. "check apple"
					    // handle check [word] command
						out.println("Checking word ...");
						
						
						 // Split the input line by whitespace to extract the command and the word
					    String[] parts = inputLine.split("\\s+");
					    
					    // Ensure that the command has a word following it
					    
					        String word = parts[1]; // Extract the word to add
					        int word_len = word.length(); // Calculate the length of the word
					        checkWord(word, word_len, out);
						
					} else if (inputLine.matches("exit")) {
						shutdownServer();
						out.println("Shutting down the server...");
		            	break;
					}
					else {
						 displayHelpMenu(out);
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
		
		
		/**
		 * Checks whether a given word exists in the word repository by communicating with the {@code Word_UDP_Game_2_Word} service.
		 *
		 * 
		 * This method sends a request to the word service to verify the existence of the specified word.
		 * Based on the response from the service, it informs the client whether the word is present
		 * in the repository.
		 * 
		 *
		 * @param word     The word to be checked in the word repository.
		 * @param word_len The length of the word being checked.
		 * @param out      The {@code PrintStream} used to send output messages to the client.
		 */
		 private void checkWord(String word, int word_len, PrintStream out) {
			 String [] wordServiceRequest = Request_UDP_Game_2_Word.send_request(3, word, word_len, 0);
				
				if (wordServiceRequest[0].equals("T")) {
					out.println("Word in word repository");
					out.println();
				} else {
					out.println("Word not in word repository");
					out.println();
				}
			
		}

		 /**
		  * Removes a specified word from the word repository by communicating with the {@code Word_UDP_Game_2_Word} service.
		  *
		  * 
		  * This method sends a request to the word service to delete the specified word from the repository.
		  * Based on the response from the service, it informs the client whether the removal was successful.
		  *
		  *
		  * @param word     The word to be removed from the word repository.
		  * @param word_len The length of the word being removed.
		  * @param out      The {@code PrintStream} used to send output messages to the client.
		  */ 
		private void removeWord(String word, int word_len, PrintStream out) {
			// TODO Auto-generated method stub
			 String [] wordServiceRequest = Request_UDP_Game_2_Word.send_request(2, word, word_len, 0);
				
				if (wordServiceRequest[0].equals("T")) {
					out.println("Word removed");
					out.println();
				} else {
					out.println("Word not removed");
					out.println();
				}
			
		}

		/**
		 * Adds a new word to the word repository by communicating with the {@code Word_UDP_Game_2_Word} service.
		 *
		 * <p>
		 * This method sends a request to the word service to insert the specified word into the repository.
		 * Based on the response from the service, it informs the client whether the addition was successful.
		 * </p>
		 *
		 * @param word     The word to be added to the word repository.
		 * @param word_len The length of the word being added.
		 * @param out      The {@code PrintStream} used to send output messages to the client.
		 */
		private void addWord(String word, int word_len, PrintStream out) {
			 String [] wordServiceRequest = Request_UDP_Game_2_Word.send_request(1, word, word_len, 0);
				
				if (wordServiceRequest[0].equals("T")) {
					out.println("Word added");
					out.println();
				} else {
					out.println("Word not added");
					out.println();
				}
			
		}

		/**
         * Displays the authentication menu with available commands to the client.
         *
         * @param out The PrintStream to send output to the client.
         */
		private void displayAuthenticationMenu(PrintStream out) {
			// TODO Auto-generated method stub
			out.println("---------Welcome to the Game Server---------");
			out.println("Please create an account or login to continue:");
		    out.println("Commands: CREATE [username] [password], LOGIN [username] [password]");
		    out.println();
		    
		}

		/**
		 * Processes the client's guess and updates the game state accordingly.
		 *
		 * @param guess          The client's guess.
		 * @param out            PrintStream to send output to client.
		 * @param formattedPuzzle The current state of the puzzle.
		 * @param revealedPuzzle The complete puzzle solution.
		 * @param failAttempts   The remaining fail attempts.
		 * @return Updated failAttempts after processing the guess.
		 */
		private String processSingleLetterGuess(char guessedLetter, boolean found, String formattedPuzzle, String revealedPuzzle, int failAttempts) {
			 // Loop over the solution grid (the "revealedPuzzle") to search for all instances.
            for (int x = 0; x < revealedPuzzle.length(); x++) {
               
                	
                    // If the cell contains the guessed letter and it hasn't already been revealed...
                    if (revealedPuzzle.charAt(x) == guessedLetter && formattedPuzzle.charAt(x) == '_') {
//                    	puzzle[i][j] = true;
                        found = true;
                        char [] formattedPuzzleChar = formattedPuzzle.toCharArray();
                        formattedPuzzleChar[x] = guessedLetter;
                        formattedPuzzle = String.valueOf(formattedPuzzleChar);
                        System.out.println(guessedLetter + " found");
                    }
             
                
            }
            if (found) {
                return "Letter " + guessedLetter + " Correct";
            } else {
            	 failAttempts = failAttempts - 1;
                return "Sorry, letter '" + guessedLetter + "' is not in the puzzle (or already revealed).";
               
            }
		}
		
		
		 /**
         * Decreases the user's score by 1 by communicating with the {@code AccountServer}.
         *
         * @param username The username of the client.
         * @return A message indicating the result of the operation.
         */
		private String decreaseScore(String username) {
			String updateScoreCommand = "UPDATE_SCORE " + username + " -1";
            String response;
            try {
                response = accountClient.sendCommand(updateScoreCommand);
            } catch (IOException e) {
            	System.err.println("Failed to update score for user " + username + ": " + e.getMessage());
                return "ERROR: Unable to update score.";
            }

            if (response != null && response.equalsIgnoreCase("Score updated successfully.")) {
                return "Your score has been decreased by 1.";
            } else if (response != null && response.startsWith("ERROR")) {
                return response;
            } else {
                return "Unexpected response from Account Server.";
            }
            
		}
		
		
	    /**
         * Displays the help menu with available commands to the client.
         *
         * @param out The PrintStream to send output to the client.
         */
        private void displayHelpMenu(PrintStream out) {
            out.print("Connected to the game server \n");
            out.print("---------CRISS CROSS WORD PUZZLE---------\n");
            out.print("start [level] [failed attempts factor] \n");
            out.print("add [word] \n");
            out.print("remove [word] \n");
            out.print("check [word] \n");
            out.print("check score \n");
            out.print("exit \n");
            out.println();
        }
		
   	 	/**
         * Increases the user's score by 1 by communicating with the {@code AccountServer}.
         *
         * @param username The username of the client.
         * @return A message indicating the result of the operation.
         */
		private String increaseScore(String username) {
			String updateScoreCommand = "UPDATE_SCORE " + username + " 1";
            String response;
            try {
                response = accountClient.sendCommand(updateScoreCommand);
            } catch (IOException e) {
            	 System.err.println("Failed to update score for user " + username + ": " + e.getMessage());
                return "ERROR: Unable to update score.";
               
               
            }

            if (response != null && response.equalsIgnoreCase("Score updated successfully.")) {
                return "Your score has been increased by 1."; 
            } else if (response != null && response.startsWith("ERROR")) {
               return response;
            } else {
                return "Unexpected response from Account Server.";
            }
		}
	
		/**
         * Displays the user's current score by communicating with the {@code AccountServer}.
         *
         * @param username The username of the client.
         * @return The user's score as a {@code String}.
         */
		private String displayScore(String username) {
			String accountCommand = "GET_SCORE " + username;
            String response = "";

            try {
                response = accountClient.sendCommand(accountCommand);
            } catch (IOException e) {
                System.out.println("ERROR: Unable to communicate with Account Server.");
                System.err.println("AccountServer communication error: " + e.getMessage());
            
            }
            
            return response;
        	
		}
	}

}