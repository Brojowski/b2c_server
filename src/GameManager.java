import com.example.b2c_core.*;

import java.util.HashMap;

/**
 * Created by alex on 4/12/17.
 */
public class GameManager
{
    private int _numPlayers;

    private User[] _players;
    private DraftTileManager _tileManager;
    /**
     * cities[0] shared by _players[0] && [1]
     * cities[1] shared by _players[1] && [2]
     * cities[2] shared by _players[0] && [2]
     */
    private City[] _cities;
    private BuildingType[][] _lastDraftSet;


    private GameManager(User... players)
    {
        _tileManager = new DraftTileManager();
        _players = players;
        _numPlayers = players.length;
        _lastDraftSet = new BuildingType[_numPlayers][];
        _cities = new City[_numPlayers];
        for (int i = 0; i < _numPlayers; i++)
        {
            _cities[i] = new City();
        }
        startDraft();
    }

    private void startDraft()
    {
        for (int i = 0; i < _numPlayers; i++)
        {
            _lastDraftSet[i] = _tileManager.draft7();
            DraftTransferObject dto = DraftTransferObject.create(_lastDraftSet[i], _cities);
            Server.emitToUser(_players[i], Routes.FromServer.BEGIN_DRAFT, dto);
        }
    }

    @Override
    public String toString()
    {
        StringBuilder out = new StringBuilder("Players:\n");
        for (User u : _players)
        {
            out.append(u.getUsername());
            out.append("\n");
        }
        return out.toString();
    }

    private static final HashMap<User, GameManager> CURRENT_GAMES = new HashMap<>();

    public static GameManager StartNewGame(User... users)
    {
        GameManager createdGame = new GameManager(users);
        for (User u : users)
        {
            CURRENT_GAMES.put(u, createdGame);
        }
        return createdGame;
    }

    public static GameManager GetUsersGame(User user)
    {
        return CURRENT_GAMES.get(user);
    }
}
