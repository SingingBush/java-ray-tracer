package ex02.raytracer;

import ex02.blas.MathUtils;
import ex02.entities.Camera;
import ex02.entities.Intersection;
import ex02.entities.Ray;
import ex02.entities.Scene;
import ex02.entities.Surface;
import ex02.entities.lights.Light;
import ex02.entities.primitives.Primitive;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class RayTracer {

    private static final Logger log = LoggerFactory.getLogger(RayTracer.class);

    private static final double EPSILON = 0.00000001F;
    private static final int MAX_REFLECTION_RECURSION_DEPTH = 8;

    private final Scene scene;
    private final int width;
    private final int height;

    // These are some of the camera's properties for easy (fast) access
    private double[] eye;
    private double[] lookAt;
    private double[] upDirection;
    private double[] rightDirection;
    private double[] viewplaneUp;
    private double[] direction;
    private double screenDist;
    private double pixelWidth;
    private double pixelHeight;
    private int superSampleWidth;

    private RayTracer(final int width, final int height, final Scene scene) {
        this.width = width;
        this.height = height;
        this.scene = scene;
    }

    public static RayTracer create(final int width, final int height, final Scene scene) {
        return new RayTracer(width, height, scene);
    }

    private Ray constructRayThroughPixel(int x, int y, double sampleXOffset, double sampleYOffset) throws Exception {
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

    // Finds an intersecting primitive. Will ignore the one specified by ignorePrimitive
    private Intersection findIntersection(final Ray ray, final Primitive ignorePrimitive) {
        if(ray == null) {
            throw new IllegalArgumentException("Ray should not be null");
        }

        // Start off with infinite distance and no intersecting primitive
        double minDistance = Double.POSITIVE_INFINITY;
        Primitive minPrimitive = null;

        for (final Primitive primitive : scene.getPrimitives()) {
            // lazy method call: intersect will be called according to the
            // implementing type of the primitive
            final double t = primitive.intersect(ray);

            // If we found a closer intersecting primitive, keep a reference to and it
            if (t < minDistance && t > EPSILON && primitive != ignorePrimitive) {
                minPrimitive = primitive;
                minDistance = t;
            }
        }

        // Return an intersection object with the closest intersecting primitive
        return new Intersection(minDistance, minPrimitive);
    }

    private double[] getColor(Ray ray, Intersection intersection, int recursionDepth) throws Exception {
        // Avoid infinite loops and help performance by limiting the recursion depth
        if (recursionDepth > MAX_REFLECTION_RECURSION_DEPTH) {
            return new double[]{0, 0, 0};
        }

        final Primitive primitive = intersection.getPrimitive();

        if (primitive == null) {
            return scene.getBackgroundColor();
        }

        final Surface surface = primitive.getSurface();
        double[] color = new double[3];
        double[] specular = surface.getSpecular();

        // Stretch the ray to the point of intersection
        ray.setMagnitude(intersection.getDistance());

        double[] pointOfIntersection = ray.getEndPoint();

        double[] diffuse = primitive.getColorAt(pointOfIntersection);
        if (diffuse == null) {
            log.warn("NULL diffuse color");
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
                if (visibleDiffuseLight > 0 && diffuse != null) {

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

    /**
     * Creates the pixel data that can be used to draw an image to a GUI or file
     * todo: use a custom exception such as RenderException
     * @return A 3 dimensional array which can also be thought of as a 2 dimensional array of RGB values.
     * @throws Exception in various parts of the render process.
     */
    public double[][][] render() throws Exception {
        final long start = System.nanoTime();

        scene.setCanvasSize(height, width);
        scene.postInit(null);

        // Copy some useful properties of the camera and scene
        final Camera camera = scene.getCamera();
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

        double[][][] pixels = new double[width][height][3];

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
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

                pixels[x][y][0] = color[0];
                pixels[x][y][1] = color[1];
                pixels[x][y][2] = color[2];
            }
        }
        final long time = System.nanoTime() - start;
        log.debug("Render took {} milliseconds", time / 1000_000);
        return pixels;
    }

}
