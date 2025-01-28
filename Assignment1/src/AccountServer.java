import java.io.*;
import java.net.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class AccountServer {
    private static final String USAGE = "Usage: java AccountServer [port]";
    private ServerSocket serverSocket;
    private Map<String, Account> accounts = new ConcurrentHashMap<>();
    private static final String DATA_FILE = "accounts.ser";

    public AccountServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        loadAccounts(); // Load account data on startup
    }

    public static void main(String[] args) {
        try {
            int port = (args.length > 0) ? Integer.parseInt(args[0]) : 5700;
            AccountServer server = new AccountServer(port);
            Runtime.getRuntime().addShutdownHook(new Thread(server::stopServer)); // Graceful shutdown hook
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
        saveAccounts(); // Save account data on shutdown
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
                System.out.println("Server shut down gracefully.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Save account data to a file
    private void saveAccounts() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(accounts);
            System.out.println("Accounts saved to file.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Load account data from a file
    @SuppressWarnings("unchecked")
    private void loadAccounts() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            accounts = (Map<String, Account>) ois.readObject();
            System.out.println("Accounts loaded from file.");
        } catch (FileNotFoundException e) {
            System.out.println("No previous account data found. Starting fresh.");
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

}