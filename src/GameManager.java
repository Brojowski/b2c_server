import com.example.b2c_core.*;

import java.util.*;

/**
 * Created by alex on 4/12/17.
 */
public class GameManager
{
    private int _numPlayers;
    private DraftTileManager _tileManager;
    private IServer _server;
    private User[] _players;
    private HashMap<User, SharedCity> leftCities = new HashMap<>();
    private HashMap<User, SharedCity> rightCities = new HashMap<>();
    private GameSection _currentSection;
    private HashMap<User, GameSection> _userCompletionTracker = new HashMap<>();
    private HashMap<User, BuildingType[]> _lastDraftSet = new HashMap<>();
    private HashMap<User, BuildingType[]> _draftedTiles = new HashMap<>();

    /**
     * This is the section of the game.
     * Modifications of a type will only be accepted during that section.
     */
    private enum GameSection
    {
        Draft,
        Place
    }


    private GameManager(IServer server, User... players)
    {
        _server = server;
        _tileManager = new DraftTileManager();
        _numPlayers = players.length;
        _players = players;
        for (int playerNum = 0; playerNum < _numPlayers; playerNum++)
        {
            User player1 = players[playerNum];
            // For the last shared city, its shared between the first player and the last player.
            User player2 = (playerNum + 1 < _numPlayers) ? players[playerNum + 1] : players[0];
            SharedCity sharedCity = new SharedCity(player1, player2);
            leftCities.put(player1, sharedCity);
            rightCities.put(player2, sharedCity);
        }
        initUserCompletionTracker();
        startDraft7();
    }

    private void initUserCompletionTracker()
    {
        for (User u : _players)
        {
            _userCompletionTracker.put(u, null);
        }
    }

    private void startDraft7()
    {
        _currentSection = GameSection.Draft;
        for (User u : _players)
        {
            BuildingType[] tiles = _tileManager.draft7();
            _lastDraftSet.put(u, tiles);
            SharedCity leftCity = leftCities.get(u);
            SharedCity rightCity = rightCities.get(u);
            _server.startDraft(u, tiles, leftCity, rightCity);
        }
    }

    public void draftResult(User player, BuildingType[] tiles)
    {
        // Verify player is in this game.
        boolean found = false;
        int i = -1;
        while (!found && (i++ < _players.length)) found = _players[i] == player;
        if (!found)
        {
            return;
        }

        // Verify user only chose two tiles.
        if (tiles.length != 2)
        {
            System.out.println("To many tiles chosen");
            return;
        }

        // Verify both tiles were available to the player.
        ArrayList<BuildingType> availableTiles = new ArrayList<>(Arrays.asList(_lastDraftSet.get(player)));
        if (!availableTiles.remove(tiles[0]))
        {
            System.out.println("Invalid tile chosen.");
            return;
        }
        if (!availableTiles.remove(tiles[1]))
        {
            System.out.println("Invalid tile chosen.");
            return;
        }
        _draftedTiles.put(player, tiles);
        _userCompletionTracker.put(player, _currentSection);

        // Check if should move on to placing tiles.
        for (Map.Entry<User, GameSection> userCompletion : _userCompletionTracker.entrySet())
        {
            if (userCompletion.getValue() != _currentSection)
            {
                return;
            }
        }
        startPlace();
    }

    private void startPlace()
    {
        System.out.println("Start placing tiles.");
    }


    @Override
    public String toString()
    {
        StringBuilder out = new StringBuilder("Players:\n");
        return out.toString();
    }

    private static final HashMap<User, GameManager> CURRENT_GAMES = new HashMap<>();

    public static GameManager StartNewGame(User... users)
    {
        GameManager createdGame = new GameManager(null, users);
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
