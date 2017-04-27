import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.example.b2c_core.User;

/**
 * Created by alex on 4/27/17.
 */
public class PlaceCompleteEventHandler implements DataListener<String>
{

    @Override
    public void onData(SocketIOClient socketIOClient, String s, AckRequest ackRequest) throws Exception
    {
        User player = Server.getUser(socketIOClient);
        GameManager gm = GameManager.GetUsersGame(player);
        gm.placeComplete(player);
    }
}
