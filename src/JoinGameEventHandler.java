import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A class to handle JoinGame requests.
 */
public class JoinGameEventHandler implements DataListener<String>
{
    private class GameManager
    {
        private Queue<User> _waitingUsers;

        private GameManager _instance;
        private GameManager()
        {
            _waitingUsers = new LinkedList<>();
        }

        private void join(User user)
        {
            _waitingUsers.add(user);
        }

        private void joinGame(User user)
        {
            if (_instance == null)
            {
                _instance = new GameManager();
            }
            _instance.join(user);
        }

    }


    @Override
    public void onData(SocketIOClient socketIOClient, String s, AckRequest ackRequest)
    {

    }
}
