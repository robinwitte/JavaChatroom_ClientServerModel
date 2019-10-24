package server;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.DefaultCaret;


public class MainFrame extends JFrame{
    
    private final Server server;
    
    private JPanel contentPanel;
    
    //logTextArea
    private JTextArea logTextArea;
    private JScrollPane logTextAreaScrollPane;
    
    //listsPanel
    private JPanel listsPanel, userListPanel, roomListPanel, roomButtonsPanel, userActionPanel, userBottomPanel;
    private JLabel roomListLabel, userListLabel;
    private JScrollPane roomListScrollPane, userListScrollPane;
    private JList<String> roomList, userList;
    private JButton createRoomButton, deleteRoomButton, renameRoomButton, userButton, showUserDataButton;
    private JComboBox userComboBox;
    private DefaultListModel<String> roomModel, userModel;
    
    MainFrame(Server server){
        this.server = server;
        initComponents();
    }
    
    public DefaultListModel<String> getRoomModel(){
        return roomModel;
    }
    public DefaultListModel<String> getUserModel(){
        return userModel;
    }
    public JTextArea getLogTextArea(){
        return logTextArea;
    }
    
    private void initComponents(){
        
        // Frame Properties
        setTitle("Chatroom");
        setSize(1000,800);
        setMinimumSize(new Dimension(800, 800));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener( new ServerWindowClosingListener(server) );
        setLocationRelativeTo(null);
        
        // Content Panel (set Border)
        contentPanel = new JPanel(new BorderLayout(10,10));
	contentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        setContentPane(contentPanel);
        
        // Panel for two lists on the right
        listsPanel = new JPanel();
        listsPanel.setLayout(new GridLayout(2,0,10,10));
        listsPanel.setPreferredSize(new Dimension(340, 40));
        
        
        roomModel = new DefaultListModel<>();
        server.getRooms().forEach((room) -> {
            roomModel.addElement(room.getName());
        });

        
        // Room-List-Panel
        roomListPanel = new JPanel();
        roomListPanel.setLayout(new BorderLayout(10,10));
        roomListLabel = new JLabel("Rooms:");
        roomListPanel.add(roomListLabel, BorderLayout.PAGE_START);
        roomListScrollPane = new JScrollPane();
        roomList = new JList<>(roomModel);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        roomList.setLayoutOrientation(JList.VERTICAL);
        roomList.setVisibleRowCount(-1);
        roomListScrollPane.setViewportView(roomList);
        roomListPanel.add(roomListScrollPane, BorderLayout.CENTER);
        
        // Room-Buttons
        createRoomButton = new JButton("<html><center>CREATE<br /><small>new room</small></center></html>");
        deleteRoomButton = new JButton("<html><center>DELETE<br /><small>selected room</small></center></html>");
        renameRoomButton = new JButton("<html><center>RENAME<br /><small>selected room</small></center></html>");
        roomButtonsPanel = new JPanel();
        roomButtonsPanel.setLayout(new GridLayout(0,3,10,10));
        roomButtonsPanel.add(createRoomButton);
        roomButtonsPanel.add(deleteRoomButton);
        roomButtonsPanel.add(renameRoomButton);
        
        //add functionality to room buttons
        createRoomButton.addActionListener((ActionEvent evt) -> {
            String s = JOptionPane.showInputDialog( "Bitte geben Sie den gewünschten Namen ein:" );
            if(s != null)
                if(!server.createRoom(s))
                    JOptionPane.showMessageDialog(null, "A room with this name exists already.");
        });
        deleteRoomButton.addActionListener((ActionEvent evt) -> {
            String s = roomList.getSelectedValue();
            if(null == s)
                JOptionPane.showMessageDialog(null, "Please select a romm from the list.");
            else switch (s) {
                case "<default>":
                    JOptionPane.showMessageDialog(null, "<default> room can't be deleted.");
                    break;
                default:
                    server.deleteRoom(s);
                    break;
            }
        });
        renameRoomButton.addActionListener((ActionEvent evt) -> {
            String oldName = roomList.getSelectedValue();
            if(null == oldName)
                JOptionPane.showMessageDialog(null, "Please select a romm from the list.");
            else switch (oldName) {
                case "<default>":
                    JOptionPane.showMessageDialog(null, "<default> room can't be renamed.");
                    break;
                default:
                    String newName = JOptionPane.showInputDialog( "Bitte geben Sie den neuen Namen ein:" );
                    if(newName != null)
                        if(!server.renameRoom(oldName, newName))
                            JOptionPane.showMessageDialog(null, "A room with this name exists already.");
                    break;
            }
        });
        
        roomListPanel.add(roomButtonsPanel, BorderLayout.PAGE_END);
        listsPanel.add(roomListPanel);
        
        
        // User-List
        userListPanel = new JPanel();
        userListPanel.setLayout(new BorderLayout(10,10));
        userListLabel = new JLabel("Users:");
        userListPanel.add(userListLabel, BorderLayout.PAGE_START);
        userListScrollPane = new JScrollPane();
        userModel = new DefaultListModel<>();
        userList = new JList<>(userModel);
        roomList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        roomList.setLayoutOrientation(JList.VERTICAL);
        roomList.setVisibleRowCount(-1);
        userListScrollPane.setViewportView(userList);
        userListPanel.add(userListScrollPane, BorderLayout.CENTER);
        listsPanel.add(userListPanel);
        
                
        // User-Action-ComboBox and Button
        userActionPanel = new JPanel();
        userActionPanel.setLayout(new GridLayout(0,2,10,10));
        String[] actions = {"warn user", "ban user", "ban user permanently"};
        userComboBox = new JComboBox(actions);
        userButton = new JButton("GO");
        
        userActionPanel.add(userComboBox);
        userActionPanel.add(userButton);
        
        // User-show-Button
        showUserDataButton = new JButton("show whole user data");
        showUserDataButton.addActionListener((ActionEvent evt) -> {
            server.showUserData();
        });
        
        // user-bottom-Panel
        userBottomPanel = new JPanel();
        userBottomPanel.setLayout(new GridLayout(2,0,10,10));
        userBottomPanel.add(userActionPanel);
        userBottomPanel.add(showUserDataButton);
        userListPanel.add(userBottomPanel, BorderLayout.PAGE_END);
        
        // functionality for user Button
        userButton.addActionListener((ActionEvent evt) -> {
            String s = userList.getSelectedValue();
            if(s != null){
                String[] parts = s.split("\\s+");
                s = parts[1];
                String choose = (String)userComboBox.getSelectedItem();
                switch (choose) {
                    case "warn user":
                        String message = JOptionPane.showInputDialog( "Bitte geben Sie die Nachricht ein:" );
                        if(message != null)
                            server.warnUser(s, message);
                        break;
                    case "ban user":
                        server.banUser(s);
                        break;
                    case "ban user permanently":
                        server.banUserPermanently(s);
                        break;
                }
            }
            else
                JOptionPane.showMessageDialog(null, "Please select a user from the list.");
        });
        
        add(listsPanel, BorderLayout.LINE_END);
        
        //log-Text-Area
        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextAreaScrollPane = new JScrollPane();
        logTextAreaScrollPane.setViewportView(logTextArea);
        DefaultCaret caret = (DefaultCaret)logTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        add(logTextAreaScrollPane, BorderLayout.CENTER);
        
    }
    
}
