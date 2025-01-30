/**
 * The AccountServer class is a multi-threaded server that manages user accounts 
 * over a network using sockets. It listens for client connections, handles 
 * account creation, authentication, and score updates, and processes requests 
 * concurrently.
 *
 * Features:
 * - Listens on a specified port (default: 5700).
 * - Manages accounts using a concurrent hash map.
 * - Handles multiple clients via threads.
 * - Supports graceful shutdown.
 *
 * Usage:
 *   java AccountServer [port]
 *
 * Dependencies:
 * - Requires Account and ClientHandler classes.
 * @author Khanh Le
 */

import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class AccountServer {
    private static final String USAGE = "Usage: java AccountServer [port]";
    private ServerSocket serverSocket;
    private Map<String, Account> accounts = new ConcurrentHashMap<>();

    public AccountServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public static void main(String[] args) {
        try {
            int port = (args.length > 0) ? Integer.parseInt(args[0]) : 5700;
            AccountServer server = new AccountServer(port);
            Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stopServer()));
            server.startServer();
        } catch (NumberFormatException e) {
            System.err.println(USAGE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void startServer() {
        try {
            System.out.println("Account Server is running...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                new Thread(new ClientHandler(clientSocket, accounts)).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server shut down gracefully.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}