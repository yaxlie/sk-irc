/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc.client;

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
                int type = msg.getmType();
                //TODO w zaleznosci od type, room or pm
                
                String sender = msg.getSender(false);

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
    
}
