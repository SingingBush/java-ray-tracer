package ex02;

import ex02.raytracer.RayTracer;
import ex02.raytracer.parser.ParserException;
import ex02.raytracer.parser.SceneParser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.PixelWriter;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * @author Samael Bate (singingbush)
 * created on 08/04/18
 */
public class RayTracerFX extends Application {

    private static final Logger log = LoggerFactory.getLogger(RayTracerFX.class);

    private static final int WIDTH = 1024;
    private static final int HEIGHT = 720;

    private ex02.entities.Scene scene;
    private Canvas canvas;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void init() throws Exception {
        super.init();
        log.info("initialising...");
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Java Ray Tracer");

        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Scene File");
        //fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));
        fileChooser.setInitialDirectory(new File("."));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*.*"),
                new FileChooser.ExtensionFilter("Plain Text", "*.txt")
        );


        final Button openButton = new Button("Open a Scene...");
        openButton.setOnAction(
                actionEvent -> {
                    final File file = fileChooser.showOpenDialog(primaryStage);
                    if (file != null) {
                        log.debug("Selected a Scene file");
                        try {
                            final SceneParser parser = new SceneParser(file);
                            this.scene = parser.parse();

                            Platform.runLater(this::displayImage); //new Thread(this::displayImage).start();
                        } catch (final IOException | ParserException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                });

        this.canvas = new Canvas(WIDTH, HEIGHT);

        initCanvas();

        final VBox box = new VBox(8.0, canvas, openButton);

        primaryStage.setScene(new Scene(box));
        primaryStage.show();
    }

    private void initCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.RED);
        gc.strokeLine(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        log.info("stopping...");
    }

    private void displayImage() {
        initCanvas();

        final RayTracer rayTracer = RayTracer.create(WIDTH, HEIGHT, this.scene);

        try {
            final GraphicsContext gc = canvas.getGraphicsContext2D();

            final double[][][] pixels = rayTracer.render();
            setPixelsOnImage(pixels, gc.getPixelWriter());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

//    private void saveImage() {
//        ImageIO.write(SwingFXUtils.fromFXImage(pic.getImage(),null), "png", file); todo
//    }

    private void setPixelsOnImage(@NotNull final double[][][] pixels, @NotNull final PixelWriter pixelWriter) {
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                pixelWriter.setColor(x, y, Color.color(
                        pixels[x][y][0],
                        pixels[x][y][1],
                        pixels[x][y][2]
                        )
                );
            }
        }
    }
}
