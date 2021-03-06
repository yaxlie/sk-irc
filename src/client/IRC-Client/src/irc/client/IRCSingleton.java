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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
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
    private ArrayList<Thread>threads;
   
    
    private HashMap<String,FXMLPmController> userChatControllers; 
    private HashMap<String,FXMLRoomController> roomChatControllers; 
   
   private FXMLLobbyController fXMLLobbyController;
   
   protected IRCSingleton() {
        lobbyInfo = new LobbyInfo();
        clientInfo = new ClientInfo();
        serverInfo = new ServerInfo();
        userChatControllers = new HashMap<>(); 
        threads = new ArrayList<>();
        roomChatControllers = new HashMap<>(); 
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
    
    public void clientLogin(Button button){
        
        try {
           serverSocket = new Socket(serverInfo.getName(), serverInfo.getMainPort());
        } catch (IOException ex) {
           Logger.getLogger(IRCSingleton.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        OutputStream os = null;
        if(serverSocket!=null){
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

                 FXMLLoader fxmlLoader = new FXMLLoader();
                 fxmlLoader.setLocation(getClass().getResource("FXMLLobby.fxml"));
                 Scene scene;
                 try {   
                     scene = new Scene(fxmlLoader.load());
                     Stage stage = new Stage();
                     stage.setTitle("Poczekalnia IRC");
                     stage.setOnCloseRequest( event -> {
                         logout();
                         threads.forEach((t) -> {
                             t.stop();
                         });
                         System.exit(0);
                    });
                     stage.setScene(scene);
                     stage.show();
//                     
//                    try {
//                        TimeUnit.SECONDS.sleep(1);
//                    } catch (InterruptedException ex) {
//                        Logger.getLogger(FXMLRoomController.class.getName()).log(Level.SEVERE, null, ex);
//                    }
//                     
//                    ArrayList<String> userList = getLobbyInfo().getUserNames();
//                    getfXMLLobbyController().getUserList().setItems(FXCollections.observableArrayList(userList));
//                    
//                    ArrayList<String> roomList = getLobbyInfo().getRoomNames();
//                    //System.out.println(userList.size());
//                    Platform.runLater(new Runnable(){
//                     @Override
//                     public void run() {
//                         getfXMLLobbyController().getRoomList().
//                            setItems(FXCollections.observableArrayList(roomList));
//                     }
//                    });

                     stage = (Stage) button.getScene().getWindow();
                     // do what you have to do
                     stage.close();

                     } catch (IOException ex) {
                         Logger.getLogger(FXMLLoginController.class.getName()).log(Level.SEVERE, null, ex);
                     }

            } catch (IOException ex) {
                Logger.getLogger(IRCSingleton.class.getName()).log(Level.SEVERE, null, ex);
            }
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

    public void logout(){
        System.out.println("[client]: Wylogowywanie");
        OutputStream os = null;
        try {
            Socket socket = new Socket(getServerInfo().getName(), getClientInfo().getMsgPort());
            os = socket.getOutputStream();
            IRCMessage msg = new IRCMessage(1, "logout", getClientInfo().getNickname(),
                "server", "data", "14");
            os.write(msg.getByte());
            socket.close();

        } catch (IOException ex) {
            Logger.getLogger(IRCSingleton.class.getName()).log(Level.SEVERE, null, ex);
        }
        for(Thread t : threads)
            t.interrupt();
    }

    public ArrayList<Thread> getThreads() {
        return threads;
    }

    public HashMap<String, FXMLRoomController> getRoomChatControllers() {
        return roomChatControllers;
    }

    public void setRoomChatControllers(HashMap<String, FXMLRoomController> roomChatControllers) {
        this.roomChatControllers = roomChatControllers;
    }

    public void joinRoomRequest(String name){
        System.out.println("[client]: Prośba o dołączenie do pokoju : " + name);
        OutputStream os = null;
        try {
            Socket socket = new Socket(getServerInfo().getName(), getClientInfo().getMsgPort());
            os = socket.getOutputStream();
            IRCMessage msg = new IRCMessage(1, "join_request", getClientInfo().getNickname(),
                name, "data", "12");
            os.write(msg.getByte());
            socket.close();

        } catch (IOException ex) {
            Logger.getLogger(IRCSingleton.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void leaveRoomRequest(String name){
        System.out.println("[client]: Prośba o opuszczenie pokoju : " + name);
        OutputStream os = null;
        try {
            Socket socket = new Socket(getServerInfo().getName(), getClientInfo().getMsgPort());
            os = socket.getOutputStream();
            IRCMessage msg = new IRCMessage(1, "leave_room_request", getClientInfo().getNickname(),
                name, "data", "13");
            os.write(msg.getByte());
            socket.close();

        } catch (IOException ex) {
            Logger.getLogger(IRCSingleton.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
