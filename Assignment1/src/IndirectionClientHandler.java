

import java.io.*;
import java.net.*;

/**
 * The IndirectionClientHandler class manages the communication between a single client and the GameServer.
 * It establishes a connection to the GameServer, then relays messages between the client and the GameServer.
 */
public class IndirectionClientHandler implements Runnable {
    private final Socket clientSocket;
    private Socket gameServerSocket;
    private final String gameServerHost;
    private final int gameServerPort;

    /**
     * Constructs an IndirectionClientHandler with the specified parameters.
     * 
     * @param clientSocket    The socket connected to the client.
     * @param gameServerHost  The hostname or IP address of the GameServer.
     * @param gameServerPort  The port on which the GameServer is listening.
     */
    public IndirectionClientHandler(Socket clientSocket, String gameServerHost, int gameServerPort) {
        this.clientSocket = clientSocket;
        this.gameServerHost = gameServerHost;
        this.gameServerPort = gameServerPort;
    }

    /**
     * Runs the client handler thread, managing the relay between the client and the GameServer.
     */
    @Override
    public void run() {
        try {
            // Establish connection to GameServer
            gameServerSocket = new Socket(gameServerHost, gameServerPort);
            System.out.println("Connected to GameServer for client: " + clientSocket.getRemoteSocketAddress());

            // Streams for client
            BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter clientWriter = new PrintWriter(clientSocket.getOutputStream(), true);

            // Streams for GameServer
            BufferedReader serverReader = new BufferedReader(new InputStreamReader(gameServerSocket.getInputStream()));
            PrintWriter serverWriter = new PrintWriter(gameServerSocket.getOutputStream(), true);

            // Start threads to handle bidirectional communication
            Thread clientToServer = new Thread(() -> relayMessages(clientReader, serverWriter, "Client to GameServer"));
            Thread serverToClient = new Thread(() -> relayMessages(serverReader, clientWriter, "GameServer to Client"));

            clientToServer.start();
            serverToClient.start();

            // Wait for both threads to finish
            clientToServer.join();
            serverToClient.join();

        } catch (IOException e) {
            System.err.println("I/O error with client " + clientSocket.getRemoteSocketAddress() + ": " + e.getMessage());
            // e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("Thread interrupted: " + e.getMessage());
            // e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            closeConnections();
        }
    }

    /**
     * Relays messages from a BufferedReader to a PrintWriter.
     * 
     * @param reader     The source BufferedReader.
     * @param writer     The destination PrintWriter.
     * @param direction  A string indicating the direction of relay for logging purposes.
     */
    private void relayMessages(BufferedReader reader, PrintWriter writer, String direction) {
        String message;
        try {
            while ((message = reader.readLine()) != null) {
                System.out.println(direction + ": " + message);
                writer.println(message);
            }
        } catch (IOException e) {
            System.err.println("Error relaying messages (" + direction + "): " + e.getMessage());
            // e.printStackTrace();
        }
    }

    /**
     * Closes all sockets and associated streams.
     */
    private void closeConnections() {
    
            if (gameServerSocket != null && !gameServerSocket.isClosed()) {
                try {
					gameServerSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                try {
					clientSocket.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }
            System.out.println("Closed connections for client: " + clientSocket.getRemoteSocketAddress());
       
    }
}