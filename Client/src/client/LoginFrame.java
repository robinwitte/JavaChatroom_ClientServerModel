package client;

import javax.swing.*;
import java.awt.event.*;

public class LoginFrame extends JFrame{
    
    ClientGUI clientGUI;
    
    private JLabel usernameLabel, passwordLabel;
    private JTextField usernameTextField;
    private JPasswordField passwordTextField;
    private JButton loginButton, registerButton;
    private JPanel usernamePanel, passwordPanel, loginButtonPanel;
      
    LoginFrame(ClientGUI clientGUI){
        this.clientGUI = clientGUI;
        initComponents();
    }
    
    public JTextField getUsernameTextField(){
        return usernameTextField;
    }
    
    public JPasswordField getPasswordTextField(){
        return passwordTextField;
    }
    
    
    private void initComponents(){
        
        setTitle("Login or Register");
        setSize(400,150);
        //setAlwaysOnTop(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener( new WindowClosingListener(clientGUI) );
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        
        //create space at the top
        add(Box.createVerticalStrut(10));
        
        //create Panels for Labels plus TextFields and the buttons
        usernamePanel = new JPanel();
        usernameLabel = new JLabel("Username");
        usernameTextField = new JTextField(24);
        usernamePanel.add(usernameLabel);
        usernamePanel.add(usernameTextField);
        
        passwordPanel = new JPanel();
        passwordLabel = new JLabel("Password");
        passwordTextField =  new JPasswordField(24);
        passwordPanel.add(passwordLabel);
        passwordPanel.add(passwordTextField);
        usernameTextField.requestFocus();
        
        loginButtonPanel = new JPanel(); 
        loginButton = new JButton("Login");
        registerButton = new JButton("Register");
        loginButtonPanel.add(loginButton);
        loginButtonPanel.add(registerButton);
        
        //add Panels to the Frame
        add(usernamePanel);
        add(passwordPanel);
        add(loginButtonPanel);
        
        //add functionality to the buttons
        loginButton.addActionListener((ActionEvent evt) -> {
            clientGUI.loginFrameButtonAction(evt, "/login");
        });
        registerButton.addActionListener((ActionEvent evt) -> {
            clientGUI.loginFrameButtonAction(evt, "/register");
        });
        
        KeyListener enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER)
                    loginButton.doClick();
            }
        };
        usernameTextField.addKeyListener(enterKeyListener);
        passwordTextField.addKeyListener(enterKeyListener);
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
