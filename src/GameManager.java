import com.example.b2c_core.User;

import java.util.HashMap;

/**
 * Created by alex on 4/12/17.
 */
public class GameManager
{
    private User player1, player2, player3;

    private GameManager(User p1, User p2, User p3)
    {
        player1 = p1;
        player2 = p2;
        player3 = p3;
    }

    @Override
    public String toString()
    {
        return "Players" +
                System.getProperty("line.separator") +
                player1 +
                System.getProperty("line.separator") +
                player2 +
                System.getProperty("line.separator") +
                player3;
    }

    private static final HashMap<User, GameManager> CURRENT_GAMES = new HashMap<>();

    public static GameManager StartNewGame(User u1, User u2, User u3)
    {
        GameManager createdGame = new GameManager(u1, u2, u3);
        CURRENT_GAMES.put(u1, createdGame);
        CURRENT_GAMES.put(u2, createdGame);
        CURRENT_GAMES.put(u3, createdGame);
        return createdGame;
    }

    public static GameManager GetUsersGame(User user)
    {
        return CURRENT_GAMES.get(user);
    }
}
