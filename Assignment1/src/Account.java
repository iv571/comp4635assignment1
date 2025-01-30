/**
 * The {@code Account} class represents a user account with a username, password, 
 * and a history of scores. It implements {@code Serializable} to allow persistent 
 * storage of account data.
 * 
 * Each account has:
 * - A unique username.
 * - A password (which should be stored securely).
 * - A list of integer scores associated with the account.
 * 
 * The class provides methods to:
 * - Retrieve the username and password.
 * - Access the list of scores.
 * - Add new scores to the history.
 * 
 * Serialization ensures that account data can be saved and loaded from a file,
 * allowing for persistent storage across server restarts.
 * 
 * @author Khanh Le
 */

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Account implements Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private List<Integer> scores;

    public Account(String username, String password) {
        this.username = username;
        this.password = password;
        this.scores = new ArrayList<>();
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public List<Integer> getScores() {
        return scores;
    }

    public void addScore(int score) {
        this.scores.add(score);
    }
}