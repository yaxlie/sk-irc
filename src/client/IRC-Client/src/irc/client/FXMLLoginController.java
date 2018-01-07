/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc.client;

import irc.client.IRCSingleton;
import irc.client.IRCSingleton;
import irc.client.IRCSingleton;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author marcin
 */
public class FXMLLoginController implements Initializable {
    
    private IRCSingleton irc = IRCSingleton.getInstance();
    
    @FXML
    private Button button;
    @FXML
    private TextField login;
    @FXML
    private TextField server;
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        button.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
               if(!"".equals(login.getText())){
                    
                    String ipText = server.getText();
                    boolean dn = (ipText.equals("localhost") || ipText.split(".").length == 4);
                    String address="";
                    
                    if(!dn){
                        try {
                            System.out.println("Domain name converting...");
                            InetAddress giriAddress = java.net.InetAddress.getByName(server.getText());
                            address = giriAddress.getHostAddress();
                        } catch (UnknownHostException ex) {
                            Logger.getLogger(FXMLLoginController.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                    ipText = dn? ipText:address;
                    
                    irc.getClientInfo().setNickname(login.getText());
                    irc.getServerInfo().setName(ipText);
                    
                    irc.clientLogin(button);
                }
            }
        });  
    }
    
}
