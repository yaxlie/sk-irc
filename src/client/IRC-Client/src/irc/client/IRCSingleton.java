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
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marcin
 */
public class IRCSingleton {
   private static IRCSingleton instance = null;
   
   private ClientInfo clientInfo;
   private ServerInfo serverInfo;
   private Socket serverSocket;
   
   protected IRCSingleton() {
        clientInfo = new ClientInfo();
        serverInfo = new ServerInfo();
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
            serverSocket.close();
       } catch (IOException ex) {
           Logger.getLogger(IRCSingleton.class.getName()).log(Level.SEVERE, null, ex);
       }
    }
    
}
