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
        int numCols = 10;
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
     *
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
     * @param puzzle The puzzle grid.
     * @return A string representing the formatted puzzle.
     */
    private static String formatPuzzle(char[][] puzzle) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < puzzle.length; i++) {
            for (int j = 0; j < puzzle[i].length; j++) {
                sb.append(puzzle[i][j]);
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
					
					int i;
					
					if (inputLine.equals("1")) {
						out.println("Level 1 selected");
						
						i=3;
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
				        
				        int letterCount = countPuzzleLetters(puzzle);
//				        int allowedAttempts = letterCount * f;
				        
				        String formattedPuzzle = formatPuzzle(puzzle);
				        
				        out.println(formattedPuzzle);
				        
						out.println(getRandomWordFromFile(2));
						out.println();
					} else if (inputLine.equals("2")) {
						out.println("Level 2 selected");
						out.println();
					} else if (inputLine.equals("3")) {
						out.println("Level 3 selected");
						out.println();
					} else if (inputLine.equals("4")) {
						out.println("Level 4 selected");
						out.println();
					} else if (inputLine.equals("5")) {
						out.println("Level 5 selected");
						out.println();
					} else {
						out.print("Connected to the game server \n");
						out.print("Select a game level to play (1-5): \n");
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
