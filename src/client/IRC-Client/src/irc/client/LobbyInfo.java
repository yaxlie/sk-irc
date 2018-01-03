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

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }
    
    public ArrayList<String> getUserNames(){
        ArrayList<String> list = new ArrayList<>();
        for (User u : users){
            list.add(u.getName());
        }
        return list;
    }
    
}
