package client;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import server.ClientThread;
import server.Room;



public class ClientGUI extends Thread{
    
    private final Client client;
    private LoginFrame loginFrame;
    private MainFrame frame;
    private PrintWriter out;
    //private ObjectInputStream inObj;
    
    private ArrayList<Room> rooms = new ArrayList<>();
    private ArrayList<PrivateChatFrame> privateChats = new ArrayList<>();
    private Room currentRoom = null;
    private String username;

    
    public ClientGUI(Client client){
        this.client = client;
    }
    
    public Client getClient(){
        return client;
    }
    
    public MainFrame getFrame(){
        return frame;
    }
    
    public LoginFrame getLoginFrame(){
        return loginFrame;
    }
    
    public ArrayList<Room> getRooms(){
        return rooms;
    }
    
    public Room getCurrentRoom(){
        return currentRoom;
    }
    
    public void setCurrentRoom(Room currentRoom){
        this.currentRoom = currentRoom;
    }
    
    
    public void sendMsgToSrv(String message) {
        out.println(message);
        if(message.equals("/quit")){
            loginFrame.dispose();
            frame.dispose();
            privateChats.forEach(privateChat->{
                privateChat.dispose();
            });
        }
    }
    
    public void sendPrvMsgToSrv(String line, String to){
        out.println(line);
        Scanner scanner = new Scanner(line).useDelimiter("#");
        String otherClient = scanner.next();
        String message = scanner.next();
        if(message.equals("/prvquit")){
            privateChats.forEach(privateChat->{
                if(to.equals(privateChat.getNameOfOtherClient())){
                    privateChat.dispose();
                }
            });
            privateChats.removeIf(privateChat -> (to.equals(privateChat.getNameOfOtherClient())));
        }
    }
    
    public void appendPrvMessage(String line){
        Scanner scanner = new Scanner(line).useDelimiter("#");
        String from = scanner.next();
        String message = scanner.next();
        if(message.equals(from + ": /prvquit")){
            privateChats.forEach(privateChat->{
                if(from.equals(privateChat.getNameOfOtherClient())){
                    privateChat.dispose();
                }
            });
            privateChats.removeIf(privateChat -> (from.equals(privateChat.getNameOfOtherClient())));
        }
        else{
            boolean exist = false;
            for(int i = 0; i < privateChats.size(); ++i) {
                if(from.equals(privateChats.get(i).getNameOfOtherClient())){
                    privateChats.get(i).getChatArea().append(message + "\n");
                    exist = true;
                }
            }
            if(!exist){
                privateChats.add(new PrivateChatFrame(this, from));
                appendPrvMessage(line);
            }
        }
    }
    
    public void switchToMainFrame(){
        loginFrame.dispose();
        currentRoom = rooms.get(0);
        refreshRoomsAndClients(rooms);
        frame.getRoomList().setSelectedIndex(0);
        frame.setVisible(true);
        frame.getInputTextField().requestFocus();
    }
    
    public void loginFrameButtonAction(ActionEvent evt, String logOrReg){
        username = loginFrame.getUsernameTextField().getText().trim();
        sendMsgToSrv(logOrReg);
        sendMsgToSrv(username);
        sendMsgToSrv(new String(loginFrame.getPasswordTextField().getPassword()).trim());
    }
    
    public void privateChatButtonAction(ActionEvent evt){
        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);
        model.addColumn("Available users");
        rooms.forEach((Room room)->{
            room.getClients().forEach(clientThread->{
                if(!username.equals(clientThread.getUsername()))
                    if(clientThread.getCurrentRoom() != null)
                        model.addRow(new Object[]{clientThread.getUsername()});
            });
        });
        JScrollPane scrollPane = new JScrollPane(table);
        JOptionPane.showMessageDialog(null, scrollPane);
        int row = table.getSelectedRow();
        if(row == -1)
            JOptionPane.showMessageDialog(null, "You didn't select a user.");
        else{
            String nameOfOtherClient = (String)model.getValueAt(row,0);
            privateChats.add(new PrivateChatFrame(this, nameOfOtherClient));
        }
    }
    
    public void changeRoom(String newRoom, String command){
        rooms.forEach(room->{
            if(room.getName().equals(newRoom))
                currentRoom = room;
        });
        sendMsgToSrv("/changeRoom" + command);
        sendMsgToSrv(newRoom);
    }
    
    public void updateRenamedRoom(String newName){
        rooms.forEach(room->{
            if(room.getName().equals(newName))
                currentRoom = room;
        });
    }

    
    public void refreshRoomsAndClients(ArrayList<Room> rooms){
        
        this.rooms = rooms;
        frame.getRoomModel().removeAllElements();
        rooms.forEach((room) -> {
            frame.getRoomModel().addElement(room.getName());
        });
        if(currentRoom!= null)
            frame.getRoomList().setSelectedValue(currentRoom.getName(), false);
        
        frame.getUserList().setText("");
        rooms.forEach((Room room)->{
            if(currentRoom != null){
                if(room.getName().equals(currentRoom.getName())){
                    room.getClients().forEach(clientThread->{
                        frame.getUserList().append(clientThread.getUsername() + "\n");
                    });
                }
            }
        });
    }
    
    
    @Override
    public void run() {
        
        try {
            out = new PrintWriter(client.getClSocket().getOutputStream(),true);
        } catch(IOException e) {
            System.err.println(e);
        }
        
        frame = new MainFrame(this);
        new ServerListener(this).start();
        loginFrame = new LoginFrame(this);
    }
}
