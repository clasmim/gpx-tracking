package controllers;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import upv.ipc.sportlib.Session;
import upv.ipc.sportlib.SportActivityApp;

// [IA-GENERATED] Este fichero fue generado íntegramente con ayuda de Claude.
// Revisado y adaptado por Chafik Laslouni y Cristina Nácher.

public class HistorialController implements Initializable {

    @FXML private TableView<Session>          sessionTable;
    @FXML private TableColumn<Session,String> colFechaInicio;
    @FXML private TableColumn<Session,String> colHoraInicio;
    @FXML private TableColumn<Session,String> colFechaFin;
    @FXML private TableColumn<Session,String> colHoraFin;
    @FXML private TableColumn<Session,String> colDuracion;
    @FXML private TableColumn<Session,String> colImportadas;
    @FXML private TableColumn<Session,String> colVistas;
    @FXML private TableColumn<Session,String> colAnotaciones;

    @FXML private Label lblTotalSesiones;
    @FXML private Label lblTotalActividades;
    @FXML private Label lblTotalVisualizadas;
    @FXML private Label lblTotalAnotaciones;
    @FXML private Label lblTotalTiempo;

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA  = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colFechaInicio.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getStartTime().format(FECHA)));
        colHoraInicio.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getStartTime().format(HORA)));
        colFechaFin.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getEndTime().format(FECHA)));
        colHoraFin.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getEndTime().format(HORA)));
        colDuracion.setCellValueFactory(c ->
            new SimpleStringProperty(c.getValue().getDuration().toMinutes() + " min"));
        colImportadas.setCellValueFactory(c ->
            new SimpleStringProperty(String.valueOf(c.getValue().getImportedActivities())));
        colVistas.setCellValueFactory(c ->
            new SimpleStringProperty(String.valueOf(c.getValue().getViewedActivities())));
        colAnotaciones.setCellValueFactory(c ->
            new SimpleStringProperty(String.valueOf(c.getValue().getAnnotationsCreated())));
        
        sessionTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void initHistorial(SportActivityApp application) {
        List<Session> sessions = application.getSessionsByUser(application.getCurrentUser());
        if (sessions == null || sessions.isEmpty()) {
            setLbl(lblTotalSesiones,     "0");
            setLbl(lblTotalActividades,  "0");
            setLbl(lblTotalVisualizadas, "0");
            setLbl(lblTotalAnotaciones,  "0");
            setLbl(lblTotalTiempo,       "0 min");
            return;
        }

        sessionTable.getItems().setAll(sessions);

        long totalMins = 0, totalImp = 0, totalVis = 0, totalAnn = 0;
        for (Session s : sessions) {
            totalMins += s.getDuration().toMinutes();
            totalImp  += s.getImportedActivities();
            totalVis  += s.getViewedActivities();
            totalAnn  += s.getAnnotationsCreated();
        }

        setLbl(lblTotalSesiones,     String.valueOf(sessions.size()));
        setLbl(lblTotalActividades,  String.valueOf(totalImp));
        setLbl(lblTotalVisualizadas, String.valueOf(totalVis));
        setLbl(lblTotalAnotaciones,  String.valueOf(totalAnn));
        setLbl(lblTotalTiempo,       totalMins + " min");
    }

    @FXML
    private void cerrar() {
        sessionTable.getScene().getWindow().hide();
    }

    private void setLbl(Label lbl, String text) {
        if (lbl != null) lbl.setText(text);
    }
}
