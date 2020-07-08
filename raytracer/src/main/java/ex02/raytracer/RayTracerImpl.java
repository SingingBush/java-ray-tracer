package ex02.raytracer;

import ex02.blas.MathUtils;
import ex02.entities.*;
import ex02.entities.lights.Light;
import ex02.entities.primitives.Primitive;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RayTracerImpl implements RayTracer {

    private static final Logger log = LoggerFactory.getLogger(RayTracerImpl.class);

    private final Scene scene;
    private final int maxRecursionDepth;
    private final Camera camera;

    private double pixelWidth;
    private double pixelHeight;

    RayTracerImpl(final Scene scene, int maxRecursionDepth) {
        this.scene = scene;
        this.maxRecursionDepth = maxRecursionDepth;

        try {
            scene.postInit(null);
        } catch (final Exception e) {}

        this.camera = scene.getCamera();
    }

    private Ray constructRayThroughPixel(final int x, final int y, final double sampleXOffset, final double sampleYOffset) {
        final Ray ray = camera.getRay();
        final double[] endPoint = ray.getEndPoint();

        final double upOffset = -1 * (y - (scene.getCanvasHeight() / 2.0) - (sampleYOffset / scene.getSuperSampleWidth())) * pixelHeight;
        final double rightOffset = (x - (scene.getCanvasWidth() / 2.0) + (sampleXOffset / scene.getSuperSampleWidth())) * pixelWidth;

        MathUtils.addVectorAndMultiply(endPoint, camera.getViewplaneUp(), upOffset);
        MathUtils.addVectorAndMultiply(endPoint, camera.getRightDirection(), rightOffset);

        ray.setDirection(MathUtils.calcPointsDiff(camera.getEye(), endPoint));
        ray.normalize();
        return ray;
    }

    private void setRenderSize(final int width, final int height) {
        this.scene.setCanvasSize(height, width);
        this.pixelWidth = this.camera.getScreenWidth() / this.scene.getCanvasWidth();
        this.pixelHeight = this.scene.getCanvasWidth() / this.scene.getCanvasHeight() * this.pixelWidth;
    }

    // Finds an intersecting primitive. Will ignore the one specified by ignorePrimitive
    private Intersection findIntersection(@NotNull final Ray ray, @Nullable final Primitive ignorePrimitive) {
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

    private double[] getColor(Ray ray, Intersection intersection, int recursionDepth) {
        // Avoid infinite loops and help performance by limiting the recursion depth
        if (recursionDepth > this.maxRecursionDepth) {
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
        for (final Light light : scene.getLights()) {
            final double[] vectorToLight = light.getVectorToLight(pointOfIntersection);

            final Ray rayToLight = new Ray(pointOfIntersection, vectorToLight, 1);
            rayToLight.normalize();

            // Light is visible if there's no intersection with an object at least epsilon away
            final double distanceToBlockingPrimitive = findIntersection(rayToLight, null).getDistance();
            final double distanceToLight = MathUtils.norm(MathUtils.calcPointsDiff(pointOfIntersection, light.getPosition()));

            final boolean lightVisible = distanceToBlockingPrimitive <= EPSILON
                    || distanceToBlockingPrimitive >= distanceToLight;

            if (lightVisible) {
                // Measure the distance to the light and find the amount of light hitting the primitive
                final double[] amountOfLightAtIntersection = light.getAmountOfLight(pointOfIntersection);

                // The amount of light visible on the surface, determined by the angle to the light source
                final double visibleDiffuseLight = MathUtils.dotProduct(vectorToLight, normal);
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
        final double[] reflectionDirection = MathUtils.reflectVector(MathUtils.oppositeVector(ray.getDirection()), normal);
        final Ray reflectionRay = new Ray(pointOfIntersection, reflectionDirection, 1);
        reflectionRay.normalize();

        final Intersection reflectionIntersection = findIntersection(reflectionRay, null);
        final double[] reflectionColor = getColor(reflectionRay, reflectionIntersection, recursionDepth + 1);

        MathUtils.addVectorAndMultiply(color, reflectionColor, surface.getReflectance());

        return color;
    }

    /**
     * Creates the pixel data that can be used to draw an image to a GUI or file
     * todo: use a custom exception such as RenderException
     * @param width the width of the image to be rendered
     * @param height the height of the image to be rendered
     * @return A 3 dimensional array which can also be thought of as a 2 dimensional array of RGB values.
     * @throws Exception in various parts of the render process.
     */
    @Override
    public double[][][] render(final int width, final int height) throws Exception {
        setRenderSize(width, height);

        final long start = System.nanoTime();

        double[][][] pixels = new double[width][height][3];

        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                pixels[x][y] = calculatePixelColor(x, y);
            }
        }

        final long time = System.nanoTime() - start;
        log.debug("Render took {} milliseconds", time / 1000_000);
        return pixels;
    }

    private double[] calculatePixelColor(final int x, final int y) {
        int hits = 0;
        double[] color = new double[3];

        final int superSampleWidth = scene.getSuperSampleWidth();

        // Supersampling loops
        for (int k = 0; k < superSampleWidth; k++) {
            for (int l = 0; l < superSampleWidth; l++) {
                double[] sampleColor = null;

                // Create the ray
                final Ray ray = constructRayThroughPixel(x, y, k, l);

                // Find the intersecting primitive
                final Intersection intersection = findIntersection(ray, null);

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

        return new double[] {
                Math.min(color[0], 1.0d),
                Math.min(color[1], 1.0d),
                Math.min(color[2], 1.0d)
        };
    }
}
