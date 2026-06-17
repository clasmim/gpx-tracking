package controllers;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.SportActivityApp;

// [IA-GENERATED] Este fichero fue generado íntegramente con ayuda de Claude y ChatGPT.
// Revisado y adaptado por Chafik Laslouni y Cristina Nácher.

public class AcumuladoController implements Initializable {

    @FXML private ComboBox<String> comboPeriodo;
    @FXML private Label lblNumActividades;
    @FXML private Label lblDistancia;
    @FXML private Label lblTiempo;
    @FXML private Label lblAscenso;
    @FXML private Label lblDescenso;

    private SportActivityApp app;
    private List<Activity> todasActividades;

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    public void initAcumulado(SportActivityApp application) {
        app = application;
        todasActividades = app.getUserActivities();

        // Rellenar combo con los meses que tienen actividades + "Todo"
        comboPeriodo.setItems(FXCollections.observableArrayList(
                "Este mes",
                "Mes anterior",
                "Últimos 3 meses",
                "Todo el año",
                "Todas"
        ));
        comboPeriodo.getSelectionModel().selectFirst();
        calcular("Este mes");
    }

    @FXML
    private void cambiarPeriodo(ActionEvent event) {
        String sel = comboPeriodo.getSelectionModel().getSelectedItem();
        if (sel != null) calcular(sel);
    }

    private void calcular(String periodo) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime desde;

        switch (periodo) {
            case "Este mes" ->
                desde = ahora.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            case "Mes anterior" ->
                desde = ahora.minusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
            case "Últimos 3 meses" ->
                desde = ahora.minusMonths(3);
            case "Todo el año" ->
                desde = ahora.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
            default ->
                desde = LocalDateTime.MIN;
        }

        final LocalDateTime filtroDesde = desde;

        // Filtrar actividades por periodo
        List<Activity> filtradas = todasActividades.stream()
                .filter(a -> a.getStartTime() != null
                        && !a.getStartTime().isBefore(filtroDesde))
                .collect(Collectors.toList());

        // Si es "Mes anterior" también filtrar el fin del mes anterior
        if ("Mes anterior".equals(periodo)) {
            LocalDateTime finMesAnterior = ahora.withDayOfMonth(1)
                    .withHour(0).withMinute(0).withSecond(0);
            filtradas = filtradas.stream()
                    .filter(a -> a.getStartTime().isBefore(finMesAnterior))
                    .collect(Collectors.toList());
        }

        // Calcular acumulados
        int num = filtradas.size();
        double distTotal = filtradas.stream()
                .mapToDouble(Activity::getTotalDistance).sum();
        long segundosTotales = filtradas.stream()
                .filter(a -> a.getDuration() != null)
                .mapToLong(a -> a.getDuration().getSeconds()).sum();
        double ascensoTotal = filtradas.stream()
                .mapToDouble(Activity::getElevationGain).sum();
        double descensoTotal = filtradas.stream()
                .mapToDouble(Activity::getElevationLoss).sum();

        // Mostrar
        set(lblNumActividades, String.valueOf(num));
        set(lblDistancia, String.format("%.2f", distTotal / 1000.0));
        set(lblTiempo, formatDuracion(segundosTotales));
        set(lblAscenso, String.format("%.0f", ascensoTotal));
        set(lblDescenso, String.format("%.0f", descensoTotal));
    }

    @FXML
    private void cerrar(ActionEvent event) {
        Stage stage = (Stage) comboPeriodo.getScene().getWindow();
        stage.close();
    }

    private void set(Label lbl, String text) {
        if (lbl != null) lbl.setText(text);
    }

    private String formatDuracion(long segundos) {
        long h = segundos / 3600;
        long m = (segundos % 3600) / 60;
        long s = segundos % 60;
        return String.format("%d:%02d:%02d", h, m, s);
    }
}
