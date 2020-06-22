package ex02.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class VertexControl extends HBox {

    private static final double SPACING = 12.0;

    private final ObjectProperty<double[]> vertex = new SimpleObjectProperty<>(this, "vertex");

    public ObjectProperty<double[]> vertexProperty() { return vertex; }

    public VertexControl(double[] vertex) {
        this(vertex[0], vertex[1], vertex[2]);
    }

    public VertexControl(double x, double y, double z) {
        super(SPACING);
        this.setAlignment(Pos.CENTER_LEFT);

        final DoubleField xField = new DoubleField(x);
        final DoubleField yField = new DoubleField(y);
        final DoubleField zField = new DoubleField(z);

        xField.onChange(
                (observable, oldValue, newValue) -> vertexProperty()
                        .setValue(new double[] { newValue, yField.getValue(), zField.getValue() })
        );

        yField.onChange(
                (observable, oldValue, newValue) -> vertexProperty()
                        .setValue(new double[] { xField.getValue(), newValue, zField.getValue() })
        );

        zField.onChange(
                (observable, oldValue, newValue) -> vertexProperty()
                        .setValue(new double[] { xField.getValue(), yField.getValue(), newValue })
        );

        this.getChildren().addAll(new Label("X:"), xField, new Label("Y:"), yField, new Label("Z:"), zField);
    }

}
