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
public class Room {
    public static final int ID_SIZE = 4;
    public static final int NAME_SIZE = 20;
    public static final int PASSWORD_SIZE = 20;
    public static final int USERS_SIZE = 10*24;
    
    private int id;
    private String name;
    private String password;
    private ArrayList<User> users = new ArrayList<>();
    private int rSize = 10;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }

    public int getrSize() {
        return rSize;
    }

    public void setrSize(int rSize) {
        this.rSize = rSize;
    }
    
    public ArrayList<String> getUserNames(){
        ArrayList<String> list = new ArrayList<>();
        for (User u : users){
            list.add(u.getName());
        }
        return list;
    }

 
}
