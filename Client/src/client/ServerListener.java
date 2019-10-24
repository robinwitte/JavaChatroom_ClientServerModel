package client;

import static com.sun.org.apache.bcel.internal.Repository.instanceOf;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import server.Room;

public class ServerListener extends Thread{
    
    private final ClientGUI clientGUI;
    private ObjectInputStream in;
    
    public ServerListener(ClientGUI clientGUI){
        this.clientGUI = clientGUI;
    }
    
    public Object readObject(){
        Object obj = null;
        try {
            obj = in.readObject();
        } catch (IOException | ClassNotFoundException ex) {
            Logger.getLogger(ServerListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        return obj;
    }
    
    public String loginOrRegister(){
        String line;
        Object obj;
        OUTER:
        while (true) {
            obj = readObject();
            if(obj instanceof String){
                line = (String)obj;
                switch (line) {
                    case "/quit":
                        break OUTER;
                    case "/success":
                        clientGUI.switchToMainFrame();
                        break OUTER;
                    default:
                        clientGUI.getLoginFrame().getUsernameTextField().setText("");
                        clientGUI.getLoginFrame().getPasswordTextField().setText("");
                        JOptionPane.showMessageDialog(null, line);
                        clientGUI.getLoginFrame().getUsernameTextField().requestFocus();
                        break;
                }
            }
            else
                clientGUI.refreshRoomsAndClients((ArrayList<Room>)obj);
        }
        return line;
    }

    
    @Override
    public void run() {
        
        try {
            in = new ObjectInputStream(clientGUI.getClient().getClSocket().getInputStream());
        } catch(IOException e) {
            System.err.println(e);
        }
        
        Object obj;        
        
        String line = loginOrRegister();
        
        OUTER:
        while (true && !line.equals("/quit")) {
            obj = readObject();
            if (obj instanceof String) {
                line = (String)obj;
                switch (line) {
                    case "/quit":
                        break OUTER;
                    case "/quitFromServer":
                        clientGUI.sendMsgToSrv("/quit");
                        JOptionPane.showMessageDialog(null, "Server closed the Chatroom.");
                        break;
                    case "/ban":
                        clientGUI.sendMsgToSrv("/quit");
                        JOptionPane.showMessageDialog(null, "You were banned.");
                        break;
                    case "/roomClosed":
                        clientGUI.changeRoom("<default>", "Close");
                        break;
                    case "/roomRenamed":
                        obj = readObject();
                        clientGUI.refreshRoomsAndClients((ArrayList<Room>)obj);
                        obj = readObject();
                        line = (String)obj;
                        clientGUI.updateRenamedRoom(line);
                        break;
                    default:
                        if(line.startsWith("#"))
                            clientGUI.appendPrvMessage(line);
                        else
                            clientGUI.getFrame().getChatArea().append(line + "\n");
                        break;
                }
            }else{
                clientGUI.refreshRoomsAndClients((ArrayList<Room>)obj);
            }
        }
        
        try {
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
