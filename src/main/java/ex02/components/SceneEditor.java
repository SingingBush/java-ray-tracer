package ex02.components;

import ex02.entities.Scene;
import ex02.entities.lights.Light;
import ex02.entities.primitives.Primitive;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

import java.util.List;
import java.util.function.Consumer;

public class SceneEditor extends Stage {

    private ex02.entities.Scene scene;

    private final Label label;
    private final Button cancelButton;
    private final Button applyButton;

    public SceneEditor(final Window owner, final Consumer<Scene> callback) {
        this.setTitle("Scene Editor");
        this.initStyle(StageStyle.DECORATED);
        this.initModality(Modality.NONE);
        this.initOwner(owner);

        this.label = new Label();
        this.label.setFont(Font.font("Tahoma", FontWeight.SEMI_BOLD, 16.0)); // Verdana or Tahoma
        this.label.setPadding(new Insets(0.0, 0.0, 12, 0.0));

        this.cancelButton = new Button("Cancel");
        this.cancelButton.setCancelButton(true);
        this.cancelButton.setOnAction(event -> this.hide());

        this.applyButton = new Button("Apply");
        this.applyButton.setOnAction(event -> callback.accept(this.scene));

        this.setScene(this.buildUI());
    }

    private javafx.scene.Scene buildUI() {
        final GridPane gridPane = new GridPane();
        gridPane.setAlignment(Pos.BASELINE_LEFT);
        gridPane.setMinSize(400, 200);
        //gridPane.setPadding(new Insets(8, 12, 8, 12));
        gridPane.setPadding(new Insets(0.0, 0.0, 12, 0.0));
        gridPane.setVgap(10.0);
        gridPane.setHgap(8.0);

        if(scene != null) {
            this.label.setText(String.format("Scene: %s", scene.getName()));

            // create component for changing background color
            final double[] backgroundColor = this.scene.getBackgroundColor();
            final ColorPicker backgroundColorPicker = new ColorPicker(Color.color(backgroundColor[0], backgroundColor[1], backgroundColor[2]));
            backgroundColorPicker.setOnAction(event -> {
                final Color value = backgroundColorPicker.getValue();
                this.scene.setBackgroundColor(value.getRed(), value.getGreen(), value.getBlue());
            });

            gridPane.add(new Label("Background Color:"), 0, 1);
            gridPane.add(backgroundColorPicker, 1, 1);

            // create component for changing ambient light color
            final double[] ambientColor = this.scene.getAmbientLight();
            final ColorPicker ambientColorPicker = new ColorPicker(Color.color(ambientColor[0], ambientColor[1], ambientColor[2]));
            ambientColorPicker.setOnAction(event -> {
                final Color value = ambientColorPicker.getValue();
                this.scene.setAmbientLight(value.getRed(), value.getGreen(), value.getBlue());
            });

            gridPane.add(new Label("Ambient Color:"), 0, 2);
            gridPane.add(ambientColorPicker, 1, 2);

            final GridPane cameraControls = new GridPane();
            cameraControls.setPadding(new Insets(0.0, 0.0, 8, 0.0));
            cameraControls.setVgap(10.0);
            cameraControls.setHgap(8.0);

            // Camera Eye
            final VertexControl cameraEye = new VertexControl(this.scene.getCamera().getEye());
            cameraEye
                    .vertexProperty()
                    .addListener((observable, oldValue, newValue) -> this.scene.getCamera().setEye(newValue));

            cameraControls.add(new Label("Camera Eye:"), 0, 0);
            cameraControls.add(cameraEye, 1, 0);

            // Camera Direction
            final VertexControl cameraDirection = new VertexControl(this.scene.getCamera().getDirection());
            cameraDirection
                    .vertexProperty()
                    .addListener((observable, oldValue, newValue) -> this.scene.getCamera().setDirection(newValue));

            cameraControls.add(new Label("Camera Direction:"), 0, 1);
            cameraControls.add(cameraDirection, 1, 1);

            // Screen Distance
            final DoubleField screenDistField = new DoubleField(this.scene.getCamera().getScreenDist());
            screenDistField.onChange(
                    (observable, oldValue, newValue) -> this.scene.getCamera().setScreenDist(newValue)
            );

            cameraControls.add(new Label("Camera Distance:"), 0, 2);
            cameraControls.add(screenDistField, 1, 2);

            gridPane.add(new Label("Camera:"), 0, 3);
            gridPane.add(cameraControls, 1, 3);

            int row = 4;

            final List<Light> lights = this.scene.getLights();
            for(final Light light : lights) {
                final LightControl lightControls = new LightControl(light.getColor(), light.getPosition());

                lightControls
                        .colorProperty()
                        .addListener((observable, oldValue, newValue) -> light.setColor(newValue.getRed(), newValue.getGreen(), newValue.getBlue()));

                lightControls
                        .vertexProperty()
                        .addListener((observable, oldValue, newValue) -> light.setPosition(newValue));

                gridPane.add(new Label("Light:"), 0, row);
                gridPane.add(lightControls, 1, row);

                //lightControls.setBackground(new Background(new BackgroundFill(Color.YELLOW.brighter(), CornerRadii.EMPTY, Insets.EMPTY)));

                row++;
            }

            final List<Primitive> primitives = this.scene.getPrimitives();
            for(final Primitive primitive : primitives) {
                // todo: finish off primitives (Sphere and Torus both have center vertex so could be moved easily)
                gridPane.add(new Label("Primitive:"), 0, row);
                gridPane.add(new Label(primitive.getClass().getSimpleName()), 1, row);
                row++;
            }
        } else {
            this.label.setText("No scene data found");
        }

        final HBox buttons = new HBox(12.0, new Button("Ok"), cancelButton, applyButton);
        buttons.setPadding(new Insets(4, 0, 4, 0));

        final VBox layout = new VBox(label, gridPane, buttons);

        layout.setPadding(new Insets(8, 12, 8, 12));
        //layout.setBackground(new Background(new BackgroundFill(Color.BLUE.brighter(), CornerRadii.EMPTY, Insets.EMPTY)));

        final ScrollPane scrollPane = new ScrollPane(layout);
        scrollPane.setMinHeight(200.0);
        scrollPane.setMaxHeight(600.0); // prevent scene editor being too big to close

        return new javafx.scene.Scene(scrollPane);
    }

    public void onSceneLoaded(ex02.entities.Scene scene) {
        this.scene = scene;
        this.label.setText(scene != null && scene.getName() != null ? scene.getName() : "No scene data found");
        this.setScene(this.buildUI());
    }

}
