package ex02;

import ex02.entities.Scene;
import ex02.raytracer.RayTracer;
import ex02.raytracer.parser.ParserException;
import ex02.raytracer.parser.SceneParser;
import org.jetbrains.annotations.Nullable;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * @author Samael Bate (singingbush)
 * created on 08/04/18
 */
public class RayTracerSWT {

    private static final Logger log = LoggerFactory.getLogger(RayTracerSWT.class);

    private static Display display;
    private static Shell shell;

//    private final RayTracer rayTracer;

    private String cmdLineParams[];
    private org.eclipse.swt.graphics.Rectangle m_rect;
    private ImageData m_imgdat;
    private Text m_sceneText;

    private Scene currentScene;

    public static void main(String[] args) {
        log.debug("Ray Tracer started with args: {}", Arrays.toString(args));

        display = new Display();
        shell = new Shell(display);


        final RayTracerSWT app = new RayTracerSWT();
        app.cmdLineParams = args.clone();
        app.runMain(display);

        display.dispose();
    }

//    RayTracerSWT(final RayTracer rayTracer) {
//        this.rayTracer = rayTracer;
//    }

    private void runMain(final Display display) {
        Shell editShell = new Shell(display);
        editShell.setText("Input");
        editShell.setSize(600, 600);
        editShell.setLayout(new GridLayout());

        Composite editComp = new Composite(editShell, SWT.NONE);
        GridData ld = new GridData();
        ld.heightHint = 30;
        editComp.setLayoutData(ld);

        m_sceneText = new Text(editShell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        ld = new GridData(GridData.FILL_BOTH);
        m_sceneText.setLayoutData(ld);
        m_sceneText.setFont(new Font(display, "Courier New", 11, 0));


        final Shell previewWindow = new Shell(display);
        previewWindow.setText("Ray Tracer Ex");
        previewWindow.setSize(1024, 720);
        previewWindow.setLayout(new GridLayout());

        // the canvas we'll be drawing on.
        final Canvas canvas = new Canvas(previewWindow, SWT.BORDER | SWT.NO_REDRAW_RESIZE);
        ld = new GridData(GridData.FILL_BOTH);
        canvas.setLayoutData(ld);

        final Composite comp = new Composite(previewWindow, SWT.NONE);
        ld = new GridData();
        ld.heightHint = 45;
        comp.setLayoutData(ld);

        // "Render Button"
        final Button renderBtn = new Button(comp, SWT.PUSH);
        renderBtn.setText("Render");
        renderBtn.setSize(150, 40);
        renderBtn.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent ev) {
                if(currentScene != null) {
                    try {
                        m_imgdat = new ImageData(m_rect.width, m_rect.height, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
                        final RayTracer rayTracer = RayTracer.create(m_rect.width, m_rect.height, currentScene);
                        final double[][][] pixels = rayTracer.render();
                        setPixelsOnImage(pixels);
                        canvas.redraw();

                    } catch (final Exception e) {
                        log.error("Error Rendering scene: " + e.getMessage(), e);
                    }
                } else {
                    displayErrorMessageBox("No scene data has been loaded");
                }
            }
        });

        final Button savePngBot = new Button(comp, SWT.PUSH);
        savePngBot.setText("Save PNG");
        savePngBot.setSize(150, 40);
        savePngBot.setBounds(250, 0, 70, 40);

        savePngBot.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent ev) {
                FileDialog dlg = new FileDialog(previewWindow, SWT.SAVE);
                dlg.setText("Save PNG");
                dlg.setFilterExtensions(new String[]{"*.png", "*.*"});
                String selected = dlg.open();
                if (selected == null)
                    return;

                final ImageLoader loader = new ImageLoader();
                loader.data = new ImageData[]{m_imgdat};
                loader.save(selected, SWT.IMAGE_PNG);
            }
        });


        final Button openBot = new Button(editComp, SWT.PUSH);
        openBot.setText("Open");
        openBot.setBounds(0, 0, 100, 30);

        openBot.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dlg = new FileDialog(previewWindow, SWT.OPEN);
                dlg.setText("Open Model");
                dlg.setFilterExtensions(new String[]{"*.txt", "*.*"});
                final String selected = dlg.open();

                if (selected != null) {
                    openFile(selected);
                }
            }
        });


        canvas.addListener(SWT.Resize, e -> {
            m_rect = canvas.getClientArea();

            autoRender(canvas);
        });

        canvas.addPaintListener(e -> {
            GC gc = e.gc;
            if (m_imgdat == null) {
                gc.drawLine(0, 0, e.width, e.height);
                return;
            }
            Image img = new Image(display, m_imgdat);
            if (img != null) {
                gc.drawImage(img, 0, 0);
            }
            img.dispose();
        });

        previewWindow.open();
        final Point l = previewWindow.getLocation();
        editShell.setLocation(new Point(l.x + 650, l.y));
        editShell.open();

        while (!previewWindow.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }

    }

    @Nullable
    private Scene openSceneFile(final File sceneFile) {
        try {
            final SceneParser parser = new SceneParser(sceneFile);
            return parser.parse();
        } catch (final ParserException e) {
            log.error("Parser encountered an error", e);
            displayErrorMessageBox("Parsing exception occurred");
        } catch (IOException e) {
            log.error("Unable to open scene file", e);
        }
        return null;
    }

    private void displayErrorMessageBox(String message) {
        final MessageBox msgBox = new MessageBox(shell);
        msgBox.setText("Error");
        msgBox.setMessage(message);
        msgBox.open();
    }

    private void autoRender(Canvas canvas) {
        String img;

        if (cmdLineParams.length == 0)
            return; // nothing to do
        else
            img = cmdLineParams[0].toLowerCase();

        try {
            openFile(img);
            m_imgdat = new ImageData(m_rect.width, m_rect.height, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
            final RayTracer rayTracer = RayTracer.create(m_rect.width, m_rect.height, currentScene);
            final double[][][] pixels = rayTracer.render();
            setPixelsOnImage(pixels);
            canvas.redraw();

            final ImageLoader loader = new ImageLoader();
            loader.data = new ImageData[]{m_imgdat};
            loader.save(img.replace(".txt", "_new.png"), SWT.IMAGE_PNG);
            System.exit(0);
        } catch (final Exception e) {
            log.error("Error rendering scene", e);
        }
    }

    private void setPixelsOnImage(double[][][] pixels) {
        for (int x = 0; x < pixels.length; x++) {
            for (int y = 0; y < pixels[x].length; y++) {
                m_imgdat.setPixel(x, y, Utils.floatArrayToColorInt(pixels[x][y]));
            }
        }
    }

    private void openFile(final String filePath) {
        try {
            final Path path = Paths.get(filePath);
            final File file = path.toFile();

            if(file.exists() && file.canRead()) {
                m_sceneText.setText(new String(Files.readAllBytes(path)));
                currentScene = openSceneFile(file);
            } else {
                log.error("cannot read path: {}", filePath);
            }
        } catch (final InvalidPathException e) {
            log.error("file not found", e);
        } catch (final IOException e) {
            log.error(e.getMessage(), e);
        }
    }

}
