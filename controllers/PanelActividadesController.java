package controllers;

import java.io.File;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

// [IA-GENERATED] Este fichero fue generado íntegramente con ayuda de Claude.
// Revisado y adaptado por Chafik Laslouni y Cristina Nácher.

public class PanelActividadesController implements Initializable {

    @FXML private ListView<Activity> actList;

    public SportActivityApp app;
    private ObservableList<Activity> datos;
    private Consumer<Activity> onActivitySelected;

    // Las imágenes se cargan una sola vez a nivel de controlador
    private Image iconTrashNormal;
    private Image iconTrashHover;
    private Image iconEditNormal;
    private Image iconEditHover;

    private class ActivityListCell extends ListCell<Activity> {
        private static final DateTimeFormatter FMT =
                DateTimeFormatter.ofPattern("dd/MM/yyyy");

        @Override
        protected void updateItem(Activity act, boolean empty) {
            super.updateItem(act, empty);
            if (empty || act == null) {
                setGraphic(null);
                setText(null);
                return;
            }

            // ── Info ────────────────────────────────────────────
            VBox info = new VBox(2);
            Label nombre = new Label(act.getName());
            nombre.getStyleClass().add("lbl-act-nombre");

            Label lblFecha = new Label(act.getStartTime() != null
                    ? act.getStartTime().format(FMT) : "Sin fecha");
            lblFecha.getStyleClass().add("lbl-act-fecha");

            Label dist = new Label(String.format("%.2f km",
                    act.getTotalDistance() / 1000.0));
            dist.getStyleClass().add("lbl-act-dist");

            info.getChildren().addAll(nombre, lblFecha, dist);

            // ── Icono editar — nueva instancia por celda ────────
            ImageView editIcon = new ImageView(iconEditNormal);
            editIcon.setFitWidth(18);
            editIcon.setFitHeight(18);
            editIcon.setPreserveRatio(true);
            Tooltip.install(editIcon, new Tooltip("Renombrar actividad"));

            editIcon.setOnMouseEntered(e -> editIcon.setImage(iconEditHover));
            editIcon.setOnMouseExited(e  -> editIcon.setImage(iconEditNormal));
            editIcon.setOnMouseClicked(e -> {
                if (act == null) return;
                TextInputDialog dialog = new TextInputDialog(act.getName());
                dialog.setTitle("Renombrar actividad");
                dialog.setHeaderText("Cambia el nombre de la actividad");
                dialog.setContentText("Nuevo nombre:");
                dialog.showAndWait().ifPresent(nuevoNombre -> {
                    if (!nuevoNombre.trim().isEmpty()) {
                        app.renameActivity(act, nuevoNombre.trim());
                        act.setName(nuevoNombre.trim());
                        actList.refresh();
                    }
                });
                e.consume();
            });

            // ── Icono papelera — nueva instancia por celda ──────
            ImageView trashIcon = new ImageView(iconTrashNormal);
            trashIcon.setFitWidth(18);
            trashIcon.setFitHeight(18);
            trashIcon.setPreserveRatio(true);
            Tooltip.install(trashIcon, new Tooltip("Eliminar actividad"));

            trashIcon.setOnMouseEntered(e -> trashIcon.setImage(iconTrashHover));
            trashIcon.setOnMouseExited(e  -> trashIcon.setImage(iconTrashNormal));
            trashIcon.setOnMouseClicked(e -> {
                if (act == null) return;
                javafx.scene.control.Alert confirm = new javafx.scene.control.Alert(
                        javafx.scene.control.Alert.AlertType.CONFIRMATION);
                confirm.setTitle("Borrar actividad");
                confirm.setHeaderText("¿Eliminar \"" + act.getName() + "\"?");
                confirm.setContentText("Se eliminarán también todas sus anotaciones. No se puede deshacer.");
                confirm.showAndWait().ifPresent(response -> {
                    if (response == javafx.scene.control.ButtonType.OK) {
                        app.removeActivity(act);
                        datos.remove(act);
                        if (onActivitySelected != null) {
                            Activity siguiente = datos.isEmpty() ? null : datos.get(0);
                            onActivitySelected.accept(siguiente);
                            if (siguiente != null)
                                actList.getSelectionModel().select(siguiente);
                        }
                    }
                });
                e.consume();
            });

            // ── Fila completa ────────────────────────────────────
            HBox iconos = new HBox(10);
            iconos.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
            iconos.getChildren().addAll(editIcon, trashIcon);

            HBox fila = new HBox();
            fila.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            HBox.setHgrow(info, javafx.scene.layout.Priority.ALWAYS);
            fila.getChildren().addAll(info, iconos);

            setGraphic(fila);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cargar imágenes una sola vez
        iconTrashNormal = new Image(getClass().getResourceAsStream("/source/resources/icons/trash.png"));
        iconTrashHover  = new Image(getClass().getResourceAsStream("/source/resources/icons/trash_hover.png"));
        iconEditNormal  = new Image(getClass().getResourceAsStream("/source/resources/icons/edit.png"));
        iconEditHover   = new Image(getClass().getResourceAsStream("/source/resources/icons/edit_hover.png"));

        datos = FXCollections.observableArrayList();
        actList.setItems(datos);
        actList.setCellFactory(c -> new ActivityListCell());

        actList.getSelectionModel().selectedItemProperty().addListener(
                (obs, oldVal, newVal) -> {
                    if (newVal != null && onActivitySelected != null) {
                        onActivitySelected.accept(newVal);
                    }
                });
    }

    public void initPanel(SportActivityApp application) {
        app = application;
        List<Activity> existing = app.getUserActivities();
        if (existing != null) {
            datos.addAll(existing);
        }
    }

    public void setOnActivitySelected(Consumer<Activity> callback) {
        this.onActivitySelected = callback;
    }

    @FXML
    private void importarGPX(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar fichero GPX");
        fc.setInitialDirectory(new File("."));
        fc.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("GPX", "*.gpx"));

        File gpx = fc.showOpenDialog(actList.getScene().getWindow());
        if (gpx == null) return;

        Activity act = app.importActivity(gpx);
        if (act != null) {
            String nombreDefault = gpx.getName().replace(".gpx", "").replace("_", " ");

            TextInputDialog dialog = new TextInputDialog(nombreDefault);
            dialog.setTitle("Nombre de la actividad");
            dialog.setHeaderText("Ponle un nombre a la actividad");
            dialog.setContentText("Nombre:");
            dialog.showAndWait().ifPresent(nombre -> {
                if (!nombre.trim().isEmpty()) {
                    app.renameActivity(act, nombre.trim());
                }
            });

            datos.add(0, act);
            actList.getSelectionModel().selectFirst();
        }
    }
}