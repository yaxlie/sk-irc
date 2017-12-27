/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc.client;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

/**
 *
 * @author marcin
 */
public class FXMLLobbyController implements Initializable {
    
    @FXML
    private Label label;
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        label.setText("Hello World!");
    }
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            Socket clientSocket = new Socket("localhost", 12345);
            InputStream is = clientSocket.getInputStream();
            byte[] buffer = new byte[320];
            is.read(buffer);
            
            IRCMessage message = new IRCMessage(buffer);
            
            
            String msg = message.getText()+message.getSender()+message.getReceiver()+message.getDate();
            System.out.println(msg);
            clientSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(FXMLLobbyController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
}
