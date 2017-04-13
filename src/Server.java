import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.eclipsesource.json.JsonArray;

import java.util.HashMap;

/**
 * Created by alex on 4/12/17.
 */
public class Server
{
    private final HashMap<User, SocketIOClient> _users = new HashMap<>();
    private static Server INSTANCE;
    SocketIOServer _server;

    private Server()
    {
        Configuration config = new Configuration();
        config.setPort(8000);
        SocketConfig socketConfig = new SocketConfig();
        socketConfig.setReuseAddress(true);
        config.setSocketConfig(socketConfig);
        _server = new SocketIOServer(config);

        _server.addConnectListener(new ConnectionListener());

        _server.addEventListener(Main.JOIN_GAME, String.class, new JoinGameEventHandler());

        _server.start();
    }

    public void stop()
    {
        _server.stop();
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
        if (INSTANCE != null)
        {
            INSTANCE.stop();
        }
    }

    private class ConnectionListener implements ConnectListener, DisconnectListener
    {

        @Override
        public void onConnect(SocketIOClient socketIOClient)
        {
            System.out.println("Connection " + socketIOClient.toString());
            _users.put(User.exampleUser(), socketIOClient);
        }

        @Override
        public void onDisconnect(SocketIOClient socketIOClient)
        {
            System.out.println("Disconnect " + socketIOClient);
            _users.remove(socketIOClient);
            if (_users.containsValue(socketIOClient))
            {
                System.out.println("Error Removing");
            }
        }
    }
}
