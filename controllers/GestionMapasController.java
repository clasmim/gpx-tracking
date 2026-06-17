package controllers;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;

// [IA-GENERATED] Este fichero fue generado íntegramente con ayuda de Claude y ChatGPT.
// Revisado y adaptado por Chafik Laslouni y Cristina Nácher.

public class GestionMapasController implements Initializable {

    @FXML private TableView<MapRegion>          tablaMapas;
    @FXML private TableColumn<MapRegion,String> colNombre;
    @FXML private TableColumn<MapRegion,String> colLatMin;
    @FXML private TableColumn<MapRegion,String> colLatMax;
    @FXML private TableColumn<MapRegion,String> colLonMin;
    @FXML private TableColumn<MapRegion,String> colLonMax;
    @FXML private TableColumn<MapRegion,String> colImagen;

    @FXML private TextField txtNombre;
    @FXML private TextField txtLatMin;
    @FXML private TextField txtLatMax;
    @FXML private TextField txtLonMin;
    @FXML private TextField txtLonMax;
    @FXML private TextField txtImagen;

    @FXML private Label  lblMensaje;
    @FXML private Button btnBorrar;

    private SportActivityApp app;
    private File imagenSeleccionada;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colNombre.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getName()));
        colLatMin.setCellValueFactory(c ->
            new SimpleStringProperty(String.format("%.6f", c.getValue().getLatMin())));
        colLatMax.setCellValueFactory(c ->
            new SimpleStringProperty(String.format("%.6f", c.getValue().getLatMax())));
        colLonMin.setCellValueFactory(c ->
            new SimpleStringProperty(String.format("%.6f", c.getValue().getLonMin())));
        colLonMax.setCellValueFactory(c ->
            new SimpleStringProperty(String.format("%.6f", c.getValue().getLonMax())));
        colImagen.setCellValueFactory(c ->
            new SimpleStringProperty(new File(c.getValue().getImagePath()).getName()));

        // Botón borrar solo activo si hay selección
        btnBorrar.setDisable(true);
        tablaMapas.getSelectionModel().selectedItemProperty().addListener(
            (obs, old, sel) -> btnBorrar.setDisable(sel == null));
        tablaMapas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void initGestion(SportActivityApp application) {
        this.app = application;
        cargarTabla();
    }

    private void cargarTabla() {
        List<MapRegion> mapas = app.getMapRegions();
        tablaMapas.setItems(
            javafx.collections.FXCollections.observableArrayList(mapas));
    }

    @FXML
    private void seleccionarImagen() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar imagen del mapa");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Imágenes JPG", "*.jpg", "*.jpeg"));
        File f = fc.showOpenDialog(txtImagen.getScene().getWindow());
        if (f != null) {
            imagenSeleccionada = f;
            txtImagen.setText(f.getAbsolutePath());
        }
    }

    @FXML
    private void añadirMapa() {
        lblMensaje.setText("");

        // Validar campos vacíos
        if (txtNombre.getText().trim().isEmpty() ||
            txtLatMin.getText().trim().isEmpty() ||
            txtLatMax.getText().trim().isEmpty() ||
            txtLonMin.getText().trim().isEmpty() ||
            txtLonMax.getText().trim().isEmpty() ||
            imagenSeleccionada == null) {
            mostrarMensaje("⚠ Rellena todos los campos y selecciona una imagen.", false);
            return;
        }

        // Parsear coordenadas
        double latMin, latMax, lonMin, lonMax;
        try {
            latMin = Double.parseDouble(txtLatMin.getText().trim().replace(",", "."));
            latMax = Double.parseDouble(txtLatMax.getText().trim().replace(",", "."));
            lonMin = Double.parseDouble(txtLonMin.getText().trim().replace(",", "."));
            lonMax = Double.parseDouble(txtLonMax.getText().trim().replace(",", "."));
        } catch (NumberFormatException e) {
            mostrarMensaje("⚠ Las coordenadas deben ser números con punto decimal. Ej: 39.537940", false);
            return;
        }

        // Validar rango lógico
        if (latMin >= latMax || lonMin >= lonMax) {
            mostrarMensaje("⚠ La latitud/longitud mínima debe ser menor que la máxima.", false);
            return;
        }

        // Añadir mapa
        MapRegion nuevo = app.addMapRegion(
            txtNombre.getText().trim(),
            imagenSeleccionada,
            latMin, latMax, lonMin, lonMax);

        if (nuevo != null) {
            limpiarFormulario();
            cargarTabla();
            Alert aviso = new Alert(Alert.AlertType.INFORMATION);
            aviso.setTitle("Mapa añadido");
            aviso.setHeaderText("El mapa \"" + nuevo.getName() + "\" se ha añadido correctamente.");
            aviso.setContentText("Ya puedes usarlo para visualizar actividades en esa región.");
            aviso.showAndWait();
        } else {
            mostrarMensaje("✖ Error al añadir el mapa. Comprueba que el fichero es válido.", false);
        }
    }

    @FXML
    private void borrarMapa() {
        MapRegion sel = tablaMapas.getSelectionModel().getSelectedItem();
        if (sel == null) return;

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Borrar mapa");
        confirm.setHeaderText("¿Borrar el mapa \"" + sel.getName() + "\"?");
        confirm.setContentText("Esta acción no se puede deshacer.");
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.OK) {
                boolean ok = app.removeMapRegion(sel);
                if (ok) {
                    cargarTabla();
                    Alert aviso = new Alert(Alert.AlertType.INFORMATION);
                    aviso.setTitle("Mapa eliminado");
                    aviso.setHeaderText("El mapa \"" + sel.getName() + "\" se ha eliminado correctamente.");
                    aviso.setContentText("No tenía actividades asociadas.");
                    aviso.showAndWait();
                } else {
                    Alert aviso = new Alert(Alert.AlertType.WARNING);
                    aviso.setTitle("No se puede eliminar");
                    aviso.setHeaderText("El mapa \"" + sel.getName() + "\" tiene actividades asociadas.");
                    aviso.setContentText("Para eliminar este mapa, primero borra todas las actividades que lo usan.");
                    aviso.showAndWait();
                }
            }
        });
    }

    @FXML
    private void cerrar() {
        tablaMapas.getScene().getWindow().hide();
    }

    private void limpiarFormulario() {
        txtNombre.clear();
        txtLatMin.clear();
        txtLatMax.clear();
        txtLonMin.clear();
        txtLonMax.clear();
        txtImagen.clear();
        imagenSeleccionada = null;
    }

    private void mostrarMensaje(String texto, boolean exito) {
        lblMensaje.setText(texto);
        lblMensaje.setTextFill(exito ? Color.web("#33691E") : Color.web("#CC4444"));
    }
}   
