import com.example.b2c_core.*;

import java.util.*;

/**
 * Created by alex on 4/12/17.
 */
public class GameManager
{
    private DraftTileManager _tileManager;
    private IServer _server;
    private User[] _players;
    private HashMap<User, SharedCity> leftCities = new HashMap<>();
    private HashMap<User, SharedCity> rightCities = new HashMap<>();
    private GameSection _currentSection;
    private GameSection _lastSection;
    private HashMap<User, GameSection> _userCompletionTracker = new HashMap<>();
    private HashMap<User, List<BuildingType>> _lastDraftSet = new HashMap<>();
    private HashMap<User, BuildingType[]> _draftedTiles = new HashMap<>();
    private boolean draftDirectionIsLeft = true;
    private boolean hasDoneSpecialDraft = false;

    /**
     * This is the section of the game.
     * Modifications of a type will only be accepted during that section.
     */
    private enum GameSection
    {
        Draft7,
        Draft5,
        Draft3,
        SpecialDraft7,
        SpecialDraft5,
        Place
    }


    private GameManager(IServer server, User... players)
    {
        _server = server;
        _tileManager = new DraftTileManager();
        int numPlayers = players.length;
        _players = players;
        for (int playerNum = 0; playerNum < numPlayers; playerNum++)
        {
            User player1 = players[playerNum];
            // For the last shared city, its shared between the first player and the last player.
            User player2 = (playerNum + 1 < numPlayers) ? players[playerNum + 1] : players[0];
            SharedCity sharedCity = new SharedCity(player1, player2);
            leftCities.put(sharedCity.getRightPlayer(), sharedCity);
            rightCities.put(sharedCity.getLeftPlayer(), sharedCity);
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
        _currentSection = GameSection.Draft7;
        for (User u : _players)
        {
            BuildingType[] tiles = _tileManager.draft7();
            _lastDraftSet.put(u, new ArrayList<>(Arrays.asList(tiles)));
        }
        baseDraft();
    }

    private void baseDraft()
    {
        for (User u : _players)
        {
            SharedCity leftCity = leftCities.get(u);
            SharedCity rightCity = rightCities.get(u);
            BuildingType[] tiles = new BuildingType[_lastDraftSet.get(u).size()];
            tiles = _lastDraftSet.get(u).toArray(tiles);
            _server.startDraft(u, tiles, leftCity, rightCity);
        }
    }

    public void draftResult(User player, BuildingType[] tiles)
    {
        if (!validPlayer(player))
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
        List<BuildingType> availableTiles = _lastDraftSet.get(player);
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

        // If all players have drafted, move on to placing.
        if (gameSectionComplete())
        {
            startPlace();
        }
    }

    private void startPlace()
    {
        System.out.println("Start placing tiles.");
        _lastSection = _currentSection;
        _currentSection = GameSection.Place;

        for (User player : _players)
        {
            SharedCity left = leftCities.get(player);
            SharedCity right = rightCities.get(player);
            SharedCity other = null;
            for (SharedCity c : leftCities.values())
            {
                if (c != left && c != right)
                {
                    other = c;
                    break;
                }
            }
            _server.startPlace(player, _draftedTiles, left, right, other);
        }
    }

    public boolean placeTile(User player, BuildingType tile, SharedCity targetCity, int x, int y)
    {
        if (!validPlayer(player))
        {
            return false;
        }

        SharedCity placementCity = null;

        placementCity = leftCities.get(targetCity.getRightPlayer());
        // The result from leftCities & rightCities should be the same object.
        if (placementCity != rightCities.get(targetCity.getLeftPlayer()))
        {
            System.out.println("ERROR: Cities did not match.");
            return false;
        }

        boolean placeSuccess = placementCity.getCity().tryAddTile(tile, x, y);
        if (placeSuccess)
        {
            System.out.println("Placed Tile.");
            for (User p : _players)
            {
                _server.boardUpdate(p, placementCity);
            }
        }
        return placeSuccess;
    }

    public void placeComplete(User player)
    {
        if (!validPlayer(player))
        {
            return;
        }
        _userCompletionTracker.put(player, _currentSection);

        if (gameSectionComplete())
        {
            switch (_lastSection)
            {
                case Draft7:
                    startDraft5();
                    break;
                case Draft5:
                    startDraft3();
                    break;
                case Draft3:
                    // Start special level OR finish game.
                    if (!hasDoneSpecialDraft)
                    {
                        hasDoneSpecialDraft = true;
                        specialDraft7();
                    }
                    else
                    {
                        finishGame();
                    }
                    break;
                case SpecialDraft7:
                    specialDraft5();
                    break;
                case SpecialDraft5:
                    // Switch draft direction to right.
                    draftDirectionIsLeft = false;
                    startDraft7();
                    break;
                default:
                    System.out.println("ERROR: wrong last section.");
            }
        }
    }

    private void specialDraft7()
    {
        _currentSection = GameSection.SpecialDraft7;
        for (User u : _players)
        {
            BuildingType[] tiles = _tileManager.draft7();
            _lastDraftSet.put(u, new ArrayList<>(Arrays.asList(tiles)));
        }
        baseDraft();
    }

    private void specialDraft5()
    {
        _currentSection = GameSection.SpecialDraft5;
        // No shifting of tiles for this one.
        baseDraft();
    }

    private void finishGame()
    {
        System.out.println("Finish game!");
        for (SharedCity c : leftCities.values())
        {
            System.out.println(c.getCity());
            System.out.println();
        }
        FinishGame(_players);
    }

    private void startDraft5()
    {
        _currentSection = GameSection.Draft5;
        transferTiles(draftDirectionIsLeft);
        baseDraft();
    }

    private void startDraft3()
    {
        _currentSection = GameSection.Draft3;
        transferTiles(draftDirectionIsLeft);
        baseDraft();
    }

    private void transferTiles(boolean draftLeft)
    {
        HashMap<User, List<BuildingType>> draft5Tiles = new HashMap<>();
        for (User player : _players)
        {
            User reference = userFromDraftDirection(draftLeft, player);
            List<BuildingType> tiles = _lastDraftSet.get(reference);
            draft5Tiles.put(reference, tiles);
        }
        _lastDraftSet = draft5Tiles;
    }

    private User userFromDraftDirection(boolean isLeft, User currentPlayer)
    {
        User tilesFrom;
        if (isLeft)
        {
            tilesFrom = leftCities.get(currentPlayer).getLeftPlayer();
        }
        else
        {
            tilesFrom = rightCities.get(currentPlayer).getRightPlayer();
        }
        return tilesFrom;
    }

    private boolean validPlayer(User player)
    {
        // Verify player is in this game.
        boolean found = false;
        int i = -1;
        while (!found && (i++ < _players.length)) found = _players[i] == player;
        return found;
    }

    private boolean gameSectionComplete()
    {
        for (Map.Entry<User, GameSection> userCompletion : _userCompletionTracker.entrySet())
        {
            if (userCompletion.getValue() != _currentSection)
            {
                return false;
            }
        }
        return true;
    }


    @Override
    public String toString()
    {
        StringBuilder out = new StringBuilder("Players:\n");
        return out.toString();
    }

    private static final HashMap<User, GameManager> CURRENT_GAMES = new HashMap<>();

    public static GameManager StartNewGame(IServer server, User... users)
    {
        GameManager createdGame = new GameManager(server, users);
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

    public static void FinishGame(User... users)
    {
        for (User u : users)
        {
            CURRENT_GAMES.remove(u);
        }
    }
}
