package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Bounds;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

// [IA-GENERATED] Este fichero fue generado íntegramente con ayuda de Claude.
// Revisado y adaptado por Chafik Laslouni y Cristina Nácher.

public class ToolbarDashboardController implements Initializable {

    @FXML private Label     lblUsuario;
    @FXML private ImageView imgAvatar;
    @FXML private Label     lblIniciales;
    @FXML private Circle    avatarCircle;
    @FXML private StackPane profileStack;

    private ContextMenu menu;
    public SportActivityApp app;

    @Override
    public void initialize(URL url, ResourceBundle rb) {}
    
    
    public void initToolbar(SportActivityApp application) {
        app = application;
        User user = app.getCurrentUser();
        if (user == null) return;

        if (lblUsuario != null) lblUsuario.setText(user.getNickName());

        Image avatar = user.getAvatar();
        if (avatar != null && imgAvatar != null) {
            imgAvatar.setImage(avatar);
            imgAvatar.setVisible(true);
            imgAvatar.setManaged(true);
            Circle clip = new Circle(18, 18, 18);
            imgAvatar.setClip(clip);
            if (lblIniciales != null) {
                lblIniciales.setVisible(false);
                lblIniciales.setManaged(false);
            }
        } else if (lblIniciales != null) {
            String nick = user.getNickName();
            lblIniciales.setText(nick.length() >= 2
                    ? nick.substring(0, 2).toUpperCase()
                    : nick.toUpperCase());
        }

        menu = new ContextMenu();
        MenuItem mod    = new MenuItem("Editar perfil");
        MenuItem logout = new MenuItem("Cerrar sesión");
        menu.getItems().addAll(mod, logout);

        mod.setOnAction(e -> {
            try { modificarPerfil(); } catch (IOException ex) { ex.printStackTrace(); }
        });
        logout.setOnAction(e -> {
            try { cerrarSesion(); } catch (IOException ex) { ex.printStackTrace(); }
        });
    }

    @FXML
    private void acumuladoAction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/Acumulado.fxml"));
        Parent root = loader.load();
        AcumuladoController ctrl = loader.getController();
        ctrl.initAcumulado(app);

        Stage stage = new Stage();
        stage.setScene(new Scene(root, 460, 620));
        stage.setResizable(false);
        stage.setTitle("Acumulado de actividades");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    @FXML
    private void historialAction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/Historial.fxml"));
        Parent root = loader.load();
        HistorialController hist = loader.getController();
        hist.initHistorial(app);

        Stage stage = new Stage();
        stage.setScene(new Scene(root, 760, 620));
        stage.setResizable(false);
        stage.setTitle("Historial de sesiones");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    @FXML
    private void mapasAction(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/GestionMapas.fxml"));
        Parent root = loader.load();
        GestionMapasController ctrl = loader.getController();
        ctrl.initGestion(app);
        Stage stage = new Stage();
        stage.setScene(new Scene(root, 760, 620));
        stage.setResizable(false);
        stage.setTitle("Gestión de mapas");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
    }

    @FXML
    private void about(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/Ayuda.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Ayuda — Club Running La Safor");
            stage.setScene(new Scene(root));
            stage.setResizable(false);
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.getIcons().add(new Image(
                getClass().getResourceAsStream("/source/resources/logo/logo_CRLS_lima.png")));
            stage.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void logoutDirect(ActionEvent event) throws IOException {
        cerrarSesion();
    }

    @FXML
    private void showMenu(MouseEvent event) {
        if (profileStack == null) return;
        Bounds bounds = profileStack.localToScreen(profileStack.getBoundsInLocal());
        menu.show(profileStack, bounds.getMinX(), bounds.getMaxY());
    }

    private void modificarPerfil() throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/Modificar.fxml"));
        Parent root = loader.load();
        ModificarController modify = loader.getController();
        modify.initModificar(app);

        Stage stage = new Stage();
        stage.setScene(new Scene(root));
        stage.setTitle("Editar perfil");
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.showAndWait();
        initToolbar(app);
    }

    private void cerrarSesion() throws IOException {
        app.logout();
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/Inicio.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) (profileStack != null
                ? profileStack.getScene().getWindow()
                : lblUsuario.getScene().getWindow());
        Scene scene = new Scene(root, 840, 570);
        stage.setScene(scene);
        stage.setTitle("Club de Running La Safor");
        stage.setMinWidth(850);
        stage.setMinHeight(680);
        stage.setWidth(850);
        stage.setHeight(680);
        stage.setMaximized(false);
        stage.setResizable(true);
        stage.show();
    }
}

