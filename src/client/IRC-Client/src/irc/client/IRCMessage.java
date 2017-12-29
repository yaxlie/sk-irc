/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

//Convert from String to byte[]:
//
//String s = "some text here";
//byte[] b = s.getBytes("UTF-8");
//Convert from byte[] to String:
//
//byte[] b = {(byte) 99, (byte)97, (byte)116};
//String s = new String(b, "US-ASCII");


package irc.client;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marcin
 */
public class IRCMessage {
    private int textBegin = 0;
    private int senderBegin = 240;
    private int receiverBegin = 260;
    private int dateBegin = 280;
    
    private String text="";
    private String sender="";
    private String receiver="";
    private String date="";

  public IRCMessage(byte[] bytes) {
        String message;
        StandardCharsets.UTF_8.name();
        try {
            message = new String(bytes, "UTF-8");
            text = message.substring(textBegin, senderBegin);
            sender = message.substring(senderBegin, receiverBegin);
            receiver = message.substring(receiverBegin, dateBegin);
            date = message.substring(dateBegin, bytes.length);
            
            text = trimZeros(text);
            sender = trimZeros(sender);
            receiver = trimZeros(receiver);
            date = trimZeros(date);
            
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(IRCMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  
  static String trimZeros(String str) {
            int pos = str.indexOf(0);
            return pos == -1 ? str : str.substring(0, pos);
        }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
  
}
