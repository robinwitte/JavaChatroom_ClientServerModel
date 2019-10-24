package client;


import java.awt.event.*;
import javax.swing.*;

public class PrivateChatClosingListener implements WindowListener{
    
    ClientGUI clientGUI;
    
    public PrivateChatClosingListener(ClientGUI clientGUI){
        this.clientGUI = clientGUI;
    }
    
    @Override 
    public void windowClosing( WindowEvent event ){   

    }

  @Override public void windowClosed( WindowEvent event ) { /*Empty*/ }
  @Override public void windowDeiconified( WindowEvent event ) { /*Empty*/ }
  @Override public void windowIconified( WindowEvent event ) { /*Empty*/ }
  @Override public void windowActivated( WindowEvent event ) { /*Empty*/ }
  @Override public void windowDeactivated( WindowEvent event ) { /*Empty*/ }
  @Override public void windowOpened( WindowEvent event ) { /*Empty*/ }
}
