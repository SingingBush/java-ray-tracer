package ex02.entities;

import ex02.blas.Vector3D;

import java.util.Arrays;

/**
 * Pretty-name class for Vector3D, used to represent rays (position, direction, magnitude)
 */
public class Ray extends Vector3D {

    public Ray(double[] position, double[] direction, double magnitude) throws Exception {
        super(position, direction, magnitude);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Ray{");
        sb.append("position=").append(Arrays.toString(getPosition()));
        sb.append(", direction=").append(Arrays.toString(getDirection()));
        sb.append(", magnitude=").append(getMagnitude());
        sb.append('}');
        return sb.toString();
    }
}
