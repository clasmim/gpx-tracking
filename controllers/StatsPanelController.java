package controllers;

import java.net.URL;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import upv.ipc.sportlib.Activity;

// [IA-GENERATED] Este fichero fue generado íntegramente con ayuda de Claude.
// Revisado y adaptado por Chafik Laslouni y Cristina Nácher.

/**
 * Right-side statistics panel.
 *
 * Called by DashboardController whenever the user selects an activity
 * in the left panel.  Shows name, date and all computed stats.
 */
public class StatsPanelController implements Initializable {

    @FXML private Label lblNombreActividad;
    @FXML private Label lblFecha;
    @FXML private Label lblDistancia;
    @FXML private Label lblDuracion;
    @FXML private Label lblVelocidad;
    @FXML private Label lblRitmo;
    @FXML private Label lblDesnivelPos;
    @FXML private Label lblDesnivelNeg;
    @FXML private Label lblAltMin;
    @FXML private Label lblAltMax;

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy  HH:mm");

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Labels start with placeholder dashes — nothing else to do here.
    }

    // ── Public API ───────────────────────────────────────────────

    /** Populate all labels with the selected activity's data. */
    public void mostrarEstadisticas(Activity act) {
        if (act == null) {
            limpiar();
            return;
        }
        set(lblNombreActividad, act.getName());
        set(lblFecha, act.getStartTime() != null
                ? act.getStartTime().format(FMT) : "—");
        set(lblDistancia,  String.format("%.2f", act.getTotalDistance() / 1000.0));
        set(lblDuracion,   formatDuration(act.getDuration()));
        set(lblVelocidad,  String.format("%.1f", act.getAverageSpeed()));
        set(lblRitmo,      String.format("%.1f", act.getAveragePace()));
        set(lblDesnivelPos, String.format("%.0f", act.getElevationGain()));
        set(lblDesnivelNeg, String.format("%.0f", act.getElevationLoss()));
        set(lblAltMin,     String.format("%.0f", act.getMinElevation()));
        set(lblAltMax,     String.format("%.0f", act.getMaxElevation()));
    }

    /** Reset all labels to placeholder dashes. */
    public void limpiar() {
        set(lblNombreActividad, "—");
        set(lblFecha,     "—");
        set(lblDistancia, "—");
        set(lblDuracion,  "—");
        set(lblVelocidad, "—");
        set(lblRitmo,     "—");
        set(lblDesnivelPos, "—");
        set(lblDesnivelNeg, "—");
        set(lblAltMin,    "—");
        set(lblAltMax,    "—");
    }

    // ── Helpers ──────────────────────────────────────────────────

    private void set(Label lbl, String text) {
        if (lbl != null) lbl.setText(text);
    }

    private String formatDuration(Duration d) {
        if (d == null) return "—";
        long h = d.toHours();
        long m = d.toMinutesPart();
        long s = d.toSecondsPart();
        return String.format("%d:%02d:%02d", h, m, s);
    }
}
