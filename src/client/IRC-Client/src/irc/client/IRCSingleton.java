/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.stage.Stage;

/**
 *
 * @author marcin
 */
public class IRCSingleton {
   private static IRCSingleton instance = null;
   
    private LobbyInfo lobbyInfo;
    private ClientInfo clientInfo;
    private ServerInfo serverInfo;
    private Socket serverSocket;
   
    
    private HashMap<String,FXMLPmController> userChatControllers; 
    //private HashMap<String,Stage> roomChatControllers; 
   
   private FXMLLobbyController fXMLLobbyController;
   
   protected IRCSingleton() {
        lobbyInfo = new LobbyInfo();
        clientInfo = new ClientInfo();
        serverInfo = new ServerInfo();
        userChatControllers = new HashMap<>(); 
        //roomChatControllers = new HashMap<>(); 
       try {
           serverSocket = new Socket(serverInfo.getName(), serverInfo.getMainPort());
       } catch (IOException ex) {
           Logger.getLogger(IRCSingleton.class.getName()).log(Level.SEVERE, null, ex);
       }
   }
   
   public static IRCSingleton getInstance() {
      if(instance == null) {
         instance = new IRCSingleton();
      }
      return instance;
   }

    public ClientInfo getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(ClientInfo clientInfo) {
        this.clientInfo = clientInfo;
    }

    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    public void setServerInfo(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }

    public Socket getServerSocket() {
        return serverSocket;
    }

    public void setServerSocket(Socket serverSocket) {
        this.serverSocket = serverSocket;
    }
    
    public void clientPortRequest(){
        OutputStream os = null;
       try {
           os = serverSocket.getOutputStream();
           String msg = clientInfo.getNickname();
           while (msg.length()<IRCMessage.SENDER_SIZE-1)
               msg+=" ";
           msg+='\0';
           os.write(msg.getBytes("UTF-8"));
       } catch (IOException ex) {
           Logger.getLogger(IRCSingleton.class.getName()).log(Level.SEVERE, null, ex);
       }
       
        InputStream is;
       try {
            is = serverSocket.getInputStream();        
            byte[] buffer = new byte[Integer.SIZE];
            is.read(buffer);
            int port = ByteBuffer.wrap(buffer).getInt();
            System.out.println(port);
            clientInfo.setPort(port);
            serverSocket.close();
       } catch (IOException ex) {
           Logger.getLogger(IRCSingleton.class.getName()).log(Level.SEVERE, null, ex);
       }
    }

    public LobbyInfo getLobbyInfo() {
        return lobbyInfo;
    }

    public void setLobbyInfo(LobbyInfo lobbyInfo) {
        this.lobbyInfo = lobbyInfo;
    }

    public FXMLLobbyController getfXMLLobbyController() {
        return fXMLLobbyController;
    }

    public void setfXMLLobbyController(FXMLLobbyController fXMLLobbyController) {
        this.fXMLLobbyController = fXMLLobbyController;
    }

    public HashMap<String, FXMLPmController> getUserChatControllers() {
        return userChatControllers;
    }

    public void setUserChatControllers(HashMap<String, FXMLPmController> userChatControllers) {
        this.userChatControllers = userChatControllers;
    }

}
