package ex02.raytracer;

import ex02.entities.Scene;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.*;

public interface RayTracer {

    double EPSILON = 0.00000001F;
    int MAX_REFLECTION_RECURSION_DEPTH = 10;

    static RayTracer create(@NotNull final Scene scene) {
        return new RayTracerImpl(scene, MAX_REFLECTION_RECURSION_DEPTH);
    }

    static RayTracer create(@NotNull final Scene scene, int maxRecursionDepth) {
        if(maxRecursionDepth < 1 || maxRecursionDepth > MAX_REFLECTION_RECURSION_DEPTH) {
            throw new IllegalArgumentException("Max Recursion needs to be from 1 to "+MAX_REFLECTION_RECURSION_DEPTH);
        }
        return new RayTracerImpl(scene, maxRecursionDepth);
    }

    /**
     * Creates the pixel data that can be used to draw an image to a GUI or file
     * todo: use a custom exception such as RenderException
     * @param width the width of the image to be rendered
     * @param height the height of the image to be rendered
     * @return A 3 dimensional array which can also be thought of as a 2 dimensional array of RGB values.
     * @throws Exception in various parts of the render process.
     */
    double[][][] render(final int width, final int height) throws Exception;

    Future<double[][][]> renderTask(final int width, final int height);
}
