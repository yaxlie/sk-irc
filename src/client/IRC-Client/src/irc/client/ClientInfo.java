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
public class ClientInfo {
    
    private String nickname = "Marcin";
    private int port;
    private int writePort;
    private int msgPort;

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
        msgPort = port + 1000;
        writePort = port + 2000;
    }

    public int getMsgPort() {
        return msgPort;
    }

    public void setMsgPort(int msgPort) {
        this.msgPort = msgPort;
    }

    public int getWritePort() {
        return writePort;
    }

    public void setWritePort(int writePort) {
        this.writePort = writePort;
    }
    
    
}
