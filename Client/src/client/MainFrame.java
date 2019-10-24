package client;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import javax.swing.event.*;
import javax.swing.text.DefaultCaret;


public class MainFrame extends JFrame{
    
    ClientGUI clientGUI;
    
    private JPanel contentPanel;
    
    //chatAreaPanel
    private JPanel chatAreaPanel, inputPanel;
    private JTextArea displayTextArea;
    private JScrollPane displayTextAreaScrollPane;
    private JButton sendButton;
    private JTextField inputTextField;
    
    //listsPanel
    private JPanel listsPanel, roomListPanel, userListPanel;
    private JLabel roomListLabel, userListLabel;
    private JScrollPane roomListScrollPane, userListScrollPane;
    private JList<String> roomList;
    private JTextArea userList;
    private JButton privateChatButton;
    private DefaultListModel<String> roomModel;
    
    
    
    public JTextArea getChatArea(){
        return displayTextArea;
    }
    
    public JTextField getInputTextField(){
        return inputTextField;
    }
    
    public JTextArea getUserList(){
        return userList;
    }
    
    public DefaultListModel<String> getRoomModel(){
        return roomModel;
    }
    
    public JList getRoomList(){
        return roomList;
    }
    
    
    
    MainFrame(ClientGUI clientGUI){
        this.clientGUI = clientGUI;
        initComponents();
    }
    
    private void initComponents(){
        
        // Frame Properties
        setTitle("Chatroom");
        setSize(800,600);
        setMinimumSize(new Dimension(500, 400));
        //setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener( new WindowClosingListener(clientGUI) );
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
            clientGUI.sendMsgToSrv(inputTextField.getText().trim());
            inputTextField.setText("");
        });
        KeyListener enterKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_ENTER)
                    sendButton.doClick();
            }
        };
        inputTextField.addKeyListener(enterKeyListener);
       
      
        // Panel for two lists on the right
        listsPanel = new JPanel();
        listsPanel.setLayout(new GridLayout(2,0,10,10));
        listsPanel.setPreferredSize(new Dimension(150, 40));
        
        // Room-List
        roomListPanel = new JPanel();
        roomListPanel.setLayout(new BorderLayout(10,10));
        roomListLabel = new JLabel("Rooms:");
        roomListPanel.add(roomListLabel, BorderLayout.PAGE_START);
        roomListScrollPane = new JScrollPane();
        roomModel = new DefaultListModel<>();
        roomList = new JList<>(roomModel);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        roomList.setLayoutOrientation(JList.VERTICAL);
        roomList.setVisibleRowCount(-1);
        roomListScrollPane.setViewportView(roomList);
        roomListPanel.add(roomListScrollPane, BorderLayout.CENTER);
        listsPanel.add(roomListPanel);
        
        // RoomChange at roomList selection     
        roomList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                Rectangle r = roomList.getCellBounds(0, roomList.getLastVisibleIndex());
                if (r != null && r.contains(evt.getPoint())) {
                    if(!clientGUI.getCurrentRoom().getName().equals(roomList.getSelectedValue()))
                        clientGUI.changeRoom(roomList.getSelectedValue(), "Regular");
                }
                inputTextField.requestFocus();
            }
        });
        
        
        // User-List
        userListPanel = new JPanel();
        userListPanel.setLayout(new BorderLayout(10,10));
        userListLabel = new JLabel("Users in this room:");
        userListPanel.add(userListLabel, BorderLayout.PAGE_START);
        privateChatButton = new JButton("private chat");
        privateChatButton.addActionListener((ActionEvent evt) -> {
            clientGUI.privateChatButtonAction(evt);
        });
        userListPanel.add(privateChatButton, BorderLayout.PAGE_END);
        userListScrollPane = new JScrollPane();
        userList = new JTextArea();
        userList.setEditable(false);
        Font font = userList.getFont();
        userList.setFont(font.deriveFont(Font.BOLD));
        userListScrollPane.setViewportView(userList);
        userListPanel.add(userListScrollPane, BorderLayout.CENTER);
        listsPanel.add(userListPanel);
        
        
        add(listsPanel, BorderLayout.LINE_END);
    }
}
