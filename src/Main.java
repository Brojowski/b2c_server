import com.corundumstudio.socketio.*;

import javax.swing.*;
import java.awt.*;

public class Main
{
    public static final String JOIN_GAME = "join";


    public static void main(String[] args)
    {
        Server.StartServer();

        final JFrame frame = new JFrame("_server");
        Button exitButton = new Button("Quit");
        JPanel panel = new JPanel();
        panel.add(exitButton);
        frame.setContentPane(panel);
        frame.setVisible(true);
        frame.setSize(50, 50);
        exitButton.addActionListener(actionEvent ->
        {
            System.out.println("Stopping server");
            Server.StopServer();
            frame.setVisible(false);
            System.exit(0);
        });
    }
}
