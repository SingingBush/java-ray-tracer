package ex02.entities;

import ex02.entities.primitives.Primitive;

/**
 * Helper class representing an intersection
 */
public class Intersection {

    private Primitive primitive;
    private double distance;

    public Intersection(double distance, Primitive primitive) {
        this.primitive = primitive;
        this.distance = distance;
    }

    public Primitive getPrimitive() {
        return primitive;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Intersection{");
        sb.append("primitive=").append(primitive);
        sb.append(", distance=").append(distance);
        sb.append('}');
        return sb.toString();
    }
}
