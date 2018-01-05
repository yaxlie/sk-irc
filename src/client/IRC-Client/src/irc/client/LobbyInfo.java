/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc.client;

import java.util.ArrayList;

/**
 *
 * @author marcin
 */
public class LobbyInfo {
    private ArrayList<User>users = new ArrayList<>();
    private ArrayList<Room>rooms = new ArrayList<>();
    

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public void setRooms(ArrayList<Room> rooms) {
        this.rooms = rooms;
    }
    
    
    
    public ArrayList<String> getUserNames(){
        ArrayList<String> list = new ArrayList<>();
        for (User u : users){
            list.add(u.getName());
        }
        return list;
    }
    
    public ArrayList<String> getRoomNames(){
        ArrayList<String> list = new ArrayList<>();
        for (Room r : rooms){
            list.add(r.getUsers().size() + "/" + Integer.toString(r.getrSize()) + "   "  + r.getName());
        }
        return list;
    }
    
    public int getPort(String name){
        for(User u : users){
            if(u.getName().equals(name)){
                return u.getPort();
            }
        }
        return 0;
    }
}
