import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.example.b2c_core.User;

import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * A class to handle JoinGame requests.
 */
public class JoinGameEventHandler implements DataListener<String>
{
    private static final int NUM_PLAYERS = 3;
    private LinkedHashSet<User> _joinQueue;
    private int _waitingPlayers;
    private IServer _server;

    public JoinGameEventHandler(IServer server)
    {
        _joinQueue = new LinkedHashSet<>();
        _waitingPlayers = 0;
        _server = server;
    }

    private void joinGame(User user)
    {
        if (_joinQueue.add(user))
        {
            _waitingPlayers++;
            System.out.println("Event: join");
        }
        if (_waitingPlayers >= NUM_PLAYERS)
        {
            startGame();
        }
    }

    private void startGame()
    {
        User[] players = new User[NUM_PLAYERS];
        for (int i = 0; i < NUM_PLAYERS; i++)
        {
            players[i] = pop();
        }
        GameManager game = GameManager.StartNewGame(_server, players);
        System.out.println(game.toString());
    }

    private User pop()
    {
        Iterator<User> i = _joinQueue.iterator();
        User next = i.next();
        i.remove();
        _waitingPlayers--;
        return next;
    }


    @Override
    public void onData(SocketIOClient socketIOClient, String s, AckRequest ackRequest)
    {
        User user = Server.getUser(socketIOClient);
        if (GameManager.GetUsersGame(user) == null)
        {
            joinGame(user);
        }
    }
}
