package ex02;

import java.io.*;
import java.util.Arrays;

import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;

import ex02.blas.MathUtils;
import ex02.entities.Camera;
import ex02.entities.Intersection;
import ex02.entities.Ray;
import ex02.entities.Scene;
import ex02.entities.Surface;
import ex02.entities.lights.Light;
import ex02.entities.primitives.Primitive;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RayTracer {

    private static final Logger LOG = LogManager.getLogger(RayTracer.class);

    public final double EPSILON = 0.00000001F;
    public final int MAX_REFLECTION_RECURSION_DEPTH = 8;

    public static String workingDirectory;

    public String cmdLineParams[];

    Scene scene;

    /**
     * @param args
     */
    public static Display display;
    public static Shell shell;

    // These are some of the camera's properties for easy (fast) access
    double[] eye;
    double[] lookAt;
    double[] upDirection;
    double[] rightDirection;
    double[] viewplaneUp;
    double[] direction;
    double screenDist;
    double pixelWidth;
    double pixelHeight;
    int superSampleWidth;


    public static void main(String[] args) {
        LOG.debug("Ray Tracer started with args: {}", Arrays.toString(args));

        display = new Display();
        shell = new Shell(display);
        RayTracer tracer = new RayTracer();
        tracer.cmdLineParams = args.clone();
        tracer.runMain(display);

        display.dispose();
    }

    public void autoRender(Canvas canvas) {
        String img;

        if (cmdLineParams.length == 0)
            return; // nothing to do
        else
            img = cmdLineParams[0].toLowerCase();

        try {
            openFile(img);
            m_imgdat = new ImageData(m_rect.width, m_rect.height, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
            renderTo(m_imgdat, canvas);
            ImageLoader loader = new ImageLoader();
            loader.data = new ImageData[]{m_imgdat};
            loader.save(img.replace(".txt", "_new.png"), SWT.IMAGE_PNG);
            System.exit(0);

        } catch (Exception ex) {
            System.out.println("Error Rendering scene: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public Ray constructRayThroughPixel(int x, int y, double sampleXOffset, double sampleYOffset) throws Exception {
        Ray ray = new Ray(eye, direction, screenDist);
        double[] endPoint = ray.getEndPoint();

        double upOffset = -1 * (y - (scene.getCanvasHeight() / 2) - (sampleYOffset / superSampleWidth)) * pixelHeight;
        double rightOffset = (x - (scene.getCanvasWidth() / 2) + (sampleXOffset / superSampleWidth)) * pixelWidth;

        MathUtils.addVectorAndMultiply(endPoint, viewplaneUp, upOffset);
        MathUtils.addVectorAndMultiply(endPoint, rightDirection, rightOffset);

        ray.setDirection(MathUtils.calcPointsDiff(eye, endPoint));
        ray.normalize();
        return ray;
    }

    // Finds an intersecting primitive. Will ignore the one specificed by ignorePrimitive
    public Intersection findIntersection(Ray ray, Primitive ignorePrimitive) {
        // Start off with infinite distance and no intersecting primitive
        double minDistance = Double.POSITIVE_INFINITY;
        Primitive minPrimitive = null;

        for (Primitive primitive : scene.getPrimitives()) {
            // lazy method call: intersect will be called according to the
            // implementing type of the primitive
            double t = primitive.intersect(ray);

            // If we found a closer intersecting primitive, keep a reference to and it
            if (t < minDistance && t > EPSILON && primitive != ignorePrimitive) {
                minPrimitive = primitive;
                minDistance = t;
            }
        }

        // Return an intersection object with the closest intersecting primitive
        return new Intersection(minDistance, minPrimitive);
    }

    public double[] getColor(Ray ray, Intersection intersection, int recursionDepth) throws Exception {
        // Avoid infinite loops and help performance by limiting the recursion depth
        if (recursionDepth > MAX_REFLECTION_RECURSION_DEPTH)
            return new double[]{0, 0, 0};

        Primitive primitive = intersection.getPrimitive();

        if (primitive == null)
            return scene.getBackgroundColor();

        Surface surface = primitive.getSurface();
        double[] color = new double[3];
        double[] specular = surface.getSpecular();

        // Stretch the ray to the point of intersection
        ray.setMagnitude(intersection.getDistance());

        double[] pointOfIntersection = ray.getEndPoint();

        double[] diffuse = primitive.getColorAt(pointOfIntersection);
        if (diffuse == null) {
            System.err.print("NUL");
        }

        // Stretch the ray to the point of intersection - 1 to we can get a viewing vector
        ray.setMagnitude(intersection.getDistance() - 1);

        // Obtain the normal at the point of intersection
        double[] normal = primitive.getNormal(pointOfIntersection);

        // Shoot rays towards each light source and see if it's visible
        for (Light light : scene.getLights()) {
            double[] vectorToLight = light.getVectorToLight(pointOfIntersection);

            Ray rayToLight = new Ray(pointOfIntersection, vectorToLight, 1);
            rayToLight.normalize();

            // Light is visible if there's no intersection with an object at least epsilon away
            double distanceToBlockingPrimitive = findIntersection(rayToLight, null).getDistance();
            double distanceToLight = MathUtils.norm(MathUtils.calcPointsDiff(pointOfIntersection, light.getPosition()));

            boolean lightVisible = distanceToBlockingPrimitive <= EPSILON
                    || distanceToBlockingPrimitive >= distanceToLight;

            if (lightVisible) {
                // Measure the distance to the light and find the amount of light hitting the primitive
                double[] amountOfLightAtIntersection = light.getAmountOfLight(pointOfIntersection);

                // The amount of light visible on the surface, determined by the angle to the light source
                double visibleDiffuseLight = MathUtils.dotProduct(vectorToLight, normal);
                if (visibleDiffuseLight > 0) {

                    // Diffuse
                    color[0] += diffuse[0] * amountOfLightAtIntersection[0] * visibleDiffuseLight;
                    color[1] += diffuse[1] * amountOfLightAtIntersection[1] * visibleDiffuseLight;
                    color[2] += diffuse[2] * amountOfLightAtIntersection[2] * visibleDiffuseLight;
                }

                // Specular

                // Find the reflection around the normal
                double[] reflectedVectorToLight = MathUtils.reflectVector(vectorToLight, normal);
                MathUtils.normalize(reflectedVectorToLight);

                double visibleSpecularLight = MathUtils.dotProduct(reflectedVectorToLight, ray.getDirection());

                if (visibleSpecularLight < 0) {
                    visibleSpecularLight = Math.pow(Math.abs(visibleSpecularLight), surface.getShininess());

                    color[0] += specular[0] * amountOfLightAtIntersection[0] * visibleSpecularLight;
                    color[1] += specular[1] * amountOfLightAtIntersection[1] * visibleSpecularLight;
                    color[2] += specular[2] * amountOfLightAtIntersection[2] * visibleSpecularLight;
                }
            }
        }

        // Ambient
        double[] sceneAmbient = scene.getAmbientLight();
        double[] surfaceAmbient = surface.getAmbient();

        color[0] += sceneAmbient[0] * surfaceAmbient[0];
        color[1] += sceneAmbient[1] * surfaceAmbient[1];
        color[2] += sceneAmbient[2] * surfaceAmbient[2];

        // Emission
        double[] surfaceEmission = surface.getEmission();

        color[0] += surfaceEmission[0];
        color[1] += surfaceEmission[1];
        color[2] += surfaceEmission[2];

        // Reflection Ray
        double[] reflectionDirection = MathUtils.reflectVector(MathUtils.oppositeVector(ray.getDirection()), normal);
        Ray reflectionRay = new Ray(pointOfIntersection, reflectionDirection, 1);
        reflectionRay.normalize();

        Intersection reflectionIntersection = findIntersection(reflectionRay, null);
        double[] reflectionColor = getColor(reflectionRay, reflectionIntersection, recursionDepth + 1);

        MathUtils.addVectorAndMultiply(color, reflectionColor, surface.getReflectance());

        return color;
    }

    void renderTo(ImageData dat, Canvas canvas) throws Parser.ParseException, Exception {
        Parser parser = new Parser(display);

        try {
            parser.parse(new StringReader(m_sceneText.getText()));
        } catch (Exception e) {
            MessageBox msgBox = new MessageBox(shell);
            msgBox.setText("Error");
            msgBox.setMessage("Parsing exception occured");
            e.printStackTrace();
            msgBox.open();
            return;
        }

        scene = parser.getScene();
        scene.setCanvasSize(dat.height, dat.width);
        scene.postInit(null);

        // Copy some usefull properties of the camera and scene
        Camera camera = scene.getCamera();
        eye = camera.getEye();
        lookAt = camera.getLookAt();
        screenDist = camera.getScreenDist();
        pixelWidth = camera.getScreenWidth() / scene.getCanvasWidth();
        pixelHeight = scene.getCanvasWidth() / scene.getCanvasHeight() * pixelWidth;
        upDirection = camera.getUpDirection();
        rightDirection = camera.getRightDirection();
        superSampleWidth = scene.getSuperSampleWidth();
        viewplaneUp = camera.getViewplaneUp();
        direction = camera.getDirection();

        GC gc = new GC(canvas);
        gc.fillRectangle(m_rect);

        for (int y = 0; y < dat.height; ++y) {
            for (int x = 0; x < dat.width; ++x) {
                int hits = 0;
                double[] color = new double[3];

                // Supersampling loops
                for (int k = 0; k < superSampleWidth; k++) {
                    for (int l = 0; l < superSampleWidth; l++) {
                        double[] sampleColor = null;

                        // Create the ray
                        Ray ray = constructRayThroughPixel(x, y, k, l);

                        // Find the intersecting primitive
                        Intersection intersection = findIntersection(ray, null);

                        // If we hit something, get its color
                        if (intersection.getPrimitive() != null) {
                            hits++;
                            sampleColor = getColor(ray, intersection, 1);
                            MathUtils.addVector(color, sampleColor);

                            ray.setMagnitude(intersection.getDistance());
                        }
                    }
                }

                // If we didn't anything in any of the samples, use the background color
                if (hits == 0) {
                    color = scene.getBackgroundAt(x, y);
                } else {
                    // Average the cumulative color values
                    MathUtils.multiplyVectorByScalar(color, 1F / hits);
                }

                // Plot the pixel
                Color cc = Utils.floatArrayToColor(color);

                dat.setPixel(x, y, Utils.floatArrayToColorInt(color));

                gc.setForeground(cc);
                gc.drawPoint(x, y);
                m_imgdat.setPixel(x, y, m_imgdat.palette.getPixel(cc.getRGB()));
                cc.dispose(); // Only dispose if we're not using the scene background
            }
        }
    }

    public static String readTextFile(Reader in) throws IOException {
        StringBuilder sb = new StringBuilder(1024);
        BufferedReader reader = new BufferedReader(in);

        char[] chars = new char[1024];
        int numRead;
        while ((numRead = reader.read(chars)) > -1) {
            sb.append(String.valueOf(chars, 0, numRead));
        }
        return sb.toString();
    }

    void openFile(String filename) {
        try {
            workingDirectory = new File(filename).getParent() + File.separator;
            final Reader fr = new FileReader(filename);
            m_sceneText.setText(readTextFile(fr));
        } catch (final FileNotFoundException e) {
            LOG.error("file not found", e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    org.eclipse.swt.graphics.Rectangle m_rect;
    ImageData m_imgdat;

    // GUI
    Text m_sceneText;


    void runMain(final Display display) {
        Shell editShell = new Shell(display);
        editShell.setText("Input");
        editShell.setSize(300, 550);
        GridLayout gridEdit = new GridLayout();
        editShell.setLayout(gridEdit);

        Composite editComp = new Composite(editShell, SWT.NONE);
        GridData ld = new GridData();
        ld.heightHint = 30;
        editComp.setLayoutData(ld);

        m_sceneText = new Text(editShell, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
        ld = new GridData(GridData.FILL_BOTH);
        m_sceneText.setLayoutData(ld);
        Font fixed = new Font(display, "Courier New", 10, 0);
        m_sceneText.setFont(fixed);


        final Shell shell = new Shell(display);
        shell.setText("Ray Tracer Ex");
        shell.setSize(600, 500);
        GridLayout gridLayout = new GridLayout();

        shell.setLayout(gridLayout);

        // the canvas we'll be drawing on.
        final Canvas canvas = new Canvas(shell, SWT.BORDER | SWT.NO_REDRAW_RESIZE);
        ld = new GridData(GridData.FILL_BOTH);
        canvas.setLayoutData(ld);

        Composite comp = new Composite(shell, SWT.NONE);
        ld = new GridData();
        ld.heightHint = 45;
        comp.setLayoutData(ld);

        // "Render Button"
        Button renderBot = new Button(comp, SWT.PUSH);
        renderBot.setText("Render");
        renderBot.setSize(150, 40);


        renderBot.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent ev) {
                try {
                    m_imgdat = new ImageData(m_rect.width, m_rect.height, 24, new PaletteData(0xFF0000, 0xFF00, 0xFF));
                    renderTo(m_imgdat, canvas);
                } catch (Parser.ParseException e) {
                    System.out.println("Error Parsing text: " + e.getMessage());
                } catch (Exception e) {
                    System.out.println("Error Rendering scene: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });

        Button savePngBot = new Button(comp, SWT.PUSH);
        savePngBot.setText("Save PNG");
        savePngBot.setBounds(250, 0, 70, 40);
        savePngBot.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent ev) {
                FileDialog dlg = new FileDialog(shell, SWT.SAVE);
                dlg.setText("Save PNG");
                dlg.setFilterExtensions(new String[]{"*.png", "*.*"});
                String selected = dlg.open();
                if (selected == null)
                    return;

                ImageLoader loader = new ImageLoader();
                loader.data = new ImageData[]{m_imgdat};
                loader.save(selected, SWT.IMAGE_PNG);
            }
        });


        Button openBot = new Button(editComp, SWT.PUSH);
        openBot.setText("Open");
        openBot.setBounds(0, 0, 100, 30);

        openBot.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileDialog dlg = new FileDialog(shell, SWT.OPEN);
                dlg.setText("Open Model");
                dlg.setFilterExtensions(new String[]{"*.txt", "*.*"});
                String selected = dlg.open();
                if (selected != null)
                    openFile(selected);

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

        shell.open();
        Point l = shell.getLocation();
        editShell.setLocation(new Point(l.x + 650, l.y));
        editShell.open();

        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) display.sleep();
        }


    }

}
