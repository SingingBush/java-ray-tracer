package ex02.entities.primitives;

import java.util.List;

import ex02.entities.IEntity;
import ex02.entities.Ray;
import ex02.entities.Surface;

/**
 * Abstract class from which all geometric primitives inherit
 */
public abstract class Primitive implements IEntity {

    private final Surface surface;

    private boolean active;

    public Primitive() {
        this.surface = new Surface();
        this.active = true;
    }

    /**
     * A generic intersection algorithm which returns the distance between the ray and the
     * implementing primitive.  Returns Double.POSITIVE_INFINITY if there is no intersection.
     *
     * @param ray
     * @return the distance between the ray and the implementing primitive or Double.POSITIVE_INFINITY
     */
    abstract public double intersect(Ray ray);

    public Surface getSurface() {
        return surface;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void postInit(List<IEntity> entities) {
        surface.postInit();
    }

    // Return a normal vector for the given point
    public abstract double[] getNormal(double[] point);

    // Return texture coordinates (2D parameterization) for the given point
    public abstract double[] getTextureCoords(double[] point);

    // Return the color at the given point (could be flat, texture, checkers)
    public double[] getColorAt(double[] point) {
        switch (surface.getTypeId()) {
            case Surface.TYPE_CHECKERS:
                return surface.getCheckersColor(getTextureCoords(point));
            case Surface.TYPE_TEXTURE:
                return surface.getTextureColor(getTextureCoords(point));
        }

        return surface.getDiffuse();
    }
}
