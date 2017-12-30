/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc.client;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marcin
 */
public class LobbyDataProcessing {
    private byte[] buffer;
    private IRCSingleton irc = IRCSingleton.getInstance();
    
    public static int ROOM_STRUCT_SIZE = 3 * 4 + 20 + 20;
    public static final int ROOMS_LIMIT = 10;
    public static int USER_STRUCT_SIZE = 4+ 20;
    public static int USERS_LIMIT = 100;
    
    public LobbyDataProcessing(byte[] buffer){
        this.buffer = buffer;
    }
    
    public void process(){
        ArrayList<User> userList = processUsers();
        irc.getLobbyInfo().setUsers(userList);
    }
    
    private ArrayList<User> processUsers(){
        ArrayList<User> userList = new ArrayList<>();
        int pointer = 0;
        pointer += ROOM_STRUCT_SIZE * ROOMS_LIMIT;
        for(int i=0; i<USERS_LIMIT; i++){
            User user = new User();
            //process user port
            byte [] sub = Arrays.copyOfRange(buffer, pointer, pointer+User.PORT_SIZE);
            user.setPort(ByteBuffer.wrap(sub).getInt());
            pointer += User.PORT_SIZE;
            //process user name
            sub = Arrays.copyOfRange(buffer, pointer, pointer+User.NAME_SIZE);
            pointer += User.NAME_SIZE;
            try {
                user.setName(new String(sub, "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(LobbyDataProcessing.class.getName()).log(Level.SEVERE, null, ex);
            }
            if(user.getName().matches(".*[0-9].*") || user.getName().matches(".*[a-z].*")
                    || user.getName().matches(".*[A-Z].*")){
                userList.add(user);
                System.out.println(user.getName());
            }
            
        }
        return userList;
    }
}
