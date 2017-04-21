import com.example.b2c_core.*;

import java.util.*;

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
    private Map<User, BuildingType[]> _sectionComplete;
    private boolean _draftInProgress;


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
        _sectionComplete = new HashMap<>();
        for (User u : players)
        {
            _sectionComplete.put(u, null);
        }
        startDraft();
    }

    private void startDraft()
    {
        _draftInProgress = true;
        for (int i = 0; i < _numPlayers; i++)
        {
            _lastDraftSet[i] = _tileManager.draft7();
            DraftTransferObject dto = DraftTransferObject.create(_lastDraftSet[i], _cities);
            Server.emitToUser(_players[i], Routes.FromServer.BEGIN_DRAFT, dto);
        }
    }

    public void finishDraft(User player, PostDraftTransferObject draftResults)
    {
        // Ignore if not drafting.
        if (!_draftInProgress)
        {
            return;
        }

        // Find the user index for references.
        int playerIndex = 0;
        while (_players[playerIndex] != player) playerIndex++;

        // Check that all tiles chosen were options.
        LinkedList<BuildingType> tiles = new LinkedList<>(Arrays.asList(_lastDraftSet[playerIndex]));
        for (BuildingType tile : draftResults.getSelectedTiles())
        {
            if (!tiles.remove(tile))
            {
                System.out.println("No tile found: " + tile);
            }
        }

        _sectionComplete.replace(_players[playerIndex], draftResults.getSelectedTiles());
        for (int i = 0; i < _numPlayers; i++)
        {
            if (_sectionComplete.get(_players[i]) == null)
            {
                return;
            }
        }

        // All the users have chosen their tiles.
        _draftInProgress = false;
        System.out.println("Place tiles");
        startPlacingTiles();
    }

    private void startPlacingTiles()
    {
        for (int i = 0; i < _numPlayers; i++)
        {
            BuildingType[] tilesToPlace = _sectionComplete.get(_players[i]);
            PlaceTransferObject placeTransfer = PlaceTransferObject.create(tilesToPlace, null, _cities, _cities);
            Server.emitToUser(_players[i], Routes.FromServer.BEGIN_PLACE, placeTransfer);
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
