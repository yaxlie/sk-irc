/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc.client;

import irc.client.listeners.LobbyListener;
import irc.client.listeners.MessageListener;
import com.sun.javafx.scene.control.skin.LabeledText;
import irc.client.IRCSingleton;
import irc.client.IRCSingleton;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

/**
 *
 * @author marcin
 */
public class FXMLLobbyController implements Initializable {
    
    private IRCSingleton irc = IRCSingleton.getInstance();
    @FXML
    private Label label;
    @FXML
    private ListView roomList;
    @FXML
    private ListView userList;
    @FXML
    private TextArea roomInfoTA;
    @FXML
    private Button joinButton;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        irc.setfXMLLobbyController(this);

    userList.setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            if(e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 &&
               (e.getTarget() instanceof LabeledText || ((GridPane) e.getTarget()).getChildren().size() > 0)) {
               int id = userList.getSelectionModel().getSelectedIndex();
               String cName = irc.getLobbyInfo().getUsers().get(id).getName();
               System.out.println("clicked on " + cName);   
               
               if(irc.getUserChatControllers().get(cName) != null){
                   irc.getUserChatControllers().get(cName).getStage().toFront();
               }
               else{
                       newStagePm(cName);         
               }
            }    
        }
    });
        
    roomList.setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            if(e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 &&
               (e.getTarget() instanceof LabeledText || ((GridPane) e.getTarget()).getChildren().size() > 0)) {
               int id = roomList.getSelectionModel().getSelectedIndex();
               String cName = irc.getLobbyInfo().getRooms().get(id).getName();
               System.out.println("clicked on " + cName);   
               
               if(irc.getRoomChatControllers().get(cName) != null){
                   irc.getRoomChatControllers().get(cName).getStage().toFront();
               }
               else{
                       irc.joinRoomRequest(cName);
                       newStageRoom(cName, id);         
               }
            }    
        }
    });

        LobbyListener ll = new LobbyListener(true);
        Thread thread = new Thread(ll);
        irc.getThreads().add(thread);
        thread.start();
        
        MessageListener ml = new MessageListener(true);
        Thread thread2 = new Thread(ml);
        irc.getThreads().add(thread2);
        thread2.start();
    }    

    public ListView getUserList() {
        return userList;
    }

    public ListView getRoomList() {
        return roomList;
    }
    
    private Stage newStage(String fxmlfile, String title){
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            fxmlLoader.setLocation(getClass().getResource(fxmlfile));
            Scene scene;
            scene = new Scene(fxmlLoader.load());
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            
            FXMLPmController controller = (FXMLPmController)fxmlLoader.getController();
            controller.setStage(stage);
            irc.getUserChatControllers().put(title, controller);

            return stage;
        } catch (IOException ex) {
            Logger.getLogger(FXMLLobbyController.class.getName()).log(Level.SEVERE, null, ex);
        }   
        return null;
    }
    
    public Stage newStagePm(String title){
        Stage stage = null;
        FXMLLoader fxmlLoader = new FXMLLoader();
        
        try {
            fxmlLoader.setLocation(getClass().getResource("FXMLPm.fxml"));
            Scene scene;
            scene = new Scene(fxmlLoader.load());
            stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            
            FXMLPmController controller = (FXMLPmController)fxmlLoader.getController();
            controller.setStage(stage);
            irc.getUserChatControllers().put(title, controller);

        } catch (IOException ex) {
            Logger.getLogger(FXMLLobbyController.class.getName()).log(Level.SEVERE, null, ex);
        }  

        FXMLPmController controller = (FXMLPmController)fxmlLoader.getController();
        controller.setStage(stage);
        irc.getUserChatControllers().put(title, controller);
        stage.setOnCloseRequest( event -> {
            irc.getUserChatControllers().remove(title);
        });
        
        stage.show();
        return stage;
    }
    
    public Stage newStageRoom(String title, int id){
        Stage stage = null;
        FXMLLoader fxmlLoader = new FXMLLoader();
        
        try {
            fxmlLoader.setLocation(getClass().getResource("FXMLRoom.fxml"));
            Scene scene;
            scene = new Scene(fxmlLoader.load());
            stage = new Stage();
            stage.setTitle(title);
            stage.setScene(scene);
            
            FXMLRoomController controller = (FXMLRoomController)fxmlLoader.getController();
            controller.setStage(stage);
            controller.setId(id);
            irc.getRoomChatControllers().put(title, controller);

        } catch (IOException ex) {
            Logger.getLogger(FXMLLobbyController.class.getName()).log(Level.SEVERE, null, ex);
        }  

        FXMLRoomController controller = (FXMLRoomController)fxmlLoader.getController();
        controller.setStage(stage);
        irc.getRoomChatControllers().put(title, controller);
        controller.refreshList();
        stage.setOnCloseRequest( event -> {
            irc.getRoomChatControllers().remove(title);
            irc.leaveRoomRequest(title);
        });
        
        stage.show();
        return stage;
    }
}
