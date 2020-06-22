package ex02;

import ex02.components.SceneEditor;
import ex02.raytracer.RayTracer;
import ex02.raytracer.parser.ParserException;
import ex02.raytracer.parser.SceneParser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.imageio.ImageIO;
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
    private SceneEditor sceneEditor;
    private Button openSceneEditorButton;

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

        this.sceneEditor = new SceneEditor(primaryStage, this::updateScene);

        this.openSceneEditorButton = new Button("Scene Editor");
        this.openSceneEditorButton.setDisable(this.scene == null);
        this.openSceneEditorButton.setOnAction(e -> sceneEditor.show());

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
                            this.updateScene(parser.parse());
                        } catch (final IOException | ParserException e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                });

        final FileChooser saveFileChooser = new FileChooser();
        saveFileChooser.setTitle("Save png File");
        saveFileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("png files (*.png)", "*.png")
        );

        final Button saveButton = new Button("Save Image");
        saveButton.setOnAction(
                actionEvent -> {
                    final File file = saveFileChooser.showSaveDialog(primaryStage);
                    saveImage(file);
                });

        this.canvas = new Canvas(WIDTH, HEIGHT);

        initCanvas();

        final HBox buttons = new HBox(12.0, openButton, openSceneEditorButton, saveButton);
        buttons.setPadding(new Insets(8, 12, 8, 12));

        final VBox box = new VBox(8.0, canvas, buttons);

        primaryStage.setScene(new Scene(box));
        primaryStage.show();
    }

    private void updateScene(final ex02.entities.Scene scene) {
        log.debug("Scene set to {}", scene != null ? scene.getName() : "null");
        this.scene = scene;
        this.sceneEditor.onSceneLoaded(this.scene);
        openSceneEditorButton.setDisable(this.scene == null);
        Platform.runLater(this::renderScene); //new Thread(this::renderScene).start();
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

    private void renderScene() {
        final RayTracer rayTracer = RayTracer.create(WIDTH, HEIGHT, this.scene);

        try {
            final GraphicsContext gc = canvas.getGraphicsContext2D();

            final double[][][] pixels = rayTracer.render();
            setPixelsOnImage(pixels, gc.getPixelWriter());
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void saveImage(final File file) {
        if(file != null) {
            try {
                final WritableImage writableImage = new WritableImage(
                        (int) canvas.getWidth(),
                        (int) canvas.getHeight()
                );

                canvas.snapshot(null, writableImage);

                ImageIO.write(SwingFXUtils.fromFXImage(writableImage, null), "png", file);
                log.debug("Saved image as {}", file.getAbsolutePath());
            } catch (final IOException e) {
                log.error("problem saving png file", e);
            }
        }
    }

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
