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

    private LinkedHashSet<User> _joinQueue;
    private int _waitingPlayers;

    public JoinGameEventHandler()
    {
        _joinQueue = new LinkedHashSet<>();
        _waitingPlayers = 0;
    }

    private void joinGame(User user)
    {
        if (_joinQueue.add(user))
        {
            _waitingPlayers++;
        }
        if (_waitingPlayers >= 3)
        {
            startGame();
        }
    }

    private void startGame()
    {
        User u1 = pop();
        User u2 = pop();
        User u3 = pop();
        GameManager game = GameManager.StartNewGame(u1, u2, u3);
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
        System.out.println("Event: join");
        joinGame(Server.getUser(socketIOClient));
    }
}
