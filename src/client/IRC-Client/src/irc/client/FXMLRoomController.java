/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package irc.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author marcin
 */
public class FXMLRoomController implements Initializable {
    
   private IRCSingleton irc = IRCSingleton.getInstance();
    
    private int id=0;
    @FXML
    private Button button;
    @FXML
    private TextField msgField;
    @FXML
    private TextArea msgArea;
    @FXML
    private ListView userList;
    
    private Stage stage;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        button.setOnAction(new EventHandler<ActionEvent>() {
        @Override
        public void handle(ActionEvent event) {
            sendMsg();
        }
    });
        refreshList();
    }   

    @FXML
    private void sendMsg(){
                System.out.println("[client]: Wysyłanie...");
        OutputStream os = null;
        try {
            Stage stage = (Stage) button.getScene().getWindow();

            Socket socket = new Socket(irc.getServerInfo().getName(), irc.getClientInfo().getMsgPort());
            os = socket.getOutputStream();
            String m = msgField.getText();
            System.out.println(stage.getTitle()+".");
            IRCMessage msg = new IRCMessage(1, m, irc.getClientInfo().getNickname(),
                stage.getTitle(), "data", "15");
            os.write(msg.getByte());
            socket.close();

            msgField.setText("");
            msgField.requestFocus();

            //TODO server powinien wyslac tez do mnie i dopiero wtedy to odczytac
            msgArea.appendText("\n" + irc.getClientInfo().getNickname() + ": " + m);

        } catch (IOException ex) {
            Logger.getLogger(IRCSingleton.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public TextArea getMsgArea() {
        return msgArea;
    }   

    public Stage getStage() {
        return stage;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }  

    public ListView getUserList() {
        return userList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
    
    public void refreshList(){
        userList.setItems(FXCollections.observableArrayList(irc.getLobbyInfo().getRooms().get(id).getUserNames()));
    }
 
}
