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
    private int textBegin = 4;
    private int senderBegin = 244;
    private int receiverBegin = 264;
    private int dateBegin = 284;
    private int dateEnd = 324;
    
    private int mType = 0;
    private String text="";
    private String sender="";
    private String receiver="";
    private String date="";
    
    public IRCMessage(int mType, String text, String sender, String receiver, String date){
        this.mType = mType;
        this.text = text;
        this.sender = sender;
        this.receiver = receiver;
        this.date = date;
    }
     public IRCMessage(byte[] bytes) {
        String message;
        StandardCharsets.UTF_8.name();
        try {
            message = new String(bytes, "UTF-8");   
            mType = ByteBuffer.wrap(bytes).getInt();
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
    
    public byte[] getByte(){
//        String s = text + sender + receiver + date;
        byte[] b = new byte[324];
        
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
                
                String s = text + sender + receiver + date;
                byte[] bytes = toBytes(mType);
//                System.out.println(Integer.toString(ByteBuffer.wrap(bytes).getInt()));
//                b = s.getBytes("UTF-8");
                System.arraycopy(bytes, 0, b, 0, 4);
                System.arraycopy(s.getBytes("UTF-8"), 0, b, 4, 320);
//                System.out.println(new String(b));
            } 
        catch (UnsupportedEncodingException ex) {
            Logger.getLogger(IRCMessage.class.getName()).log(Level.SEVERE, null, ex);
        }
        return b;
    }

    public int getmType() {
        return mType;
    }

    public void setmType(int mType) {
        this.mType = mType;
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
