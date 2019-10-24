package server;

import java.net.*;
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;


public class Server{
    
    private final int port;
    private ServerSocket srvSocket = null;
    private Socket clSocket = null;
    
    private final ArrayList<ClientThread> threads = new ArrayList<>();
    private final ArrayList<Room> rooms = new ArrayList<>();
    
    private MainFrame frame;
    
    private BufferedWriter logWriter;
    
    public Server(int port){
        this.port = port;                
    }
    
    /* getter */
    
    public ArrayList<ClientThread> getThreads(){
        return threads;
    }
    public ArrayList<Room> getRooms(){
        return rooms;
    }
    public ServerSocket getSrvSocket(){
        return srvSocket;
    }
    public BufferedWriter getLogWriter(){
        return logWriter;
    }

    
    public void createLogFile(){
        Instant timestamp = Instant.now();
        LocalDateTime ldt = LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault());
        String filename = String.format("%04d-%02d-%02d_%02d-%02d-%02d", ldt.getYear(), ldt.getMonth().getValue(), ldt.getDayOfMonth(), ldt.getHour(), ldt.getMinute(), ldt.getSecond());
        String filepath = "log" + File.separator + filename + ".txt";
        File file = new File(filepath);
        try {
            file.createNewFile();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            logWriter = new BufferedWriter(new FileWriter(filepath, true));
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Start server socket and a thread to observe keyboard input
     */
    public void start(){
        try {
            srvSocket = new ServerSocket(port);
            rooms.add(new Room("<default>"));
            rooms.add(new Room("Informatik"));
            rooms.add(new Room("Uni-Jena"));
            rooms.add(new Room("Musiker"));
            rooms.add(new Room("Programming"));
            rooms.add(new Room("Fuck It"));
            frame = new MainFrame(this);
            frame.setVisible(true);
            logAction("Server socket started on port " + port + ".");
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    /**
     * Accept new client sockets and start a ClientThread for each
     */
    public void listenAndConnectToClients(){
        while(true){
            try{
                clSocket = srvSocket.accept();
                new ClientThread(clSocket, this).start();
            } catch (SocketException e) {
                if(srvSocket.isClosed()){
                    System.out.println("Server Closed");
                    break;
                }
            } catch (IOException e) {
                System.err.println(e);
            }
        }
    }
    
    
    public void refreshUserList(){
        frame.getUserModel().removeAllElements();
        rooms.forEach((room) -> {
            room.getClients().forEach(client->{
                frame.getUserModel().addElement("[" + room.getName() + "] " + client.getUsername());
            });
        });
    }
    
    public void logAction(String action){
        Instant timestamp = Instant.now();
        LocalDateTime ldt = LocalDateTime.ofInstant(timestamp, ZoneId.systemDefault());
        action = String.format("[%d:%02d] %s%n", ldt.getHour(), ldt.getMinute(), action.replaceAll("\n", ""));
        frame.getLogTextArea().append(action);
        try {
            logWriter.append(action);
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    /* room functionality */
    
    public void sendRoomsToAll(){
        threads.forEach(thread->{
            thread.sendRoomsToMe();
        });
    }
    
    public boolean createRoom(String name){
        boolean success = true;
        logAction("[DO_CREATEROOM] " + name);
        for(int i=0; i<rooms.size(); i++){
            if(rooms.get(i).getName().equalsIgnoreCase(name))
                success = false;
        }
        if(success){
            rooms.add(new Room(name));
            frame.getRoomModel().addElement(name);
            sendRoomsToAll();
            logAction("[DONE_CREATEROOM] " + name);
        }
        else
            logAction("[FAILED_CREATEROOM] " + name);
        return success;
    }
    
    public void deleteRoom(String name){
        logAction("[DO_DELETEROOM] " + name);
        for(int i=0; i<rooms.size(); i++){
            if(rooms.get(i).getName().equalsIgnoreCase(name)){
                rooms.get(i).sendAllClientsToDefault();
                rooms.remove(i);
            }
        }
        frame.getRoomModel().removeElement(name);
        sendRoomsToAll();
        logAction("[DONE_DELETEROOM] " + name);
    }

    public boolean renameRoom(String oldName, String newName){
        boolean success = true;
        logAction("[DO_RENAMEROOM] " + oldName + " to " + newName);
        for(int i=0; i<rooms.size(); i++){
            if(rooms.get(i).getName().equalsIgnoreCase(newName))
                success = false;
        }
        if(success){
            for(int i=0; i<rooms.size(); i++){
                if(rooms.get(i).getName().equalsIgnoreCase(oldName)){
                    rooms.get(i).setName(newName);
                    frame.getRoomModel().set(i,newName);
                }
            }
            threads.forEach(thread->{
                if(thread.getCurrentRoom().getName().equals(newName)){
                    thread.sendMessageToMe("\n*** The server renamed the room to '" + newName + "' ***\n");
                    thread.sendMessageToMe("/roomRenamed");
                    thread.sendRoomsToMe();
                    thread.sendMessageToMe(newName);
                }
            });
            sendRoomsToAll();
            refreshUserList();
            logAction("[DONE_RENAMEROOM] " + oldName + " to " + newName);
        }
        else
            logAction("[FAILED_RENAMEROOM] " + oldName + " to " + newName);
        return success;
    }
    
    
    /* ban functionality */
    
    public void warnUser(String username, String message){
        logAction("[DO_WARNUSER] " + username);
        threads.forEach(thread->{
            if(thread.getUsername().equals(username))
                thread.sendMessageToMe("*** " + message + " ***");
        });
        logAction("[DONE_WARNUSER] " + username);
    }
    
    public void banUser(String username){
        logAction("[DO_BANUSER] " + username);
        threads.forEach(thread->{
            if(thread.getUsername().equals(username))
                thread.sendMessageToMe("/ban");
        });
        logAction("[DONE_BANUSER] " + username);
    }
    
    public void banUserPermanently(String username){
        logAction("[DO_BANUSERPERMANENTLY] " + username);
        StringBuilder sb = new StringBuilder();
        sb.append(username);
        
        try{
            BufferedWriter fileOut = new BufferedWriter(new FileWriter("bannedMembers.txt", true));
            fileOut.append(sb);
            fileOut.newLine();
            fileOut.close();
        }catch(FileNotFoundException e){
            System.err.println("File not found.");
        }catch(IOException e){
            System.err.println(e);
        }
        banUser(username);
        logAction("[DONE_BANUSERPERMANENTLY] " + username);
    }
    
    public void showUserData(){
        logAction("[SHOW_USERDATA]");
        DefaultTableModel model = new DefaultTableModel();
        JTable table = new JTable(model);
        model.addColumn("username");
        model.addColumn("password");

        String tempUsername;
        String tempPassword;
        
        try{
            Scanner fileRead = new Scanner(new File("members.txt"));
            fileRead.useDelimiter("[,\n]");
            
            while(fileRead.hasNext()){
                tempUsername = fileRead.next();
                tempPassword = fileRead.next();
                model.addRow(new Object[]{tempUsername, tempPassword});
            }
            fileRead.close();
        }catch(FileNotFoundException e){
            System.err.println("File not found.");
        }catch(Exception e){
            System.err.println(e);
        }
        JScrollPane scrollPane = new JScrollPane(table);
        JOptionPane.showMessageDialog(null, scrollPane);
    }
    
    
    public static void main(String args[]) {

        int port = 1234;                // default port-number
        
        Server server = new Server(port);
        server.createLogFile();
        server.start();
        server.listenAndConnectToClients();
        
    } 
}