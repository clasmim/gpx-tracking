package controllers;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import upv.ipc.sportlib.SportActivityApp;

public class DashboardController implements Initializable {

    public SportActivityApp app;

    @FXML private ToolbarDashboardController toolbarDashboardController;
    @FXML private PanelActividadesController panelActividadesController;
    @FXML private MapaConGraficaController   mapaConGraficaController;
    @FXML private StatsPanelController       statsPanelController;

    public void initDash(SportActivityApp application) {
        app = application;
        toolbarDashboardController.initToolbar(app);
        panelActividadesController.initPanel(app);
        mapaConGraficaController.initMapa(app);

        panelActividadesController.setOnActivitySelected(activity -> {
            mapaConGraficaController.mostrarActividad(activity);
            statsPanelController.mostrarEstadisticas(activity);
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {}
}
