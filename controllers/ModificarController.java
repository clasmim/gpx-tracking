package controllers;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.stage.FileChooser;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.User;

public class ModificarController implements Initializable {

    public SportActivityApp application;

    @FXML private TextField  nicknameField;
    @FXML private PasswordField passwordField;
    @FXML private TextField  passwordVisible;
    @FXML private TextField  emailField;
    @FXML private DatePicker datePicker;
    @FXML private ImageView  iconoOjo;
    @FXML private ImageView  avatarPreview;
    @FXML private Circle     avatarCircle;
    @FXML private Label      lblAvatarIniciales;
    @FXML private Label      lblAvatar;
    @FXML private Label      errorEmail;
    @FXML private Label      errorPassword;
    @FXML private Label      errorFecha;

    private String  newAvatarPath   = null;
    private boolean passwordMostrada = false;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Email
        emailField.focusedProperty().addListener((obs, tenia, tiene) -> {
            if (!tiene) validarEmail();
        });
        emailField.textProperty().addListener((obs, ov, nv) -> {
            if (errorEmail.isVisible()) validarEmail();
        });

        // Contraseña — solo validar si el campo no está vacío
        passwordField.focusedProperty().addListener((obs, tenia, tiene) -> {
            if (!tiene && !passwordField.getText().isEmpty()) validarPassword();
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

    public void initModificar(SportActivityApp app) {
        application = app;
        User user = application.getCurrentUser();

        nicknameField.setText(user.getNickName());
        nicknameField.setDisable(true);

        emailField.setText(user.getEmail());
        datePicker.setValue(user.getBirthDate());

        // Contraseña vacía — el usuario solo la rellena si quiere cambiarla
        passwordField.setText("");

        // Cargar avatar si existe
        Image av = user.getAvatar();
        if (av != null) {
            avatarPreview.setImage(av);
            Circle clip = new Circle(32, 32, 32);
            avatarPreview.setClip(clip);
            avatarPreview.setVisible(true);
            avatarPreview.setManaged(true);
            lblAvatarIniciales.setVisible(false);
            lblAvatarIniciales.setManaged(false);
        }
    }

    @FXML
    private void imagePick(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setInitialDirectory(new File("."));
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes", "*.jpg", "*.png", "*.jpeg"));

        File imgFile = fc.showOpenDialog(nicknameField.getScene().getWindow());
        if (imgFile != null) {
            newAvatarPath = imgFile.getAbsolutePath();
            lblAvatar.setText(imgFile.getName());
            lblAvatar.setVisible(true);
            lblAvatar.setManaged(true);

            Image img = new Image(imgFile.toURI().toString());
            avatarPreview.setImage(img);
            Circle clip = new Circle(32, 32, 32);
            avatarPreview.setClip(clip);
            avatarPreview.setVisible(true);
            avatarPreview.setManaged(true);
            lblAvatarIniciales.setVisible(false);
            lblAvatarIniciales.setManaged(false);
        }
    }
    
    
    // [IA-GENERATED] Este método fue depurado con ayuda de Claude.
    @FXML
    private void sumbitAction(ActionEvent event) {
        // Validar campos obligatorios
        boolean ok = validarEmail() & validarFecha();

        // Validar contraseña solo si se ha rellenado
        String pass = passwordMostrada
            ? passwordVisible.getText().trim()
            : passwordField.getText().trim();

        if (!pass.isEmpty() && !User.checkPassword(pass)) {
            mostrarError(errorPassword, passwordField,
                "Mín. 8 caracteres con mayúscula, minúscula, dígito y símbolo (!@#$%&*()-+=).");
            ok = false;
        }

        if (!ok) return;

        // Si contraseña vacía, mantener la actual
        User user = application.getCurrentUser();
        if (pass.isEmpty()) {
            pass = user.getPassword();
        }

        String avatarPath = (newAvatarPath != null)
                ? newAvatarPath
                : user.getAvatarPath();

        boolean actualizado = application.updateCurrentUser(
                emailField.getText().trim(),
                pass,
                datePicker.getValue(),
                avatarPath);

        if (actualizado) {
            javafx.scene.control.Alert aviso = new javafx.scene.control.Alert(
            javafx.scene.control.Alert.AlertType.INFORMATION);
            aviso.setTitle("Perfil actualizado");
            aviso.setHeaderText("Los cambios se han guardado correctamente.");
            aviso.setContentText("Tu perfil ha sido actualizado.");
            aviso.showAndWait();
            nicknameField.getScene().getWindow().hide();
       
        } else {
            mostrarError(errorEmail, emailField,
                "No se han podido guardar los cambios. Comprueba los datos.");
        }
    }

    @FXML
    private void cancelAction(ActionEvent event) {
        nicknameField.getScene().getWindow().hide();
    }
    
    
    // [IA-GENERATED] Este método fue generado con ayuda de Claude.
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
                getClass().getResourceAsStream("/resources/icons/eye_open.png")));
        } else {
            passwordField.setText(passwordVisible.getText());
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            passwordVisible.setVisible(false);
            passwordVisible.setManaged(false);
            iconoOjo.setImage(new Image(
                getClass().getResourceAsStream("/resources/icons/eye_closed.png")));
        }
    }

    // ── Validaciones ─────────────────────────────────────────────
    private boolean validarEmail() {
        String email = emailField.getText().trim();
        if (email.isEmpty()) {
            mostrarError(errorEmail, emailField, "El correo electrónico es obligatorio.");
            return false;
        }
        if (!User.checkEmail(email)) {
            mostrarError(errorEmail, emailField, "Formato inválido. Ejemplo: usuario@dominio.com");
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
            ocultarError(errorPassword, passwordField);
            return true;
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

    // ── Helpers ──────────────────────────────────────────────────
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
