package controllers;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import upv.ipc.sportlib.SportActivityApp;

public class LoginController implements Initializable {

    @FXML private TextField     nicknameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField     passwordVisible;
    @FXML private ImageView     iconoOjo;
    @FXML private Label         errorNick;
    @FXML private Label         errorPassword;

    public SportActivityApp application;
    private boolean passwordMostrada = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Limpiar errores al escribir
        nicknameField.textProperty().addListener((obs, ov, nv) -> {
            ocultarError(errorNick, nicknameField);
        });
        passwordField.textProperty().addListener((obs, ov, nv) -> {
            passwordVisible.setText(nv);
            ocultarError(errorPassword, passwordField);
        });
        passwordVisible.textProperty().addListener((obs, ov, nv) -> {
            passwordField.setText(nv);
            ocultarError(errorPassword, passwordField);
        });
    }

    public void initLogin(SportActivityApp app) {
        application = app;
    }
    
    // [IA-GENERATED] Este método fue generado con ayuda de Claude.
    // ── Botón ojo ────────────────────────────────────────────────
    @FXML
    private void togglePassword(ActionEvent event) {
        passwordMostrada = !passwordMostrada;
        if (passwordMostrada) {
            passwordVisible.setText(passwordField.getText());
            passwordVisible.setVisible(true);
            passwordVisible.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            iconoOjo.setImage(new Image(
                getClass().getResourceAsStream("/source/resources/icons/eye_open.png")));
        } else {
            passwordField.setText(passwordVisible.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            iconoOjo.setImage(new Image(
                getClass().getResourceAsStream("/source/resources/icons/eye_closed.png")));
        }
    }

    // ── Enviar ───────────────────────────────────────────────────
    @FXML
    private void submitAction(ActionEvent event) throws IOException {
        ocultarError(errorNick, nicknameField);
        ocultarError(errorPassword, passwordField);

        String nick = nicknameField.getText().trim();
        String pass = passwordMostrada
            ? passwordVisible.getText()
            : passwordField.getText();

        boolean hayError = false;
        if (nick.isEmpty()) {
            mostrarError(errorNick, nicknameField, "El nombre de usuario es obligatorio.");
            hayError = true;
        }
        if (pass.isEmpty()) {
            mostrarError(errorPassword, passwordField, "La contraseña es obligatoria.");
            hayError = true;
        }
        if (hayError) return;

        if (application.login(nick, pass)) {
            launchMain();
        } else {
            mostrarError(errorNick, nicknameField, "Usuario o contraseña incorrectos.");
            mostrarError(errorPassword, passwordField, "Usuario o contraseña incorrectos.");
            if (passwordMostrada) {
                passwordVisible.clear();
                passwordVisible.requestFocus();
            } else {
                passwordField.clear();
                passwordField.requestFocus();
            }
        }
    }

    // ── Volver ───────────────────────────────────────────────────
    @FXML
    private void volverInicio(ActionEvent event) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Inicio.fxml"));
        Parent root = loader.load();
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene scene = new Scene(root, 840, 570);
        stage.setScene(scene);
        stage.setTitle("Club Running La Safor");
        stage.setMinWidth(840);
        stage.setMinHeight(570);
        stage.setWidth(840);
        stage.setHeight(570);
        stage.show();
    }

    // ── Launch Dashboard ─────────────────────────────────────────
    private void launchMain() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Dashboard.fxml"));
        Parent root = loader.load();
        DashboardController mainClass = loader.getController();
        mainClass.initDash(application);
        Stage stage = (Stage) nicknameField.getScene().getWindow();
        Scene scene = new Scene(root, 1400, 900);
        stage.setScene(scene);
        stage.setTitle("Club Running La Safor");
        stage.setMinWidth(1400);
        stage.setMinHeight(900);
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.show();
    }

    // ── Helpers ──────────────────────────────────────────────────
    private void mostrarError(Label lbl, javafx.scene.control.Control campo, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);
        campo.getStyleClass().remove("campo-texto-error");
        campo.getStyleClass().add("campo-texto-error");
    }

    private void ocultarError(Label lbl, javafx.scene.control.Control campo) {
        lbl.setVisible(false);
        lbl.setManaged(false);
        campo.getStyleClass().remove("campo-texto-error");
    }
}
