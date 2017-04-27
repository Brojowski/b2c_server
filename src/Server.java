import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketConfig;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import com.example.b2c_core.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;

/**
 * Created by alex on 4/12/17.
 */
public class Server implements IServer
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

        _server.addEventListener(Routes.ToServer.JOIN_GAME, String.class, new JoinGameEventHandler(this));
        _server.addEventListener(Routes.ToServer.DRAFT_COMPLETE, String.class, new DraftCompleteListener());

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

    @Override
    public void startDraft(User player, BuildingType[] availableTiles, SharedCity leftCity, SharedCity rightCity, SharedCity... otherCities)
    {
        DraftTransferObject dto = new DraftTransferObject();
        dto.currentUser = player;
        dto.availableTiles = availableTiles;
        dto.leftCity = leftCity;
        dto.rightCity = rightCity;
        dto.otherCities = otherCities;
        _clients.get(player).sendEvent(Routes.FromServer.BEGIN_DRAFT, dto);
    }

    @Override
    public void startPlace(User player, HashMap<User, BuildingType[]> tileToPlace, SharedCity leftCity, SharedCity rightCity, SharedCity... otherCities)
    {
        PlaceTransferObject pto = new PlaceTransferObject();
        pto.currentUser = player;
        pto.tiles = tileToPlace;
        pto.leftCity = leftCity;
        pto.rightCity = rightCity;
        pto.otherCities = otherCities;
        _clients.get(player).sendEvent(Routes.FromServer.BEGIN_PLACE, pto);
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
