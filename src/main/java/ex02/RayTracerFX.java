package ex02;

import ex02.raytracer.RayTracer;
import ex02.raytracer.parser.ParserException;
import ex02.raytracer.parser.SceneParser;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author Samael Bate (singingbush)
 * created on 08/04/18
 */
public class RayTracerFX extends Application {

    private static final Logger log = LoggerFactory.getLogger(RayTracerFX.class);

    private ex02.entities.Scene scene;
    private ImageView imageView;
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
                            final SceneParser parser = new SceneParser(Files.newBufferedReader(file.toPath()));
                            this.scene = parser.parse();
                            this.displayImage();
                        } catch (final IOException | ParserException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                });

        // not sure if should use WritableImage or Canvas
        this.canvas = new Canvas(1024, 720);
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setFill(Color.RED);
        gc.strokeLine(0, 0, 1024, 720);

        final WritableImage imageData = new WritableImage(1024, 720);
//        imageData.getPixelWriter().setColor(10, 10, Color.BEIGE);
//        imageData.getPixelWriter().setColor(10, 20, Color.TOMATO);

        this.imageView = new ImageView(imageData);

        final VBox box = new VBox(8.0, canvas, openButton);

        primaryStage.setScene(new Scene(box));
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        log.info("stopping...");
    }

    private void displayImage() {
        try {
            final RayTracer rayTracer = RayTracer.create(1024, 720, this.scene);
            final double[][][] pixels = rayTracer.render();
            
            // put pixels into WritableImage
            final WritableImage imageData = new WritableImage(1024, 720);
            final PixelWriter pw = imageData.getPixelWriter();

            for (int x = 0; x < pixels.length; x++) {
                for (int y = 0; y < pixels[x].length; y++) {
                    pw.setColor(x, y, Color.color(pixels[x][y][0], pixels[x][y][1], pixels[x][y][2]));

                    canvas.getGraphicsContext2D()
                            .getPixelWriter()
                            .setColor(x, y, Color.color(pixels[x][y][0], pixels[x][y][1], pixels[x][y][2]));
                }
            }
            this.imageView.setImage(imageData);
        } catch (final Exception e) {
            log.error(e.getMessage(), e);
        }
    }

//    private void saveImage() {
//        ImageIO.write(SwingFXUtils.fromFXImage(pic.getImage(),null), "png", file); todo
//    }
}
