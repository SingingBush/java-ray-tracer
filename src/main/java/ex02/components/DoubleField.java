package ex02.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.util.converter.DoubleStringConverter;

public class DoubleField extends TextField {

    private final ObjectProperty<Double> value = new SimpleObjectProperty<>(this, "value");

    public ObjectProperty<Double> doubleProperty() { return value; }

    public final void setValue(Double value) { doubleProperty().set(value); }
    public final Double getValue() { return doubleProperty().get(); }

    public DoubleField(final double initialValue) {
        super(String.valueOf(initialValue));

        if(Double.isInfinite(initialValue)) {
            this.setEditable(false);
            this.setDisabled(true);

            this.setBackground(new Background(new BackgroundFill(Paint.valueOf(Color.GRAY.brighter().toString()), new CornerRadii(4.0), Insets.EMPTY)));
        } else {
            setValue(initialValue);

            this.textProperty().addListener((observable, oldValue, newValue) -> {
                // this prevents the user from typing anything that's not a number
                ((StringProperty)observable).setValue(newValue.matches("-?\\d+(\\.\\d+)?") ? newValue : oldValue);
            });

            //this.setTextFormatter(new TextFormatter<>(new DoubleStringConverter(), initialValue)); // causes trailing zeros to drop off
            this.textProperty().bindBidirectional(this.value, new DoubleStringConverter());
        }
    }

    public void onChange(ChangeListener<Double> listener) {
        this.doubleProperty().addListener(listener);
    }
}
