import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.example.b2c_core.Routes;
import com.example.b2c_core.User;

import java.util.HashMap;

/**
 * Created by alex on 4/12/17.
 */
public class Server
{
    private final HashMap<SocketIOClient, User> _users = new HashMap<>();
    private final HashMap<User, SocketIOClient> _clients = new HashMap<>();
    private static Server INSTANCE;
    private SocketIOServer _server;

    private Server()
    {
        Configuration config = new Configuration();
        config.setPort(8000);
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);
        config.setSocketConfig(socketConfig);
        _server = new SocketIOServer(config);

        _server.addConnectListener(new ConnectionListener());

        _server.addEventListener(Routes.ToServer.JOIN_GAME, String.class, new JoinGameEventHandler());

        _server.start();
    }

    public static void StartServer()
    {
        if (INSTANCE == null)
        {
            INSTANCE = new Server();
        }
    }

    public static void StopServer()
    {
        INSTANCE._server.stop();
    }


    public static User getUser(SocketIOClient client)
    {
        return INSTANCE._users.get(client);
    }

    public static void emitToUser(User user, String event, Object... objects)
    {
        INSTANCE._clients.get(user).sendEvent(event, objects);
    }

    private class ConnectionListener implements ConnectListener, DisconnectListener
    {

        @Override
        public void onConnect(SocketIOClient socketIOClient)
        {
            System.out.println("Connection " + socketIOClient.toString());
            User u = User.exampleUser();
            _users.put(socketIOClient, u);
            _clients.put(u, socketIOClient);
        }

        @Override
        public void onDisconnect(SocketIOClient socketIOClient)
        {
            System.out.println("Disconnect " + socketIOClient);
            User socketUser = _users.get(socketIOClient);
            // Using | instead of || because we don't want short circuiting
            if (_users.remove(socketIOClient) == null
                    | _clients.remove(socketUser) == null)
            {
                System.out.println("Error Removing");
            }
        }
    }
}
