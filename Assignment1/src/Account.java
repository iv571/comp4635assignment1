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