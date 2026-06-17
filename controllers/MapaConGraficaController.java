package controllers;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Text;
import javafx.util.Duration;
import upv.ipc.sportlib.Activity;
import upv.ipc.sportlib.Annotation;
import upv.ipc.sportlib.AnnotationType;
import upv.ipc.sportlib.GeoPoint;
import upv.ipc.sportlib.MapProjection;
import upv.ipc.sportlib.MapRegion;
import upv.ipc.sportlib.SportActivityApp;
import upv.ipc.sportlib.TrackPoint;
import javafx.scene.chart.NumberAxis;

// [IA-GENERATED] Este fichero fue generado íntegramente con ayuda de Claude y ChatGPT.
// Revisado y adaptado por Chafik Laslouni y Cristina Nácher.

/**
 * Center panel: map + elevation chart.
 *
 * Fixes in this version:
 *  - LINE / CIRCLE annotations now use a real two-click workflow instead of
 *    a hardcoded pixel offset for the second point.
 *  - A preview shape is drawn while the user moves the mouse to the 2nd point.
 *  - The pending-annotation state is cancelled cleanly on right-click or Escape.
 *  - ContextMenu listeners are set once in initialize() to avoid accumulation.
 *  - lblCoords NPE guard added (field may not be injected if absent from FXML).
 */
public class MapaConGraficaController implements Initializable {

    // ── FXML injected ─────────────────────────────────────────────
    @FXML private ScrollPane   mapScrollPane;
    @FXML private Slider       zoomSlider;
    @FXML private Label        lblCoords;       // may be null if not in FXML
    @FXML private AreaChart<Number, Number> elevationChart;
    @FXML private VBox panelBienvenida;

    // ── Map node hierarchy ────────────────────────────────────────
    private Group zoomGroup;
    private Pane  mapPane;

    // ── State ─────────────────────────────────────────────────────
    private SportActivityApp app;
    private Activity         currentActivity;
    private MapProjection    projection;
    private ContextMenu      contextMenu;

    /** Highlight circle that follows the mouse on the elevation chart. */
    private Circle chartHighlight;

    @FXML private ToggleButton btnVelocidad;
    private Polyline routePolyline;
    private final List<javafx.scene.Node> velocidadNodes = new ArrayList<>();

    // ── Two-click annotation state ────────────────────────────────
    /**
     * When non-null the user has already configured a LINE or CIRCLE
     * annotation and we are waiting for the second map click.
     */
    private PendingAnnotation pendingAnnotation = null;

    /** Preview shape shown while waiting for the second click. */
    private javafx.scene.shape.Shape previewShape = null;

    // ─────────────────────────────────────────────────────────────
    //  Inner helper – holds first-click data between clicks
    // ─────────────────────────────────────────────────────────────
    private static class PendingAnnotation {
        final AnnotationType type;
        final double x1, y1;          // map-pane coords of first click
        final String text;
        final Color  color;
        final double strokeWidth;

        PendingAnnotation(AnnotationType type, double x1, double y1,
                          String text, Color color, double strokeWidth) {
            this.type = type;
            this.x1 = x1; this.y1 = y1;
            this.text = text;
            this.color = color;
            this.strokeWidth = strokeWidth;
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Initialise
    // ─────────────────────────────────────────────────────────────
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Zoom slider
        if (zoomSlider != null) {
            zoomSlider.setMin(0.5);
            zoomSlider.setMax(3.0);
            zoomSlider.setValue(1.0);
            zoomSlider.valueProperty().addListener(
                    (obs, o, n) -> applyZoom(n.doubleValue()));
            if (btnVelocidad != null) {
    btnVelocidad.setTooltip(new javafx.scene.control.Tooltip(
        "Activar/desactivar visualización de velocidad sobre el trazado"
    ));
}
        }

        // Build context menu ONCE – handlers assigned per-click in onMapRightClick
        MenuItem miPoint  = new MenuItem("📍 Añadir punto");
        MenuItem miText   = new MenuItem("📝 Añadir texto");
        MenuItem miLine   = new MenuItem("📏 Añadir línea");
        MenuItem miCircle = new MenuItem("⭕ Añadir círculo");
        contextMenu = new ContextMenu(miPoint, miText, miLine, miCircle);

        // Placeholder shown on startup
        buildPlaceholder();
    }

    public void initMapa(SportActivityApp application) {
        this.app = application;
    }

    // ─────────────────────────────────────────────────────────────
    //  Public API – called by DashboardController
    // ─────────────────────────────────────────────────────────────
    public void mostrarActividad(Activity activity) {
        this.currentActivity = activity;
        if (panelBienvenida != null) panelBienvenida.setVisible(false);
        cancelPending();
        velocidadNodes.clear();
        if (btnVelocidad != null) { btnVelocidad.setSelected(false); btnVelocidad.setText("Velocidad"); }

        MapRegion region = activity.getSuggestedMap();
        if (region == null) {
            buildPlaceholder();
            return;
        }

        File imgFile = new File(region.getImagePath());
        buildMap(imgFile, region);
        
        // Only draw route and annotations if map was successfully loaded
        if (projection == null) {
            return;
        }
        
        drawRoute(activity);
        drawAnnotations(activity);
        buildElevationChart(activity);
    }

    // ─────────────────────────────────────────────────────────────
    //  Zoom
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void zoomIn(ActionEvent event) {
        zoomSlider.setValue(Math.min(zoomSlider.getMax(),
                zoomSlider.getValue() + 0.1));
    }

    @FXML
    private void zoomOut(ActionEvent event) {
        zoomSlider.setValue(Math.max(zoomSlider.getMin(),
                zoomSlider.getValue() - 0.1));
    }

    private void applyZoom(double scale) {
        if (zoomGroup == null) return;
        double h = mapScrollPane.getHvalue();
        double v = mapScrollPane.getVvalue();
        zoomGroup.setScaleX(scale);
        zoomGroup.setScaleY(scale);
        mapScrollPane.setHvalue(h);
        mapScrollPane.setVvalue(v);
    }

    // ─────────────────────────────────────────────────────────────
    //  Mouse position label
    // ─────────────────────────────────────────────────────────────
    @FXML
    private void showPosition(MouseEvent event) {
        if (lblCoords == null) return;
        if (projection != null) {
            GeoPoint geo = projection.unproject(event.getX(), event.getY());
            lblCoords.setText(String.format("Lat: %.5f  Lon: %.5f",
                    geo.getLatitude(), geo.getLongitude()));
        } else {
            lblCoords.setText(String.format("X: %d  Y: %d",
                    (int) event.getX(), (int) event.getY()));
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Build map
    // ─────────────────────────────────────────────────────────────
    private void buildPlaceholder() {
        mapPane  = new Pane();
        mapPane.setPrefSize(800, 600);
        Label hint = new Label("Selecciona una actividad para ver el mapa");
        hint.setLayoutX(200);
        hint.setLayoutY(280);
        hint.setStyle("-fx-font-size:16; -fx-text-fill:#777;");
        mapPane.getChildren().add(hint);
        wrapInScrollPane(mapPane);
        projection = null;
    }

    private void buildMap(File imgFile, MapRegion region) {
        if (!imgFile.exists()) {
            buildPlaceholder();
            return;
        }

        Image img = new Image(imgFile.toURI().toString());
        double W  = img.getWidth();
        double H  = img.getHeight();

        projection = new MapProjection(region, W, H);

        mapPane = new Pane();
        mapPane.setPrefSize(W, H);
        mapPane.setMinSize(W, H);
        mapPane.setMaxSize(W, H);

        ImageView iv = new ImageView(img);
        iv.setFitWidth(W);
        iv.setFitHeight(H);
        mapPane.getChildren().add(iv);

        // Chart highlight dot (hidden until hover)
        chartHighlight = new Circle(5, Color.DEEPSKYBLUE);
        chartHighlight.setVisible(false);
        mapPane.getChildren().add(chartHighlight);

        // Mouse-move → coords label + preview shape during pending annotation
        mapPane.setOnMouseMoved(e -> {
            showPosition(e);
            updatePreview(e.getX(), e.getY());
        });

        // Unified click handler for the map pane
        mapPane.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                // Right-click: cancel pending or show context menu
                if (pendingAnnotation != null) {
                    cancelPending();
                } else if (currentActivity != null) {
                    contextMenu.hide();
                    onMapRightClick(e.getX(), e.getY(), e);
                }
            } else if (e.getButton() == MouseButton.PRIMARY
                    && pendingAnnotation != null) {
                // Left-click while waiting for 2nd point
                commitSecondPoint(e.getX(), e.getY());
                e.consume();
            }
        });

        // Escape key cancels pending annotation
        mapPane.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE) {
                cancelPending();
            }
        });

        wrapInScrollPane(mapPane);
        applyZoom(zoomSlider != null ? zoomSlider.getValue() : 1.0);
    }

    private void wrapInScrollPane(Pane content) {
        zoomGroup = new Group();
        Group outer = new Group();
        zoomGroup.getChildren().add(content);
        outer.getChildren().add(zoomGroup);
        if (mapScrollPane != null) {
            mapScrollPane.setContent(outer);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Draw route
    // ─────────────────────────────────────────────────────────────
    private void drawRoute(Activity activity) {
        List<TrackPoint> points = activity.getTrackPoints();
        if (points == null || points.isEmpty()) return;

        Polyline route = new Polyline();
        route.setStroke(Color.DODGERBLUE);
        route.setStrokeWidth(2.5);
        route.setFill(null);
        route.setSmooth(true);

        List<Double> coords = new ArrayList<>();
        for (TrackPoint tp : points) {
            Point2D p = projection.project(tp);
            coords.add(p.getX());
            coords.add(p.getY());
        }
        route.getPoints().addAll(coords);
        routePolyline = route;
        mapPane.getChildren().add(route);

        // Start marker – green
        Point2D startPt = projection.project(activity.getStartPoint());
        Circle startMarker = new Circle(startPt.getX(), startPt.getY(), 7, Color.LIMEGREEN);
        startMarker.setStroke(Color.WHITE);
        startMarker.setStrokeWidth(2);
        mapPane.getChildren().add(startMarker);

        // End marker – red
        Point2D endPt = projection.project(activity.getEndPoint());
        Circle endMarker = new Circle(endPt.getX(), endPt.getY(), 7, Color.RED);
        endMarker.setStroke(Color.WHITE);
        endMarker.setStrokeWidth(2);
        mapPane.getChildren().add(endMarker);
        
        // Centrar el mapa en el punto medio del trazado al cargar
        if (!points.isEmpty()) {
            int midIdx = points.size() / 2;
            Point2D mid = projection.project(points.get(midIdx));

            javafx.application.Platform.runLater(() -> {
                double mapW  = mapPane.getWidth()  * zoomGroup.getScaleX();
                double mapH  = mapPane.getHeight() * zoomGroup.getScaleY();
                double viewW = mapScrollPane.getViewportBounds().getWidth();
                double viewH = mapScrollPane.getViewportBounds().getHeight();

                double px = mid.getX() * zoomGroup.getScaleX();
                double py = mid.getY() * zoomGroup.getScaleY();

                double h = clamp01((px - viewW / 2) / (mapW - viewW));
                double v = clamp01((py - viewH / 2) / (mapH - viewH));

                mapScrollPane.setHvalue(h);
                mapScrollPane.setVvalue(v);
            });
        }

        chartHighlight.toFront();
    }

    // ─────────────────────────────────────────────────────────────
    //  Draw annotations
    // ─────────────────────────────────────────────────────────────
    private void drawAnnotations(Activity activity) {
        for (Annotation ann : activity.getAnnotations()) {
            drawAnnotation(ann);
        }
    }

    private void drawAnnotation(Annotation ann) {
    Color color = parseColor(ann.getColor());
    List<GeoPoint> pts = ann.getGeoPoints();
    if (pts == null || pts.isEmpty()) return;

    switch (ann.getType()) {
        case POINT -> {
            Point2D p = projection.project(pts.get(0));
            Circle c = new Circle(p.getX(), p.getY(), 6, color);
            c.setStroke(Color.WHITE);
            c.setStrokeWidth(1.5);
            mapPane.getChildren().add(c);

            Text texto = null;
            if (ann.getText() != null && !ann.getText().isBlank()) {
                texto = new Text(p.getX() + 8, p.getY() - 4, ann.getText());
                texto.setFill(color);
                mapPane.getChildren().add(texto);
            }
            final Text textoFinal = texto;
            c.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.SECONDARY) {
                    mostrarMenuBorrarAnotacion(ann, c, textoFinal);
                    e.consume();
                }
            });
            if (textoFinal != null) {
                textoFinal.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.SECONDARY) {
                        mostrarMenuBorrarAnotacion(ann, c, textoFinal);
                        e.consume();
                    }
                });
            }
        }
        case TEXT -> {
            Point2D p = projection.project(pts.get(0));
            Text t = new Text(p.getX(), p.getY(), ann.getText());
            t.setFill(color);
            t.setStyle("-fx-font-weight:bold;");
            mapPane.getChildren().add(t);
            t.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.SECONDARY) {
                    mostrarMenuBorrarAnotacion(ann, t);
                    e.consume();
                }
            });
        }
        case LINE -> {
            if (pts.size() < 2) return;
            Point2D a = projection.project(pts.get(0));
            Point2D b = projection.project(pts.get(1));
            Line l = new Line(a.getX(), a.getY(), b.getX(), b.getY());
            l.setStroke(color);
            l.setStrokeWidth(ann.getStrokeWidth());
            mapPane.getChildren().add(l);

            Text texto = null;
            if (ann.getText() != null && !ann.getText().isBlank()) {
                double mx = (a.getX() + b.getX()) / 2;
                double my = (a.getY() + b.getY()) / 2 - 10;
                texto = new Text(mx, my, ann.getText());
                texto.setFill(color);
                mapPane.getChildren().add(texto);
            }
            final Text textoFinal = texto;
            l.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.SECONDARY) {
                    mostrarMenuBorrarAnotacion(ann, l, textoFinal);
                    e.consume();
                }
            });
            if (textoFinal != null) {
                textoFinal.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.SECONDARY) {
                        mostrarMenuBorrarAnotacion(ann, l, textoFinal);
                        e.consume();
                    }
                });
            }
        }
        case CIRCLE -> {
            if (pts.size() < 2) return;
            Point2D centre = projection.project(pts.get(0));
            Point2D edge   = projection.project(pts.get(1));
            double radius  = centre.distance(edge);
            Circle c = new Circle(centre.getX(), centre.getY(), radius);
            c.setFill(Color.TRANSPARENT);
            c.setStroke(color);
            c.setStrokeWidth(ann.getStrokeWidth());
            mapPane.getChildren().add(c);

            Text texto = null;
            if (ann.getText() != null && !ann.getText().isBlank()) {
                texto = new Text(centre.getX(), centre.getY() - radius - 4, ann.getText());
                texto.setFill(color);
                mapPane.getChildren().add(texto);
            }
            final Text textoFinal = texto;
            c.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.SECONDARY) {
                    mostrarMenuBorrarAnotacion(ann, c, textoFinal);
                    e.consume();
                }
            });
            if (textoFinal != null) {
                textoFinal.setOnMouseClicked(e -> {
                    if (e.getButton() == MouseButton.SECONDARY) {
                        mostrarMenuBorrarAnotacion(ann, c, textoFinal);
                        e.consume();
                    }
                });
            }
        }
    }
}

    private void mostrarMenuBorrarAnotacion(Annotation ann, javafx.scene.Node... nodos) {
    ContextMenu cm = new ContextMenu();
    MenuItem borrar = new MenuItem("Borrar anotación");
    borrar.setOnAction(ev -> {
        app.removeAnnotation(ann);
        for (javafx.scene.Node nodo : nodos) {
            if (nodo != null) mapPane.getChildren().remove(nodo);
        }
    });
    cm.getItems().add(borrar);
    cm.show(nodos[0], javafx.geometry.Side.TOP, 0, 0);
}
    
    // ─────────────────────────────────────────────────────────────
    //  Right-click → context menu
    // ─────────────────────────────────────────────────────────────
    private void onMapRightClick(double x, double y, MouseEvent e) {
        // Re-bind menu items with current coordinates (replace previous handler)
        contextMenu.getItems().get(0).setOnAction(ev ->
                createPointOrTextAnnotation(AnnotationType.POINT,  x, y));
        contextMenu.getItems().get(1).setOnAction(ev ->
                createPointOrTextAnnotation(AnnotationType.TEXT,   x, y));
        contextMenu.getItems().get(2).setOnAction(ev ->
                startTwoClickAnnotation(AnnotationType.LINE,   x, y));
        contextMenu.getItems().get(3).setOnAction(ev ->
                startTwoClickAnnotation(AnnotationType.CIRCLE, x, y));

        contextMenu.show(mapPane, e.getScreenX(), e.getScreenY());
    }

    // ─────────────────────────────────────────────────────────────
    //  Single-click annotations (POINT, TEXT)
    // ─────────────────────────────────────────────────────────────
    private void createPointOrTextAnnotation(AnnotationType type, double x, double y) {
        Dialog<Annotation> dlg = buildAnnotationDialog(type, false);

        dlg.setResultConverter(btn -> {
            if (btn.getButtonData() != ButtonBar.ButtonData.OK_DONE) return null;
            @SuppressWarnings("unchecked")
            GridPane grid = (GridPane) dlg.getDialogPane().getContent();
            TextField tfText   = (TextField) grid.getChildren().get(1);
            ColorPicker cpColor = (ColorPicker) grid.getChildren().get(3);

            String hex  = toHex(cpColor.getValue());
            GeoPoint gp = projection.unproject(x, y);
            return new Annotation(type, tfText.getText().trim(), hex, 2.0,
                                  List.of(gp));
        });

        dlg.showAndWait().ifPresent(ann -> {
            Annotation saved = app.addAnnotation(currentActivity, ann);
            if (saved != null) drawAnnotation(saved);
        });
    }

    // ─────────────────────────────────────────────────────────────
    //  Two-click annotations (LINE, CIRCLE) – step 1
    // ─────────────────────────────────────────────────────────────
    /**
     * Opens the configuration dialog for a LINE or CIRCLE annotation.
     * After the user confirms, the controller enters "pending" mode:
     * the cursor changes and the next left-click on the map provides
     * the second point.
     */
    private void startTwoClickAnnotation(AnnotationType type, double x1, double y1) {
        Dialog<PendingAnnotation> dlg = buildAnnotationDialog(type, true);

        dlg.setResultConverter(btn -> {
            if (btn.getButtonData() != ButtonBar.ButtonData.OK_DONE) return null;
            @SuppressWarnings("unchecked")
            GridPane grid = (GridPane) dlg.getDialogPane().getContent();
            TextField   tfText  = (TextField)   grid.getChildren().get(1);
            ColorPicker cpColor = (ColorPicker) grid.getChildren().get(3);

            return new PendingAnnotation(type, x1, y1,
                    tfText.getText().trim(),
                    cpColor.getValue(),
                    2.0);
        });

        dlg.showAndWait().ifPresent(pending -> {
            pendingAnnotation = pending;

            // Visual hint: cursor + dashed preview shape added at first point
            mapPane.setCursor(javafx.scene.Cursor.CROSSHAIR);
            addInitialPreview(pending);
        });
    }

    /** Creates the preview shape at the first-click position. */
    private void addInitialPreview(PendingAnnotation p) {
        removePreview();
        if (p.type == AnnotationType.LINE) {
            Line l = new Line(p.x1, p.y1, p.x1, p.y1);
            l.setStroke(p.color);
            l.setStrokeWidth(p.strokeWidth);
            l.getStrokeDashArray().addAll(6.0, 4.0);
            l.setOpacity(0.7);
            previewShape = l;
        } else { // CIRCLE
            Circle c = new Circle(p.x1, p.y1, 0);
            c.setFill(Color.TRANSPARENT);
            c.setStroke(p.color);
            c.setStrokeWidth(p.strokeWidth);
            c.getStrokeDashArray().addAll(6.0, 4.0);
            c.setOpacity(0.7);
            previewShape = c;
        }
        mapPane.getChildren().add(previewShape);
        previewShape.toFront();
        chartHighlight.toFront();
    }

    /** Updates the preview shape as the mouse moves. */
    private void updatePreview(double mouseX, double mouseY) {
        if (pendingAnnotation == null || previewShape == null) return;

        if (pendingAnnotation.type == AnnotationType.LINE) {
            Line l = (Line) previewShape;
            l.setEndX(mouseX);
            l.setEndY(mouseY);
        } else { // CIRCLE
            Circle c = (Circle) previewShape;
            double radius = Math.hypot(mouseX - pendingAnnotation.x1,
                                       mouseY - pendingAnnotation.y1);
            c.setRadius(radius);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Two-click annotations – step 2 (second point committed)
    // ─────────────────────────────────────────────────────────────
    private void commitSecondPoint(double x2, double y2) {
        if (pendingAnnotation == null) return;

        PendingAnnotation p = pendingAnnotation;
        cancelPending();   // clears state + removes preview + restores cursor

        GeoPoint gp1 = projection.unproject(p.x1, p.y1);
        GeoPoint gp2 = projection.unproject(x2, y2);

        // Guard: reject degenerate annotations (both points identical)
        if (gp1.getLatitude() == gp2.getLatitude()
                && gp1.getLongitude() == gp2.getLongitude()) {
            return;
        }

        String hex = toHex(p.color);
        Annotation ann = new Annotation(p.type, p.text, hex, p.strokeWidth,
                                        List.of(gp1, gp2));

        Annotation saved = app.addAnnotation(currentActivity, ann);
        if (saved != null) {
            drawAnnotation(saved);
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Pending state helpers
    // ─────────────────────────────────────────────────────────────
    private void cancelPending() {
        pendingAnnotation = null;
        removePreview();
        if (mapPane != null) {
            mapPane.setCursor(javafx.scene.Cursor.DEFAULT);
        }
    }

    private void removePreview() {
        if (previewShape != null && mapPane != null) {
            mapPane.getChildren().remove(previewShape);
            previewShape = null;
        }
    }

    // ─────────────────────────────────────────────────────────────
    //  Shared dialog builder
    // ─────────────────────────────────────────────────────────────
    /**
     * Builds a configuration dialog for any annotation type.
     *
     * @param twoClick true → show instruction label for 2nd-click types.
     */
    private <T> Dialog<T> buildAnnotationDialog(AnnotationType type, boolean twoClick) {
        Dialog<T> dlg = new Dialog<>();
        dlg.setTitle("Nueva anotación – " + type.name());
        dlg.setHeaderText("Configura la anotación");

        ButtonType okBtn = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        dlg.getDialogPane().getButtonTypes().addAll(okBtn, ButtonType.CANCEL);

        TextField   tfText  = new TextField();
        tfText.setPromptText("Texto (opcional)");
        ColorPicker cpColor = new ColorPicker(Color.RED);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.add(new Label("Texto:"),  0, 0);
        grid.add(tfText,               1, 0);
        grid.add(new Label("Color:"),  0, 1);
        grid.add(cpColor,              1, 1);

        if (twoClick) {
            String instruccion = type == AnnotationType.LINE
                    ? "Haz clic en el mapa para fijar el punto final de la línea."
                    : "Haz clic en el mapa para fijar un punto en el borde del círculo.";
            Label lbl = new Label(instruccion);
            lbl.setWrapText(true);
            lbl.setStyle("-fx-text-fill: #555; -fx-font-style: italic;");
            grid.add(lbl, 0, 2, 2, 1);
        }

        dlg.getDialogPane().setContent(grid);
        return dlg;
    }

    // ─────────────────────────────────────────────────────────────
    //  Elevation chart
    // ─────────────────────────────────────────────────────────────
    private void buildElevationChart(Activity activity) {
    if (elevationChart == null) return;
    elevationChart.getData().clear();

    List<TrackPoint> points = activity.getTrackPoints();
    if (points == null || points.isEmpty()) return;

    NumberAxis xAxis = (NumberAxis) elevationChart.getXAxis();
    xAxis.setLabel("Distancia (km)");
    xAxis.setTickUnit(1);
    xAxis.setMinorTickCount(0);
    xAxis.setAutoRanging(true);

    NumberAxis yAxis = (NumberAxis) elevationChart.getYAxis();
    yAxis.setTickLabelGap(4);

    XYChart.Series<Number, Number> series = new XYChart.Series<>();
    series.setName("Altitud (m)");

    List<Double> distAcumulada = new ArrayList<>();
    double totalDist = 0;
    distAcumulada.add(0.0);
    for (int i = 1; i < points.size(); i++) {
        totalDist += points.get(i - 1).distanceTo(points.get(i));
        distAcumulada.add(totalDist);
    }

    int step = Math.max(1, points.size() / 200);

    for (int i = 0; i < points.size(); i += step) {
        double distKm = distAcumulada.get(i) / 1000.0;
        series.getData().add(
            new XYChart.Data<>(distKm, points.get(i).getElevation()));
    }

    elevationChart.getData().add(series);
    elevationChart.setCreateSymbols(false);
    elevationChart.setAnimated(false);
    elevationChart.setLegendVisible(false);

    int[] pointIndices = buildIndexMap(points, step);
    installChartHover(series, points, pointIndices, distAcumulada, step);
}
    
    
    private int[] buildIndexMap(List<TrackPoint> pts, int step) {
        int count = (pts.size() + step - 1) / step;
        int[] idx = new int[count];
        for (int i = 0; i < count; i++) idx[i] = i * step;
        return idx;
    }

    private void installChartHover(
        XYChart.Series<Number, Number> series,
        List<TrackPoint> points,
        int[] indices,
        List<Double> distAcumulada,
        int step) {

    javafx.scene.control.Tooltip tooltip = new javafx.scene.control.Tooltip();
    tooltip.setStyle(
        "-fx-background-color: white;" +
        "-fx-border-color: #E0E0DC;" +
        "-fx-border-width: 1;" +
        "-fx-padding: 8 12 8 12;" +
        "-fx-font-size: 11px;" +
        "-fx-text-fill: #1A1A1A;"
    );

    elevationChart.setOnMouseMoved(e -> {
        NumberAxis xAxis = (NumberAxis) elevationChart.getXAxis();
        double xInAxis = xAxis.sceneToLocal(e.getSceneX(), 0).getX();
        double distKm  = xAxis.getValueForDisplay(xInAxis).doubleValue();

        int bestIdx = 0;
        double bestDiff = Double.MAX_VALUE;
        for (int i = 0; i < indices.length; i++) {
            double d = distAcumulada.get(indices[i]) / 1000.0;
            double diff = Math.abs(d - distKm);
            if (diff < bestDiff) {
                bestDiff = diff;
                bestIdx = i;
            }
        }

        TrackPoint tp = points.get(indices[bestIdx]);
        Point2D p = projection.project(tp);

        chartHighlight.setCenterX(p.getX());
        chartHighlight.setCenterY(p.getY());
        chartHighlight.setVisible(true);

        scrollMapTo(p);

        double velocidad = 0;
        if (indices[bestIdx] > 0) {
            TrackPoint prev = points.get(indices[bestIdx] - 1);
            velocidad = prev.speedTo(tp);
        }

        String ritmo = "—";
        if (velocidad > 0) {
            double minKm = 60.0 / velocidad;
            int min = (int) minKm;
            int sec = (int) ((minKm - min) * 60);
            ritmo = String.format("%d:%02d min/km", min, sec);
        }

        String texto = String.format(
            "Distancia: %.2f km\nAltitud: %.0f m\nVelocidad: %.1f km/h\nRitmo: %s",
            distAcumulada.get(indices[bestIdx]) / 1000.0,
            tp.getElevation(),
            velocidad,
            ritmo
        );
        tooltip.setText(texto);
        javafx.scene.control.Tooltip.install(elevationChart, tooltip);
        tooltip.show(elevationChart, e.getScreenX() + 15, e.getScreenY() - 10);
    });

    elevationChart.setOnMouseExited(e -> {
        if (chartHighlight != null) chartHighlight.setVisible(false);
        tooltip.hide();
    });
}
    

    private void scrollMapTo(Point2D p) {
    if (mapScrollPane == null || zoomGroup == null) return;

    double mapW  = mapPane.getWidth()  * zoomGroup.getScaleX();
    double mapH  = mapPane.getHeight() * zoomGroup.getScaleY();
    double viewW = mapScrollPane.getViewportBounds().getWidth();
    double viewH = mapScrollPane.getViewportBounds().getHeight();

    double px = p.getX() * zoomGroup.getScaleX();
    double py = p.getY() * zoomGroup.getScaleY();

    // Posición actual del viewport
    double currentLeft   = mapScrollPane.getHvalue() * (mapW - viewW);
    double currentTop    = mapScrollPane.getVvalue() * (mapH - viewH);
    double currentRight  = currentLeft + viewW;
    double currentBottom = currentTop  + viewH;

    // Si el punto ya es visible con margen de 40px, no mover
    double margin = 40;
    boolean dentroX = px > currentLeft  + margin && px < currentRight  - margin;
    boolean dentroY = py > currentTop   + margin && py < currentBottom - margin;

    if (dentroX && dentroY) return;

    double h = clamp01((px - viewW / 2) / (mapW - viewW));
    double v = clamp01((py - viewH / 2) / (mapH - viewH));

    final Timeline tl = new Timeline(
            new KeyFrame(Duration.millis(200),
                    new KeyValue(mapScrollPane.hvalueProperty(), h),
                    new KeyValue(mapScrollPane.vvalueProperty(), v)));
    tl.play();
}
    

    @FXML
    private void importarGPXBienvenida(ActionEvent event) {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Seleccionar fichero GPX");
        fc.getExtensionFilters().add(
                new javafx.stage.FileChooser.ExtensionFilter("GPX", "*.gpx"));
        java.io.File gpx = fc.showOpenDialog(mapScrollPane.getScene().getWindow());
        if (gpx == null) return;
        Activity act = app.importActivity(gpx);
        if (act != null) mostrarActividad(act);
    }

    // ── Velocidad sobre el trazado ────────────────────────────────
    @FXML
    private void toggleVelocidad() {
        if (currentActivity == null) {
            if (btnVelocidad != null) {
                btnVelocidad.setSelected(false);
                btnVelocidad.setText("Velocidad");
            }
            return;
        }

        if (btnVelocidad.isSelected()) {
            btnVelocidad.setText("Velocidad");
            drawRouteVelocidad(currentActivity);
        } else {
            btnVelocidad.setText("Velocidad");
            mapPane.getChildren().removeAll(velocidadNodes);
            velocidadNodes.clear();

            if (routePolyline != null) {
                routePolyline.setVisible(true);
                if (!mapPane.getChildren().contains(routePolyline)) {
                    mapPane.getChildren().add(1, routePolyline);
                }
            }
        }
    }

    private void drawRouteVelocidad(Activity activity) {
        List<TrackPoint> points = activity.getTrackPoints();
        if (points == null || points.size() < 2) return;

        List<Double> velocidades = new ArrayList<>();
        for (int i = 1; i < points.size(); i++) {
            velocidades.add(points.get(i - 1).speedTo(points.get(i)));
        }

        double maxV = velocidades.stream().mapToDouble(Double::doubleValue).max().orElse(10);
        double minV = velocidades.stream().filter(v -> v > 0).mapToDouble(Double::doubleValue).min().orElse(0);

        // Ocultar polyline azul
        if (routePolyline != null) routePolyline.setVisible(false);

        // Quitar nodos anteriores de velocidad
        mapPane.getChildren().removeAll(velocidadNodes);
        velocidadNodes.clear();

        // Dibujar segmentos coloreados
        List<javafx.scene.Node> segmentos = new ArrayList<>();
        for (int i = 1; i < points.size(); i++) {
            Point2D a = projection.project(points.get(i - 1));
            Point2D b = projection.project(points.get(i));
            double v = velocidades.get(i - 1);
            Color color = colorPorVelocidad(v, minV, maxV);
            Line seg = new Line(a.getX(), a.getY(), b.getX(), b.getY());
            seg.setStroke(color);
            seg.setStrokeWidth(3.0);
            segmentos.add(seg);
        }
        velocidadNodes.addAll(segmentos);
        mapPane.getChildren().addAll(1, segmentos);

        // Leyenda
        drawLeyendaVelocidad(minV, maxV);
        chartHighlight.toFront();
    }

    private Color colorPorVelocidad(double v, double minV, double maxV) {
        if (maxV <= minV) return Color.YELLOW;
        double t = Math.max(0, Math.min(1, (v - minV) / (maxV - minV)));
        if (t < 0.5) return Color.color(1.0, t * 2, 0.0);
        else return Color.color(1.0 - (t - 0.5) * 2, 1.0, 0.0);
    }

    private void drawLeyendaVelocidad(double minV, double maxV) {
        double x = 16, y = 16, w = 140, h = 16;
        List<javafx.scene.Node> leyenda = new ArrayList<>();

        javafx.scene.shape.Rectangle bg = new javafx.scene.shape.Rectangle(x-6, y-6, w+12, 62);
        bg.setFill(Color.color(1,1,1,0.85)); bg.setArcWidth(8); bg.setArcHeight(8);
        leyenda.add(bg);

        Text titulo = new Text(x, y+10, "Velocidad sobre el trazado");
        titulo.setFill(Color.web("#333")); leyenda.add(titulo);

        int pasos = 20;
        for (int i = 0; i < pasos; i++) {
            double t = (double)i/(pasos-1);
            Color c = colorPorVelocidad(minV + t*(maxV-minV), minV, maxV);
            javafx.scene.shape.Rectangle r = new javafx.scene.shape.Rectangle(x+i*(w/pasos), y+18, w/pasos+1, h);
            r.setFill(c); leyenda.add(r);
        }

        Text l1 = new Text(x, y+52, String.format("%.1f km/h", minV));
        l1.setFill(Color.web("#333")); leyenda.add(l1);
        Text l2 = new Text(x+w-45, y+52, String.format("%.1f km/h", maxV));
        l2.setFill(Color.web("#333")); leyenda.add(l2);

        velocidadNodes.addAll(leyenda);
        mapPane.getChildren().addAll(leyenda);
    }

    // ─────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────
    private static Color parseColor(String hex) {
        try {
            return Color.web(hex);
        } catch (Exception e) {
            return Color.RED;
        }
    }

    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X",
                (int) (c.getRed()   * 255),
                (int) (c.getGreen() * 255),
                (int) (c.getBlue()  * 255));
    }

    private static double clamp01(double v) {
        return Math.max(0, Math.min(1, v));
    }
}