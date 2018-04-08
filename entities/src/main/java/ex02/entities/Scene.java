package ex02.entities;


import java.util.ArrayList;
import java.util.List;

import ex02.blas.MathUtils;
import ex02.entities.lights.Light;
import ex02.entities.primitives.Primitive;

/**
 * This class represents a 3D scene model.
 * The scene objects holds a collection of primitives which are part of the scene.
 *
 * @author Gur
 */
public class Scene implements IEntity {

    private List<Primitive> primitives = new ArrayList<>();
    private List<Light> lights = new ArrayList<>();
    private Camera camera;

    private double[] backgroundColor = {0, 0, 0};
    private double[] ambientLight = {0, 0, 0};
    private int superSampleWidth = 1;

    int textureWidth;
    int textureHeight;
    double[][][] backgroundTexture = null;

    int canvasWidth;
    int canvasHeight;

    @Override
    public void postInit(List<IEntity> entities) throws Exception {
    }

    // Returns the color of the background (or texture) at the given coordinates
    public double[] getBackgroundAt(int x, int y) {
        if (backgroundTexture != null) {
            int textureX = (int) Math.round((double) x / canvasWidth * textureWidth);
            int textureY = (int) Math.round((double) y / canvasHeight * textureHeight);

            return backgroundTexture[textureY][textureX];
        } else {
            return backgroundColor;
        }

    }

    // Accepts a list of entities and puts each one in its list or member
    public void setEntities(final List<IEntity> entities) throws Exception {
        for (IEntity entity : entities) {

            // Test to see if it's a primitive
            if (entity instanceof Primitive) {
                primitives.add((Primitive) entity);
                continue;
            }

            // Test to see if it's a light source
            if (entity instanceof Light) {
                lights.add((Light) entity);
                continue;
            }

            // Test to see if it's the camera
            if (entity instanceof Camera) {
                this.camera = (Camera) entity;

                continue;
            }

            // Test to see if it's the scene object. if it is, just ignore it
            if (entity instanceof Scene) {
                continue;
            }

            // We've reached some unknown entity, through an exception
            throw new Exception("Unknown entity type found: " + entity.getClass().getCanonicalName());
        }
    }

    public void setCanvasSize(int height, int width) {
        this.canvasHeight = height;
        this.canvasWidth = width;
    }

    ///////////////////////////  Getters & Setters  ///////////////////////////////

    public List<Primitive> getPrimitives() {
        return primitives;
    }

    public void setPrimitives(List<Primitive> listOfPrimitives) {
        this.primitives = listOfPrimitives;
    }

    public double[] getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(double[] backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public double[] getAmbientLight() {
        return ambientLight;
    }

    public int getSuperSampleWidth() {
        return superSampleWidth;
    }

    /**
     * Read parameters into members
     * @param name parameter name
     * @param args parameter value
     * @throws Exception
     */
    @Override
    public void setParameter(String name, String[] args) throws Exception {
        if ("background-col".equals(name)) backgroundColor = MathUtils.parseVector(args);
//        if ("background-tex".equals(name)) {
//            backgroundTexture = Utils.loadTexture(args[0]);
//            textureWidth = backgroundTexture.length;
//            textureHeight = backgroundTexture[0].length;
//        }
        if ("ambient-light".equals(name)) ambientLight = MathUtils.parseVector(args);
        if ("super-samp-width".equals(name)) superSampleWidth = Integer.parseInt(args[0]);
    }

    public List<Light> getLights() {
        return lights;
    }

    public Camera getCamera() {
        return camera;
    }

    public int getCanvasHeight() {
        return canvasHeight;
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }
}
