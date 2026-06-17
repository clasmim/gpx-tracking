import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class RunningLaSafor extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        
        //carga fuentes aplicacion
        
        Font.loadFont(getClass().getResourceAsStream(
            "/source/resources/fonts/Montserrat-Regular.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream(
            "/source/resources/fonts/Montserrat-Bold.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream(
            "/source/resources/fonts/Montserrat-SemiBold.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream(
            "/source/resources/fonts/Montserrat-Medium.ttf"), 12);
        Font.loadFont(getClass().getResourceAsStream(
            "/source/resources/fonts/Montserrat-Light.ttf"), 12);

        
        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Inicio.fxml"));
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/source/resources/icons/logo_32x32.png")));
        Scene scene = new Scene(root, 840,570);
        stage.setTitle("Club Running La Safor");
        stage.setScene(scene);
        
        /*bloqueo tamaño pantalla*/
        stage.setMinWidth(840);
        stage.setMinHeight(570);
        stage.setWidth(840);
        stage.setHeight(570);
        
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
    
}
