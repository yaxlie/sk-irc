/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc.client;
/**
 *
 * @author marcin
 */
public class ServerInfo {
    private String name = "";
    private int mainPort = 12345;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMainPort() {
        return mainPort;
    }

    public void setMainPort(int mainPort) {
        this.mainPort = mainPort;
    }
    
    
       
}
