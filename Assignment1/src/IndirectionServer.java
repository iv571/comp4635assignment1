import java.io.*;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The IndirectionServer class acts as a proxy between multiple clients and the GameServer.
 * It listens for incoming client connections, establishes connections to the GameServer,
 * and relays messages between clients and the GameServer.
 * 
 * Usage: java IndirectionServer [listen_port] [game_server_host] [game_server_port]
 * 
 * Example: java IndirectionServer 6000 localhost 5599
 * 
 * Author: [Your Name]
 */
public class IndirectionServer {
    private static final int THREAD_POOL_SIZE = 50; // Adjust based on expected load
    private final int listenPort;
    private final String gameServerHost;
    private final int gameServerPort;
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    /**
     * Constructs an IndirectionServer with the specified parameters.
     * 
     * @param listenPort     The port on which the server listens for client connections.
     * @param gameServerHost The hostname or IP address of the GameServer.
     * @param gameServerPort The port on which the GameServer is listening.
     */
    public IndirectionServer(int listenPort, String gameServerHost, int gameServerPort) {
        this.listenPort = listenPort;
        this.gameServerHost = gameServerHost;
        this.gameServerPort = gameServerPort;
    }

    /**
     * Starts the Indirection Server, initializes the thread pool, and begins listening for clients.
     */
    public void start() {
        try {
            serverSocket = new ServerSocket(listenPort);
            System.out.println("Indirection Server started on port " + listenPort);
            System.out.println("Connecting to GameServer at " + gameServerHost + ":" + gameServerPort);

            // Initialize thread pool
            executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

            // Continuously accept client connections
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Accepted connection from client: " + clientSocket.getRemoteSocketAddress());

                // Handle each client connection in a separate thread
                executorService.execute(new IndirectionClientHandler(clientSocket, gameServerHost, gameServerPort));
            }
        } catch (IOException e) {
            System.err.println("Error starting Indirection Server: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    /**
     * Shuts down the server and releases all resources.
     */
    public void shutdown() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            if (executorService != null && !executorService.isShutdown()) {
                executorService.shutdown();
            }
            System.out.println("Indirection Server has been shut down.");
        } catch (IOException e) {
            System.err.println("Error shutting down Indirection Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * The entry point of the Indirection Server application.
     * 
     * @param args Command-line arguments. Expects three arguments: listen_port, game_server_host, game_server_port.
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java IndirectionServer [listen_port] [game_server_host] [game_server_port]");
            System.out.println("Example: java IndirectionServer 6000 localhost 5599");
            System.exit(1);
        }

        int listenPort = Integer.parseInt(args[0]);
        String gameServerHost = args[1];
        int gameServerPort = Integer.parseInt(args[2]);

        IndirectionServer server = new IndirectionServer(listenPort, gameServerHost, gameServerPort);
        server.start();
    }
}