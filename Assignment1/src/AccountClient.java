// AccountClient.java
import java.io.*;
import java.net.*;

public class AccountClient {
    private String host;
    private int port;

    /**
     * Constructs an AccountClient to communicate with the AccountServer.
     *
     * @param host The hostname or IP address of the AccountServer.
     * @param port The port number on which the AccountServer is listening.
     */
    public AccountClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Sends a command to the AccountServer and retrieves the response.
     *
     * @param command The command string (e.g., "CREATE username password").
     * @return The response from the AccountServer.
     * @throws IOException If an I/O error occurs during communication.
     */
    public String sendCommand(String command) throws IOException {
        try (Socket socket = new Socket(host, port);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            out.println(command);
            return in.readLine(); // Assuming the response is a single line
        }
    }
}