import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.listener.DataListener;
import com.example.b2c_core.PostDraftTransferObject;
import com.example.b2c_core.User;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DraftCompleteListener implements DataListener<String>
{
    @Override
    public void onData(SocketIOClient socketIOClient, String s, AckRequest ackRequest) throws Exception
    {
        System.out.println(s);
        ObjectMapper m = new ObjectMapper();
        PostDraftTransferObject pdto = m.readValue(s,PostDraftTransferObject.class);
        System.out.println(pdto);
        System.out.println(s);

        User u = Server.getUser(socketIOClient);

        GameManager game = GameManager.GetUsersGame(u);

        game.draftResult(u,pdto.getSelectedTiles());
    }
}
