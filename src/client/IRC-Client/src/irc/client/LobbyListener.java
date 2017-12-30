/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        port = 12345;
        try {
           socket = new Socket(irc.getServerInfo().getName(), port);
       } catch (IOException ex) {
           Logger.getLogger(IRCSingleton.class.getName()).log(Level.SEVERE, null, ex);
       }
    }
    
    @Override
    public void run() {
        InputStream is;
        while(active){
            try {
                 is = socket.getInputStream();        
                 byte[] buffer = new byte[5000];
                 is.read(buffer);
                 //int port = ByteBuffer.wrap(buffer).getInt();
                 String s = new String(buffer, "UTF-8");
                 System.out.println(s);
            } catch (IOException ex) {
                Logger.getLogger(IRCSingleton.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try {
            socket.close();
        } catch (IOException ex) {
            Logger.getLogger(LobbyListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
    
    
    
}
