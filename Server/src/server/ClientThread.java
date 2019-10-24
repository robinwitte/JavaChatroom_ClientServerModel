package server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClientThread extends Thread implements Serializable{
    
    /* variables for saving username and password */
    private static Scanner fileRead;
    String username = "";
    String filepath = "members.txt";
    
    boolean running = true;
    
    private Room currentRoom = null;
    
    /* streams, sockets and list of threads */
    private transient Server server = null;
    private transient Scanner in = null;
    private transient ObjectOutputStream out = null;
    private transient Socket clSocket = null;
    private transient ArrayList<ClientThread> threads = new ArrayList<>();
    private transient ArrayList<Room> rooms = new ArrayList<>();
        
    public ClientThread(Socket clSocket, Server server) {
        this.clSocket = clSocket;
        this.server = server;
        this.threads = server.getThreads();
        this.rooms = server.getRooms();
    }
    
    
    public String getUsername(){
        return username;
    }
    
    public Room getCurrentRoom(){
        return currentRoom;
    }
       
    public void sendMessageToRoom(String line){
        threads.forEach(thread->{
            if(thread.currentRoom == this.currentRoom && thread != this)
                thread.sendMessageToMe(line);
        });
    }
    
    public void sendMessageToMe(String line){
        try {
            server.logAction("--> " + username + " [SENDMESSAGE] " + line);
            out.writeObject(line);
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void  sendMessageToPrivateChat(String line){
        Scanner scanner = new Scanner(line).useDelimiter("#");
        //String dummy = scanner.next();
        String to = scanner.next();
        String message = scanner.next();
        String from = username;
        if(!message.equals("/prvquit"))
            sendMessageToMe(line);
        threads.forEach(thread->{
            if(thread.username.equals(to))
                thread.sendMessageToMe("#" + from + "#" + from + ": " + message);
        });
    }
    
    public void sendRoomsToMe(){
        try {
            out.writeObject(rooms);
            out.reset();
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void sendRoomsToAll(){
        threads.forEach(thread->{
            thread.sendRoomsToMe();
        });
    }
    
    public void changeRoomAtClose(String name){
        server.logAction("<-- " + username + " [DO_CHANGEROOM] " + currentRoom.getName() + " to " + name);
        currentRoom.getClients().remove(this);
        if(currentRoom.getClients().isEmpty())
            rooms.remove(currentRoom);
        rooms.forEach(room->{
            if(room.getName().equals(name))
                currentRoom = room;
        });
        currentRoom.addClient(this);
        sendMessageToRoom("*** " + username + " entered the room " + currentRoom.getName() + " ***");
        sendMessageToMe("\n---------- Welcome to the room '" + currentRoom.getName() + "' " + username + "! ----------\n");
        sendRoomsToAll();
        server.refreshUserList();
        server.logAction("--> " + username + " [DONE_CHANGEROOM] " + currentRoom.getName());
    }
    
    
    public void changeRoom(String name){
        server.logAction("<-- " + username + " [DO_CHANGEROOM] " + currentRoom.getName() + " to " + name);
        sendMessageToRoom("*** " + username + " is leaving the room " + currentRoom.getName() + " ***");
        sendMessageToMe("\n---------- You left the room '" + currentRoom.getName() + "'. -----------\n");
        currentRoom.getClients().remove(this);
        rooms.forEach(room->{
            if(room.getName().equals(name))
                currentRoom = room;
        });
        currentRoom.addClient(this);
        sendMessageToRoom("*** " + username + " entered the room " + currentRoom.getName() + " ***");
        sendMessageToMe("\n---------- Welcome to the room '" + currentRoom.getName() + "' " + username + "! ----------\n");
        sendRoomsToAll();
        server.refreshUserList();
        server.logAction("--> " + username + " [DONE_CHANGEROOM] " + currentRoom.getName());
    }
    
    
    public boolean verifyLogin(String username, String password, String filepath){
        
        boolean found = false;
        String tempUsername = null;
        String tempPassword;
        
        try{
            fileRead = new Scanner(new File(filepath));
            fileRead.useDelimiter("[,\n]");
            
            while(fileRead.hasNext() && !found){
                tempUsername = fileRead.next();
                tempPassword = fileRead.next();
                if(tempUsername.trim().equals(username.trim()) && tempPassword.trim().equals(password.trim())){
                    found = true;
                }
            }
            fileRead.close();
        }catch(FileNotFoundException e){
            System.err.println("File not found.");
        }catch(Exception e){
            System.err.println(e);
        }
        
        return found;
    }     
         
         
    public boolean verifyUniqueUsername(String username, String filepath){
        
        boolean found = false;
        String tempUsername = "";
        String tempPassword = "";
        
        try{
            fileRead = new Scanner(new File(filepath));
            fileRead.useDelimiter("[,\n]");
            
            while(fileRead.hasNext() && !found){
                tempUsername = fileRead.next();
                tempPassword = fileRead.next();
                if(tempUsername.trim().equals(username.trim()))
                    found = true;
            }
            fileRead.close();
        }catch(FileNotFoundException e){
            System.err.println("File not found.");
        }catch(Exception e){
            System.err.println(e);
        }
        return !found;
    }
    
    
    public void writeUserdata (String username, String password, String filepath){
        
        StringBuilder sb = new StringBuilder();
        sb.append(username.trim());
        sb.append(",");
        sb.append(password.trim());
        
        try{
            BufferedWriter fileOut = new BufferedWriter(new FileWriter(filepath, true));
            fileOut.append(sb);
            fileOut.newLine();
            fileOut.close();
        }catch(FileNotFoundException e){
            System.err.println("File not found.");
        }catch(IOException e){
            System.err.println(e);
        }
    }
    
    
    public boolean isUserBanned(String username, String filepath){
        boolean found = false;
        String tempUsername;
        
        try{
            fileRead = new Scanner(new File(filepath));
            fileRead.useDelimiter("[\n]");
            
            while(fileRead.hasNext() && !found){
                tempUsername = fileRead.next().trim();      
                if(tempUsername.equals(username))
                    found = true;
            }
            fileRead.close();
        }catch(FileNotFoundException e){
            System.err.println("File not found.");
        }catch(Exception e){
            System.err.println(e);
        }
        return found;
    }
    
    public boolean isUserLoggedInAlready(String username){
        boolean firstTime = false;
        boolean found = false;
        for(int i=0; i<threads.size(); i++){
            if(threads.get(i).getUsername().equals(username)){
                if(firstTime)
                    found = true;
                firstTime = true;
            }
        }
        return found;
    }
    
    public void quit(){
        sendMessageToMe("/quit");
        threads.remove(this);
        if(currentRoom != null){
            currentRoom.getClients().remove(this);
            sendRoomsToAll();
            server.refreshUserList();
        }
        in.close();
        try {
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            clSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void loginOrRegister(){
        while(true){
            String logOrReg = in.nextLine().trim();
            if(logOrReg.equals("/quit")){
                running = false;
                break;
            }
            username = in.nextLine();
            String password = in.nextLine();

            if(logOrReg.equals("/login")){
                if(!verifyLogin(username, password, filepath))
                    sendMessageToMe("Wrong username or password.");
                else if(isUserBanned(username, "bannedMembers.txt"))
                    sendMessageToMe("You are banned.");
                else if(isUserLoggedInAlready(username))
                    sendMessageToMe("You are logged in already.");
                else
                    break;
            }
            else if(logOrReg.equals("/register")){
                if(!verifyUniqueUsername(username, filepath))
                    sendMessageToMe("The username exists already.");
                else{
                    writeUserdata(username, password, filepath);
                    break;
                }
            }
        }
    }
    
    
    @Override
    public void run() {

        try {
               
            /* initialise in- and output-streams */
            in = new Scanner(clSocket.getInputStream());
            out = new ObjectOutputStream(clSocket.getOutputStream());
            
            threads.add(this);
            
            loginOrRegister();
            
            if(running){
                
                currentRoom = rooms.get(0);
                rooms.get(0).addClient(this);
                sendRoomsToAll();
                server.refreshUserList();
                
                server.logAction("[NEW_USER] " + username);
                
                sendMessageToMe("/success");
                sendMessageToMe("\n---------- Welcome to the room '" + currentRoom.getName() + "' " + username + "! ----------\n");
                sendMessageToRoom("*** " + username + " entered the room " + currentRoom.getName() + " ***");

                OUTER:
                while (true) {
                    String line;
                    line = in.nextLine();
                    switch (line) {
                        case "/quit":
                            server.logAction("<-- " + username + " [DO_QUIT]");
                            break OUTER;
                        case "/changeRoomRegular":
                            line = in.nextLine();
                            changeRoom(line);
                            break;
                        case "/changeRoomClose":
                            line = in.nextLine();
                            changeRoomAtClose(line);
                            break;
                        default:
                            if(line.startsWith("#")){
                                sendMessageToPrivateChat(line);
                            }
                            else{
                                server.logAction("<-- " + username + " [DO_SENDMESSAGETOROOM] " + line);
                                sendMessageToRoom(username + ": " + line);
                                sendMessageToMe(line);
                            }
                            break;
                    }
                }
                
                sendMessageToRoom("*** " + username + " is leaving the room " + currentRoom.getName() + " ***");
               
                sendMessageToMe("*** Bye " + username + " ***");
                
                server.logAction("[USER_LEAVE] " + username);

            }
        } catch (IOException e) {
            System.out.println(e);
        }
        quit();
    }
}