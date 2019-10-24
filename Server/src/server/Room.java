package server;

import java.util.*;
import java.io.*;

public class Room implements Serializable{
    private String name;
    private ArrayList<ClientThread> clients = new ArrayList<>();
    
    Room(String name){
        this.name = name;
    }
    
    public String getName(){
        return name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public ArrayList<ClientThread> getClients(){
        return clients;
    }
    
    void changeName(String name){
        this.name = name;
    }
    
    void addClient(ClientThread client){
        clients.add(client);
    }
    
    void sendAllClientsToDefault(){
        clients.forEach(client->{
            client.sendMessageToMe("\n*** The server closed the room. You will be transfered to the default Room ***\n");
            client.sendMessageToMe("/roomClosed");
        });
    }
}
