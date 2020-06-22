package ex02.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LightControl extends GridPane {

    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(this, "color");
    private final ObjectProperty<double[]> vertex = new SimpleObjectProperty<>(this, "vertex");

    public ObjectProperty<Color> colorProperty() { return color; }
    public ObjectProperty<double[]> vertexProperty() { return vertex; }

    public LightControl(@NotNull double[] color, @Nullable double[] position) {
        this.setPadding(new Insets(0.0, 0.0, 8, 0.0));
        this.setVgap(10.0);
        this.setHgap(8.0);

        //this.setBackground(new Background(new BackgroundFill(Color.GREEN.brighter(), CornerRadii.EMPTY, Insets.EMPTY)));

        // Control for the lights colour
        final ColorPicker lightColorPicker = new ColorPicker(Color.color(color[0], color[1], color[2]));
        //lightColorPicker.setPadding(new Insets(0,0,6,0)); // messes with the size of the picker
        lightColorPicker.setOnAction(event -> {
            final Color value = lightColorPicker.getValue();
            colorProperty().setValue(value);
        });

        // Control for the lights position. todo: if there was no position (null or infinity) skip this bit
        final VertexControl lightPosition = new VertexControl(position);
        lightPosition
                .vertexProperty()
                .addListener((observable, oldValue, newValue) -> this.vertexProperty().setValue(newValue));

        this.add(new Label("Color"), 0, 0);
        this.add(lightColorPicker, 1, 0);

        this.add(new Label("Position"), 0, 1);
        this.add(lightPosition, 1, 1);
    }
}
