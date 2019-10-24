package server;


import java.awt.event.*;
import java.io.IOException;
import java.util.logging.*;
import javax.swing.*;

public class ServerWindowClosingListener implements WindowListener{
    
    Server server;
    
    public ServerWindowClosingListener(Server server){
        this.server = server;
    }
    
    @Override 
    public void windowClosing( WindowEvent event ){   
        int option = JOptionPane.showConfirmDialog( null, "Wirklich beenden?" );
        if ( option == JOptionPane.OK_OPTION ){
            server.getThreads().forEach(thread->{
                thread.sendMessageToMe("/quitFromServer");
            });
            server.logAction("Server socket closed.");
            try {
                server.getLogWriter().close();
            } catch (IOException ex) {
                Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.exit(0);
        }
  }

  @Override public void windowClosed( WindowEvent event ) { /*Empty*/ }
  @Override public void windowDeiconified( WindowEvent event ) { /*Empty*/ }
  @Override public void windowIconified( WindowEvent event ) { /*Empty*/ }
  @Override public void windowActivated( WindowEvent event ) { /*Empty*/ }
  @Override public void windowDeactivated( WindowEvent event ) { /*Empty*/ }
  @Override public void windowOpened( WindowEvent event ) { /*Empty*/ }
}
