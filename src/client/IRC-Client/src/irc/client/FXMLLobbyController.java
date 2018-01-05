/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc.client;

import com.sun.javafx.scene.control.skin.LabeledText;
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
    
    @FXML
    private void handleButtonAction(ActionEvent event) {
        System.out.println("You clicked me!");
        label.setText("Hello World!");
    }
    
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
                       Stage stage = newStagePm("FXMLPm.fxml", cName);         
                       //irc.getUserChatControllers().put(cName, stage);
                       stage.setOnCloseRequest( event -> {
                            irc.getUserChatControllers().remove(cName);
                       });
                       stage.show();
               }
            }    
        }
    });
        
    roomList.setOnMouseClicked(new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent e) {
            if(e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2 &&
               (e.getTarget() instanceof LabeledText || ((GridPane) e.getTarget()).getChildren().size() > 0)) {
               int id = userList.getSelectionModel().getSelectedIndex();
               String cName = irc.getLobbyInfo().getRooms().get(id).getName();
               System.out.println("clicked on " + cName);     
               
               
            }    
        }
    });

//        joinButton.setOnAction(new EventHandler<ActionEvent>() {
//            @Override
//            public void handle(ActionEvent event) {
//                System.out.println("Próba wysyłania");
//                OutputStream os = null;
//                try {
//                    Socket socket = new Socket("localhost", irc.getClientInfo().getMsgPort());
//                    os = socket.getOutputStream();
//                    String m = "wiadomosc proba";
//                    IRCMessage msg = new IRCMessage(1, "Wiadomosc", irc.getClientInfo().getNickname(),
//                        irc.getClientInfo().getNickname(), "data");
//                    os.write(msg.getByte());
//                    socket.close();
//                } catch (IOException ex) {
//                    Logger.getLogger(IRCSingleton.class.getName()).log(Level.SEVERE, null, ex);
//                }
//            }
//        });
                
        LobbyListener ll = new LobbyListener(true);
        Thread thread = new Thread(ll);
        thread.start();
        
        MessageListener ml = new MessageListener(true);
        Thread thread2 = new Thread(ml);
        thread2.start();
    }    

    public ListView getUserList() {
        return userList;
    }

    public ListView getRoomList() {
        return roomList;
    }
    
    public Stage newStagePm(String fxmlfile, String title){
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
}
