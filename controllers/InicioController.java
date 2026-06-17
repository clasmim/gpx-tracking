package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import upv.ipc.sportlib.SportActivityApp;

/**
 * FXML Controller class
 *
 * @author crinacba & clasmim
 */
public class InicioController implements Initializable {
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    public SportActivityApp application = SportActivityApp.getInstance();

    @FXML
    private void irLogin(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
        Parent root = loader.load();
        
        LoginController regClass = loader.getController();
        regClass.initLogin(application);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 840,570);


        stage.setScene(scene);
        stage.setTitle("Club Running La Safor");
        stage.show();  
    }

    @FXML
    private void irRegister(MouseEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Register.fxml"));
        Parent root = loader.load();

        RegisterController regClass = loader.getController();
        regClass.initRegister(application);

        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root);
        


        stage.setScene(scene);
        stage.setTitle("Club Running La Safor");
        stage.setWidth(850);
        stage.setHeight(680);
        stage.setResizable(false);   
        stage.show();
    }
}
