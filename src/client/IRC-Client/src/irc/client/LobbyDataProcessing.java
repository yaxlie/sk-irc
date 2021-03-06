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
    
    public static int ROOM_MAX_USERS = 10;
    public static int ROOM_STRUCT_SIZE = 4 + 20 + 20 + 10*24;
    public static final int ROOMS_LIMIT = 10;
    public static int USER_STRUCT_SIZE = 4+ 20;
    public static int USERS_LIMIT = 100;
    public static int STRUCT_SIZE = (4 + 20 + 20 + 10*24) * 10 + (4+ 20) * 100;
    
    public LobbyDataProcessing(byte[] buffer){
        this.buffer = buffer;
    }
    
    public void process(){
        ArrayList<User> userList = processUsers();
        irc.getLobbyInfo().setUsers(userList);
        
        ArrayList<Room> roomList = processRooms();
        irc.getLobbyInfo().setRooms(roomList);
    }
    
    private ArrayList<User> processUsers(){
        ArrayList<User> userList = new ArrayList<>();
        int pointer = 0;
        pointer += ROOM_STRUCT_SIZE * ROOMS_LIMIT;
        for(int i=0; i<USERS_LIMIT; i++){
            User user = new User();
//            try {
//                //process user port
//                System.out.println(new String(buffer, "UTF-8"));
//            } catch (UnsupportedEncodingException ex) {
//                Logger.getLogger(LobbyDataProcessing.class.getName()).log(Level.SEVERE, null, ex);
//            }
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
                //System.out.println(user.getName());
            }
            
        }
        return userList;
    }
    
    private ArrayList<Room> processRooms(){
        ArrayList<Room> roomList = new ArrayList<>();
        int pointer = 0;
        
        for(int i=0; i<ROOMS_LIMIT; i++){
            Room room = new Room();
            //process id
            byte [] sub = Arrays.copyOfRange(buffer, pointer, pointer+Room.ID_SIZE);
            room.setId(ByteBuffer.wrap(sub).getInt());
            pointer += Room.ID_SIZE;
            //process room name
            sub = Arrays.copyOfRange(buffer, pointer, pointer+Room.NAME_SIZE);
            pointer += Room.NAME_SIZE;
            try {
                room.setName(new String(sub, "UTF-8"));
            } catch (UnsupportedEncodingException ex) {}
            
            //process room password
            sub = Arrays.copyOfRange(buffer, pointer, pointer+Room.PASSWORD_SIZE);
            pointer += Room.PASSWORD_SIZE;
            try {
                room.setPassword(new String(sub, "UTF-8"));
            } catch (UnsupportedEncodingException ex) {}
            
            //dodawanie uzytkownikow pokoju
            for(int j=0; j<ROOM_MAX_USERS; j++){
                User user = new User();
                //process user port
                byte [] sub2 = Arrays.copyOfRange(buffer, pointer, pointer+User.PORT_SIZE);
                user.setPort(ByteBuffer.wrap(sub2).getInt());
                pointer += User.PORT_SIZE;
                //process user name
                sub2 = Arrays.copyOfRange(buffer, pointer, pointer+User.NAME_SIZE);
                pointer += User.NAME_SIZE;
                try {
                    user.setName(new String(sub2, "UTF-8"));
                } catch (UnsupportedEncodingException ex) {
                    Logger.getLogger(LobbyDataProcessing.class.getName()).log(Level.SEVERE, null, ex);
                }
                if(user.getName().matches(".*[0-9].*") || user.getName().matches(".*[a-z].*")
                    || user.getName().matches(".*[A-Z].*"))
                    room.getUsers().add(user);
            }
//                if(room.getName().matches(".*[0-9].*") || room.getName().matches(".*[a-z].*")
//                    || room.getName().matches(".*[A-Z].*"))
                    roomList.add(room);
                    //System.out.println(room.getName() + room.getPassword());
            }
           return roomList; 
        }
}
