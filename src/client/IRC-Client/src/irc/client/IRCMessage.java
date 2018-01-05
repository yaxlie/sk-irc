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
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author marcin
 */
public class IRCMessage {
    public static final int TEXT_SIZE = 240;
    public static final int RECEIVER_SIZE = 20;
    public static final int SENDER_SIZE = 20;
    public static final int DATE_SIZE = 40;
    public static final int STRUCT_SIZE = 344;
    private int textBegin = 4;
    private int senderBegin = 244;
    private int receiverBegin = 264;
    private int dateBegin = 284;
    private int dateEnd = 324;
    private int typeBegin = 324;
    private int typeEnd = 344;
    
    private int mConfig = 0;
    private String text="";
    private String sender="";
    private String receiver="";
    private String date="";
    private String type="";
    
    public IRCMessage(int mConfig, String text, String sender, String receiver, String date, String type){
        this.mConfig = mConfig;
        this.text = text;
        this.sender = sender;
        this.receiver = receiver;
        this.date = date;
        this.type = type;
    }
     public IRCMessage(byte[] bytes) {
        String message;
        StandardCharsets.UTF_8.name();
        try {
            message = new String(bytes, "UTF-8");   
            mConfig = ByteBuffer.wrap(bytes).getInt();
            text = message.substring(textBegin, senderBegin);
            sender = message.substring(senderBegin, receiverBegin);
            receiver = message.substring(receiverBegin, dateBegin);
            date = message.substring(dateBegin, dateEnd);
            type = message.substring(typeBegin, typeEnd);
            
//            text = trimZeros(text);
//            sender = trimZeros(sender);
//            receiver = trimZeros(receiver);
//            date = trimZeros(date);
            
            
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(IRCMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
  
  public static String trimZeros(String str) {
            int pos = str.indexOf(0);
            String s = str.substring(0, pos) + " ";
            return pos == -1 ? str : s;
        }

    public String getText(boolean substring) {
        if(substring){
            text = text.substring(0,text.length()-1);
            while(text.charAt(text.length()-1) == ' ')
                text = text.substring(0,text.length()-1);  
        }
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getSender(boolean substring) {
        if(substring){
            sender = sender.substring(0,sender.length()-1);
            while(sender.charAt(sender.length()-1) == ' ')
                sender = sender.substring(0,sender.length()-1);  
        }
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver(boolean substring) {
        if(substring){
            receiver = receiver.substring(0,receiver.length()-1);
            while(receiver.charAt(receiver.length()-1) == ' ')
                receiver = receiver.substring(0,receiver.length()-1);  
        }
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getDate(boolean substring) {
        if(substring){
            date = date.substring(0,date.length()-1);
            while(date.charAt(date.length()-1) == ' ')
                date = date.substring(0,date.length()-1);  
        }
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType(boolean substring) {
        if(substring){
            type = type.substring(0,type.length()-1);
            while(type.charAt(type.length()-1) == ' ')
                type = type.substring(0,type.length()-1);  
        }
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
    
    
    
    public byte[] getByte(){
//        String s = text + sender + receiver + date;
        byte[] b = new byte[STRUCT_SIZE];
        
        try {
                while(text.length()<239)
                    text += " ";
                text+='\0';
                while(sender.length()<19)
                    sender += " ";
                sender+='\0';
                while(receiver.length()<19)
                    receiver += " ";
                receiver+='\0';
                while(date.length()<39)
                    date += " ";
                date+='\0';
                while(type.length()<19)
                    type += " ";
                type+='\0';
                
                String s = text + sender + receiver + date + type;
                byte[] bytes = toBytes(mConfig);
//                System.out.println(Integer.toString(ByteBuffer.wrap(bytes).getInt()));
//                b = s.getBytes("UTF-8");
                System.arraycopy(bytes, 0, b, 0, 4);
                System.arraycopy(s.getBytes("UTF-8"), 0, b, 4, 340);
//                System.out.println(new String(b));
            } 
        catch (UnsupportedEncodingException ex) {
            Logger.getLogger(IRCMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return b;
    }

    public int getmConfig() {
        return mConfig;
    }

    public void setmType(int mConfig) {
        this.mConfig = mConfig;
    }
    byte[] toBytes(int i)
    {
      byte[] result = new byte[4];

      result[0] = (byte) (i >> 24);
      result[1] = (byte) (i >> 16);
      result[2] = (byte) (i >> 8);
      result[3] = (byte) (i /*>> 0*/);

      return result;
    }
    
    
}
