package controllers;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class RegisterController implements Initializable {

    // ── Campos ──────────────────────────────────────────────────
    @FXML private TextField     nicknameField;
    @FXML private TextField     emailField;
    @FXML private PasswordField passwordField;
    @FXML private TextField     passwordVisible;
    @FXML private DatePicker    datePicker;
    @FXML private ImageView     iconoOjo;
    @FXML private ImageView     avatarPreview;
    @FXML private Circle        avatarCircle;
    @FXML private Label         lblAvatarIniciales;
    @FXML private Label         lblAvatar;

    // ── Labels de error ─────────────────────────────────────────
    @FXML private Label errorNick;
    @FXML private Label errorEmail;
    @FXML private Label errorPassword;
    @FXML private Label errorFecha;

    public SportActivityApp application = SportActivityApp.getInstance();
    private String rutaAvatar = null;
    private boolean passwordMostrada = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        ocultarErroresIniciales();

        // Nick
        nicknameField.focusedProperty().addListener((obs, tenia, tiene) -> {
            if (!tiene) validarNick();
        });
        nicknameField.textProperty().addListener((obs, ov, nv) -> {
            if (errorNick.isVisible()) validarNick();
        });

        // Email
        emailField.focusedProperty().addListener((obs, tenia, tiene) -> {
            if (!tiene) validarEmail();
        });
        emailField.textProperty().addListener((obs, ov, nv) -> {
            if (errorEmail.isVisible()) validarEmail();
        });

        // Contraseña
        passwordField.focusedProperty().addListener((obs, tenia, tiene) -> {
            if (!tiene) validarPassword();
        });
        passwordField.textProperty().addListener((obs, ov, nv) -> {
            passwordVisible.setText(nv);
            if (errorPassword.isVisible()) validarPassword();
        });
        passwordVisible.textProperty().addListener((obs, ov, nv) -> {
            passwordField.setText(nv);
            if (errorPassword.isVisible()) validarPassword();
        });

        // Fecha
        datePicker.valueProperty().addListener((obs, ov, nv) -> validarFecha());
    }

    public void initRegister(SportActivityApp app) {
        application = app;
    }

    private void ocultarErroresIniciales() {
        ocultarError(errorNick, nicknameField);
        ocultarError(errorEmail, emailField);
        ocultarError(errorPassword, passwordField);
        ocultarError(errorFecha, datePicker);
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

    // ── Seleccionar avatar ───────────────────────────────────────
    @FXML
    private void imagePick(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("."));
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.png", "*.jpeg"));

        File imgFile = fc.showOpenDialog(nicknameField.getScene().getWindow());

        if (imgFile != null) {
            rutaAvatar = imgFile.getAbsolutePath();
            lblAvatar.setText(imgFile.getName());

            Image img = new Image(imgFile.toURI().toString());
            avatarPreview.setImage(img);

            // El avatar del FXML mide 64x64, por eso el clip debe ser 32,32,32.
            Circle clip = new Circle(32, 32, 32);
            avatarPreview.setClip(clip);
            avatarPreview.setVisible(true);
            avatarPreview.setManaged(true);

            lblAvatarIniciales.setVisible(false);
            lblAvatarIniciales.setManaged(false);
        }
    }

    // ── Enviar ───────────────────────────────────────────────────
    @FXML
    private void submitAction(ActionEvent event) throws IOException {
        boolean ok = validarNick()
                   & validarEmail()
                   & validarPassword()
                   & validarFecha();

        if (!ok) return;

        String pass = passwordMostrada
            ? passwordVisible.getText()
            : passwordField.getText();

        boolean registrado = application.registerUser(
            nicknameField.getText().trim(),
            emailField.getText().trim(),
            pass,
            datePicker.getValue(),
            rutaAvatar
        );
        
        if (registrado) {
            application.login(nicknameField.getText(), pass);
            launchMain();
        } else {
            mostrarError(errorNick, nicknameField,
                "El nombre de usuario ya está en uso. Elige otro.");
            nicknameField.requestFocus();
            nicknameField.selectAll();
        }
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

    // ── Navegación ───────────────────────────────────────────────
    @FXML
    private void cancelarAction(ActionEvent event) throws IOException {
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

    // ── Validaciones ─────────────────────────────────────────────
    private boolean validarNick() {
        String nick = nicknameField.getText().trim();

        if (nick.isEmpty()) {
            mostrarError(errorNick, nicknameField, "El nombre de usuario es obligatorio.");
            return false;
        }

        if (!User.checkNickName(nick)) {
            mostrarError(errorNick, nicknameField,
                "Entre 6 y 15 caracteres. Solo letras, dígitos, guion o subguion.");
            return false;
        }

        ocultarError(errorNick, nicknameField);
        return true;
    }

    private boolean validarEmail() {
        String email = emailField.getText().trim();

        if (email.isEmpty()) {
            mostrarError(errorEmail, emailField, "El correo electrónico es obligatorio.");
            return false;
        }

        if (!User.checkEmail(email)) {
            mostrarError(errorEmail, emailField,
                "Formato inválido. Ejemplo: usuario@dominio.com");
            return false;
        }

        ocultarError(errorEmail, emailField);
        return true;
    }

    private boolean validarPassword() {
        String pass = passwordMostrada
            ? passwordVisible.getText()
            : passwordField.getText();

        if (pass.isEmpty()) {
            mostrarError(errorPassword, passwordField, "La contraseña es obligatoria.");
            return false;
        }

        if (!User.checkPassword(pass)) {
            mostrarError(errorPassword, passwordField,
                "Mín. 8 caracteres con mayúscula, minúscula, dígito y símbolo (!@#$%&*()-+=).");
            return false;
        }

        ocultarError(errorPassword, passwordField);
        ocultarError(errorPassword, passwordVisible);
        return true;
    }

    private boolean validarFecha() {
        LocalDate fecha = datePicker.getValue();

        if (fecha == null) {
            mostrarError(errorFecha, datePicker, "La fecha de nacimiento es obligatoria.");
            return false;
        }

        if (!User.isOlderThan(fecha, 12)) {
            mostrarError(errorFecha, datePicker, "Debes tener más de 12 años para registrarte.");
            return false;
        }

        ocultarError(errorFecha, datePicker);
        return true;
    }

    // ── Helpers visuales ─────────────────────────────────────────
    private void mostrarError(Label lbl, javafx.scene.control.Control campo, String msg) {
        lbl.setText(msg);
        lbl.setVisible(true);
        lbl.setManaged(true);

        if (campo != null) {
            campo.getStyleClass().remove("campo-texto-error");
            campo.getStyleClass().add("campo-texto-error");
        }
    }

    private void ocultarError(Label lbl, javafx.scene.control.Control campo) {
        lbl.setVisible(false);
        lbl.setManaged(false);

        if (campo != null) {
            campo.getStyleClass().remove("campo-texto-error");
        }
    }
}