/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc.client.listeners;

import irc.client.FXMLRoomController;
import irc.client.IRCSingleton;
import irc.client.LobbyDataProcessing;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;

/**
 *
 * @author marcin
 */
public class LobbyListener implements Runnable{
    
    private IRCSingleton irc;
    private int port;
    private Socket socket;
    private boolean active;
    
    public LobbyListener(boolean active){
        this.active = active;
        irc = IRCSingleton.getInstance();
        //port = irc.getClientInfo().getPort();
        port = irc.getClientInfo().getPort();
    }
    
    @Override
    public void run() {
        InputStream is;
        while(active){
            try {
                socket = new Socket(irc.getServerInfo().getName(), port);
            } catch (IOException ex) {
                Logger.getLogger(IRCSingleton.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try {
                 is = socket.getInputStream();        
                 byte[] buffer = new byte[LobbyDataProcessing.STRUCT_SIZE];
                 int b = is.read(buffer);
                 //if(b!=-1){
                    LobbyDataProcessing lobbyDataProcessing = new LobbyDataProcessing(buffer);
                    lobbyDataProcessing.process();

                    //ustaw listę użytkowników w lobby
                    ArrayList<String> userList = irc.getLobbyInfo().getUserNames();
                    irc.getfXMLLobbyController().getUserList().
                            setItems(FXCollections.observableArrayList(userList));
                    

                    
                    //ustaw listę pokojów w lobby
                    ArrayList<String> roomList = irc.getLobbyInfo().getRoomNames();
                    //System.out.println(userList.size());
                    Platform.runLater(new Runnable(){
                     @Override
                     public void run() {
                         irc.getfXMLLobbyController().getRoomList().
                            setItems(FXCollections.observableArrayList(roomList));
                     }
                    });
                    
                    //odswież listę użytkowników dla każdego otwartego okna pokoju
                    for (FXMLRoomController r : irc.getRoomChatControllers().values()) {
                        r.refreshList();
                    }
                    
                // }
                 socket.close();
            } catch (IOException ex) {
                Logger.getLogger(IRCSingleton.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
}
