package client;


import java.awt.event.*;
import javax.swing.*;

public class WindowClosingListener implements WindowListener{
    
    ClientGUI clientGUI;
    
    public WindowClosingListener(ClientGUI clientGUI){
        this.clientGUI = clientGUI;
    }
    
    @Override 
    public void windowClosing( WindowEvent event ){   
        int option = JOptionPane.showConfirmDialog( null, "Wirklich beenden?" );
        if ( option == JOptionPane.OK_OPTION ){
            clientGUI.sendMsgToSrv("/quit");
        }
    }

  @Override public void windowClosed( WindowEvent event ) { /*Empty*/ }
  @Override public void windowDeiconified( WindowEvent event ) { /*Empty*/ }
  @Override public void windowIconified( WindowEvent event ) { /*Empty*/ }
  @Override public void windowActivated( WindowEvent event ) { /*Empty*/ }
  @Override public void windowDeactivated( WindowEvent event ) { /*Empty*/ }
  @Override public void windowOpened( WindowEvent event ) { /*Empty*/ }
}
