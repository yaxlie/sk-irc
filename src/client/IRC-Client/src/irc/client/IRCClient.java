/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc.client;

import java.util.HashMap;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author marcin
 */
public class IRCClient extends Application {
    
    public static final int ROOMS = 10;
    public static final int USERS = 100;
    
    private IRCSingleton irc = IRCSingleton.getInstance();
    
    
    @Override
    public void start(Stage stage) throws Exception {
            
        Parent root = FXMLLoader.load(getClass().getResource("FXMLLogin.fxml"));
        
        Scene scene = new Scene(root);  
        stage.setOnCloseRequest( event -> {
                            irc.logout();
                       });
        stage.setScene(scene);
        stage.show();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
