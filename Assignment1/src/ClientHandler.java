import java.io.*;
import java.net.*;
import java.util.Map;

public class ClientHandler implements Runnable {
    private Socket socket;
    private Map<String, Account> accounts;

    public ClientHandler(Socket socket, Map<String, Account> accounts) {
        this.socket = socket;
        this.accounts = accounts;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
            String command;
            while ((command = in.readLine()) != null) {
                String response = handleCommand(command.trim());
                out.println(response);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String handleCommand(String command) {
        String[] parts = command.split("\\s+");
        if (parts.length < 1) {
            return "ERROR: Empty command.";
        }
        switch (parts[0].toUpperCase()) {
            case "CREATE":
                if (parts.length < 3)
                    return "ERROR: CREATE requires username and password.";
                return createAccount(parts[1], parts[2]);
            case "LOGIN":
                if (parts.length < 3)
                    return "ERROR: LOGIN requires username and password.";
                return login(parts[1], parts[2]);
            case "UPDATE_SCORE":
                if (parts.length < 3)
                    return "ERROR: UPDATE_SCORE requires username and score.";
                try {
                    return updateScore(parts[1], Integer.parseInt(parts[2]));
                } catch (NumberFormatException e) {
                    return "ERROR: Score must be a number.";
                }
            case "GET_SCORE":
                if (parts.length < 2)
                    return "ERROR: GET_SCORE requires username.";
                return getScore(parts[1]);
            default:
                return "ERROR: Unknown command.";
        }
    }

    private String createAccount(String username, String password) {
        if (accounts.containsKey(username)) {
            return "ERROR: Account already exists.";
        }
        Account account = new Account(username, password);
        accounts.put(username, account);
        // System.out.println("User Info: " + account.getUsername()
        // + " " + account.getPassword());
        return "Account created successfully.";
    }

    private String login(String username, String password) {
        Account account = accounts.get(username);
        if (account != null && account.getPassword().equals(password)) {
            return "Login successful.";
        }
        return "ERROR: Invalid username or password.";
    }

    private String updateScore(String username, int score) {
        Account account = accounts.get(username);
        if (account != null) {
            account.addScore(score);
            return "Score updated successfully.";
        }
        return "ERROR: Account not found.";
    }

    private String getScore(String username) {
        Account account = accounts.get(username);
        if (account != null) {
            if (account.getScores().isEmpty()) {
                return "No Historical Record";
            }
            return "Scores: " + account.getScores();
        }
        return "ERROR: Account not found.";
    }
}