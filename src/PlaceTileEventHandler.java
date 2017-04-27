import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.example.b2c_core.PlaceTileTransferObject;
import com.example.b2c_core.Routes;
import com.example.b2c_core.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.internal.runtime.ECMAException;

/**
 * Created by alex on 4/27/17.
 */
public class PlaceTileEventHandler implements DataListener<String>
{
    @Override
    public void onData(SocketIOClient socketIOClient, String s, AckRequest ackRequest) throws Exception
    {
        try
        {
            //System.out.println(Routes.ToServer.PLAY_TILE + " : " + s);
            ObjectMapper mapper = new ObjectMapper();
            PlaceTileTransferObject ptto = mapper.readValue(s, PlaceTileTransferObject.class);

            User player = Server.getUser(socketIOClient);
            // Its suspicious if requests are made on another
            // players behalf. Ignore it.
            if (player.equals(ptto.currentUser))
            {
                GameManager playersGame = GameManager.GetUsersGame(player);
                if (playersGame.placeTile(player, ptto.tileToPlace, ptto.targetCity, ptto.x, ptto.y))
                {
                    ackRequest.sendAckData("OK");
                    return;
                }
                ackRequest.sendAckData(s);
            }
            else
            {
                ackRequest.sendAckData(s);
                throw new Exception("ERROR: Users did not match.");
            }
        } catch (Exception e)
        {
            // Log because otherwise it never shows up.
            e.printStackTrace();
            // Rethrow for system to handle.
            throw e;
        }
    }
}
