package client;

import java.net.*;
import java.io.*;



public class Client{
    
    private final int port;
    private String host = null;
    
    private Socket clSocket = null;
    
    private ClientGUI clientGUI = null;
    
    private boolean isClosed = false;

    
    public Client(int port, String host){
        this.port = port;
        this.host = host;
    }
    
    public Socket getClSocket(){
        return clSocket;
    }
    
    public ClientGUI getClientGUI(){
        return clientGUI;
    }
    
    public void setIsClosed(boolean isClosed){
        this.isClosed = isClosed;
    }
    
    public boolean getIsClosed(){
        return isClosed;
    }
    
    /**
     * start client socket and streamthreads
     */
    private void start(){
        try {
            clSocket = new Socket(host, port);
            clientGUI = new ClientGUI(this);
            clientGUI.start();
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + host);
        } catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to the host " + host);
        }
    }
    
    private void close(){
        try {
            clSocket.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    
    
    public static void main(String[] args) {
        
        int port = 1234;                                // default port-number
        String host = "localhost";                      // default host
        
        Client client = new Client(port, host);
        client.start();
        
        
        //client.startGUI();
        
        
        /*while(true){
            if(client.isClosed){
                try {
                    client.close();
                } catch (IOException e) {
                    System.err.println(e);
                }
                break;
            }
        }*/
    }
}






