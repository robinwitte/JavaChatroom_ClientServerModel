package client;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;


public class PrivateChatFrame extends JFrame{
    
    private ClientGUI clientGUI;
    
    private String nameOfOtherClient;
    
    private JPanel contentPanel;
    
    //chatAreaPanel
    private JPanel chatAreaPanel, inputPanel;
    private JTextArea displayTextArea;
    private JScrollPane displayTextAreaScrollPane;
    private JButton sendButton;
    private JTextField inputTextField;
    
    PrivateChatFrame(ClientGUI clientGUI, String nameOfOtherClient){
        this.clientGUI = clientGUI;
        this.nameOfOtherClient = nameOfOtherClient;
        initComponents();
    }
    
    public JTextArea getChatArea(){
        return displayTextArea;
    }
    
    public String getNameOfOtherClient(){
        return nameOfOtherClient;
    }
    
    private void initComponents(){
        
        // Frame Properties
        setTitle("private chat with " + nameOfOtherClient);
        setSize(400,300);
        setMinimumSize(new Dimension(200, 100));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener( new PrivateChatClosingListener(clientGUI));
        setLocationRelativeTo(null);
        
        // Content Panel (set Border)
        contentPanel = new JPanel(new BorderLayout(10,10));
	contentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setContentPane(contentPanel);
        
        // Input-Textfield, Send-Button and Display-Text-Area
        inputTextField = new JTextField(50);
        sendButton = new JButton("send");
        displayTextArea = new JTextArea();
        displayTextArea.setEditable(false);
        displayTextAreaScrollPane = new JScrollPane();
        displayTextAreaScrollPane.setViewportView(displayTextArea);
        DefaultCaret caret = (DefaultCaret)displayTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        inputPanel = new JPanel();
        inputPanel.setLayout(new BorderLayout(10,10));
        inputPanel.add(inputTextField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.LINE_END);
        
        // Panel for whole Chat-Area
        chatAreaPanel = new JPanel();
        chatAreaPanel.setLayout(new BorderLayout(10,10)); 
        chatAreaPanel.add(displayTextAreaScrollPane, BorderLayout.CENTER);
        chatAreaPanel.add(inputPanel, BorderLayout.PAGE_END);
        
        add(chatAreaPanel, BorderLayout.CENTER);
        
        // Send-Button-Action und Enter-KeyListener
        sendButton.addActionListener((ActionEvent evt) -> {
            String line = "#" + nameOfOtherClient + "#" + inputTextField.getText().trim();
            clientGUI.sendPrvMsgToSrv(line, nameOfOtherClient);
            inputTextField.setText("");
        });
        KeyListener enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER)
                    sendButton.doClick();
            }
        };
        addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                String line = "#" + nameOfOtherClient + "#/prvquit";
                clientGUI.sendPrvMsgToSrv(line , nameOfOtherClient);
            }
        });
        inputTextField.addKeyListener(enterKeyListener);
        
        setVisible(true);
        inputTextField.requestFocus();
    }
}
