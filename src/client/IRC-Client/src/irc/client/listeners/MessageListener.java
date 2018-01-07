/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc.client.listeners;

import irc.client.FXMLPmController;
import irc.client.FXMLRoomController;
import irc.client.IRCMessage;
import irc.client.IRCSingleton;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author marcin
 */
public class MessageListener implements Runnable{
    
        
    private IRCSingleton irc;
    private int port;
    private Socket socket;
    private boolean active;
    
    public MessageListener(boolean active){
        this.active = active;
        irc = IRCSingleton.getInstance();
        //port = irc.getClientInfo().getPort();
        port = irc.getClientInfo().getWritePort();
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
//                 byte[] buffer = new byte[LobbyDataProcessing.STRUCT_SIZE];
                byte[] buffer = new byte[IRCMessage.STRUCT_SIZE];
                is.read(buffer);
                IRCMessage msg = new IRCMessage(buffer);
                int config = msg.getmConfig();
                //TODO w zaleznosci od type, room or pm
                
                String sender = msg.getSender(false);
                System.out.println(config);

                switch(config){
                    case 1:
                        showPM(sender, msg);
                        break;
                    case 2:
                        showRM(sender, msg);
                        break;
                    default:
                        break;  
                }
                
            } catch (IOException ex) {
                Logger.getLogger(IRCSingleton.class.getName()).log(Level.SEVERE, null, ex);
            }
            try{
                socket.close();
            }catch(Exception e){}
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    private void showPM(String sender, IRCMessage msg){
        FXMLPmController contr = irc.getUserChatControllers().get(sender);
                
                if(contr == null){
                    Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        irc.getfXMLLobbyController().newStagePm(sender);
                        FXMLPmController contr = irc.getUserChatControllers().get(sender);
                        contr.getMsgArea().appendText("\n" + msg.getSender(true) + ": " + msg.getText(true));
                    }
                    // ...
                    });
                }
                else
                    contr.getMsgArea().appendText("\n" + msg.getSender(true) + ": " + msg.getText(true));
    }
    
        private void showRM(String sender, IRCMessage msg){
        FXMLRoomController contr = irc.getRoomChatControllers().get(sender);
                //System.out.println(msg.getText(true) + "\n" + msg.getSender(true)+".");
                if(contr == null){
                    Platform.runLater(new Runnable(){
                    @Override
                    public void run() {
                        irc.getfXMLLobbyController().newStageRoom(sender, irc.getLobbyInfo().getRoomId(sender));
                        FXMLRoomController contr = irc.getRoomChatControllers().get(sender);
                        contr.getMsgArea().appendText("\n" + msg.getType(true) + ": " + msg.getText(true));
                    }
                    // ...
                    });
                }
                else
                    contr.getMsgArea().appendText("\n" + msg.getType(true) + ": " + msg.getText(true));
    }
}
