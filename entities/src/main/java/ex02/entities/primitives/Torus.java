package ex02.entities.primitives;

import java.util.List;

import ex02.blas.MathUtils;
import ex02.blas.RootFinder;
import ex02.entities.IEntity;
import ex02.entities.Ray;
import org.apache.commons.math3.linear.BlockRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

public class Torus extends Primitive implements Center {

    private double[] center;
    private double centralRadius;
    private double tubeRadius;
    private double centralRadiusSquare;
    private double tubeRadiusSquare;
    private double[] normal;

    // quatric polynomial coefficients
    private double a4, a3, a2, a1, a0;
    private double alpha, beta, gamma;

    @Override
    public double[] getNormal(double[] point) {
        double[] normal = {0, 0, 0};

        double innerComponent = MathUtils.sqr(point[0]) +
                MathUtils.sqr(point[1]) +
                MathUtils.sqr(point[2]) - tubeRadiusSquare - centralRadiusSquare;

        normal[0] = 4 * point[0] * innerComponent;
        normal[1] = 4 * point[1] * innerComponent;
        normal[2] = 4 * point[2] * innerComponent + (8 * centralRadiusSquare * MathUtils.sqr(point[2]));

        // Create the normal in matrix form
        final RealMatrix normalMatrix = new BlockRealMatrix(4, 1);
        normalMatrix.setEntry(0, 0, normal[0]);
        normalMatrix.setEntry(1, 0, normal[1]);
        normalMatrix.setEntry(2, 0, normal[2]);
        normalMatrix.setEntry(3, 0, 1);

        // Create the translation matrix
        final RealMatrix M = MatrixUtils.createRealIdentityMatrix(4);
        M.setEntry(0, 3, center[0]);
        M.setEntry(1, 3, center[1]);
        M.setEntry(2, 3, center[2]);

        // Translate the normal
        final RealMatrix Mnormal = M.multiply(normalMatrix);

        // Extract it from the matrix form
        double[] translatedNormal = {Mnormal.getEntry(0, 0), Mnormal.getEntry(1, 0), Mnormal.getEntry(2, 0)};

        MathUtils.normalize(translatedNormal);
        return translatedNormal;
    }

    @Override
    public double[] getTextureCoords(double[] point) {

        double[] referenceVector = {1, 0, 0};
        double[] pointOnRing = point.clone();
        MathUtils.addVectorAndMultiply(pointOnRing, MathUtils.oppositeVector(normal), tubeRadius);
        double[] vectorToRing = MathUtils.calcPointsDiff(center, pointOnRing);

        MathUtils.normalize(vectorToRing);

        double u = Math.acos(MathUtils.dotProduct(referenceVector, vectorToRing));
        if (MathUtils.dotProduct(MathUtils.crossProduct(referenceVector, vectorToRing), normal) < 0) {
            u = 2 * Math.PI - u;
        }

        u /= (2 * Math.PI);

        double[] fromRingToPoint = MathUtils.calcPointsDiff(pointOnRing, point);

        MathUtils.normalize(fromRingToPoint);

        double v = Math.acos(MathUtils.dotProduct(referenceVector, fromRingToPoint));
//	    if(MathUtils.dotProduct(MathUtils.crossProduct(referenceVector, fromRingToPoint), normal) < 0)
//	    {
//	    	v = 2 * Math.PI - v;
//	    }
        v /= (2 * Math.PI);


        return new double[]{u, v};
    }

    @Override
    public double intersect(Ray ray) {

        // Convert the ray position and direction to matrix style
        final RealMatrix rayPosition = new BlockRealMatrix(4, 1);
        final RealMatrix rayDirection = new BlockRealMatrix(4, 1);
        rayPosition.setEntry(0, 0, ray.getPosition()[0]);
        rayPosition.setEntry(1, 0, ray.getPosition()[1]);
        rayPosition.setEntry(2, 0, ray.getPosition()[2]);
        rayPosition.setEntry(3, 0, 1);
        rayDirection.setEntry(0, 0, ray.getDirection()[0]);
        rayDirection.setEntry(1, 0, ray.getDirection()[1]);
        rayDirection.setEntry(2, 0, ray.getDirection()[2]);
        rayDirection.setEntry(3, 0, 1);

        // Create the translation matrix
        final RealMatrix M = MatrixUtils.createRealIdentityMatrix(4);
        M.setEntry(0, 3, -center[0]);
        M.setEntry(1, 3, -center[1]);
        M.setEntry(2, 3, -center[2]);

        // Translate the position and direction vectors
        final RealMatrix MPosition = M.multiply(rayPosition);
        final RealMatrix MDirection = M.multiply(rayDirection);

        // Extract them from the matrix form
        double[] translatedPosition = {MPosition.getEntry(0, 0), MPosition.getEntry(1, 0), MPosition.getEntry(2, 0)};
        double[] translatedDirection = {MDirection.getEntry(0, 0), MDirection.getEntry(1, 0), MDirection.getEntry(2, 0)};

        MathUtils.normalize(translatedDirection);

        // Reconstruct the ray after translation
        ray.setPosition(translatedPosition);
        ray.setDirection(translatedDirection);

        // Prepare parameters to work with for solving the polynomial
        double[] p = ray.getPosition();
        double[] d = ray.getDirection();
        alpha = MathUtils.dotProduct(d, d);
        beta = 2 * MathUtils.dotProduct(p, d);
        gamma = MathUtils.dotProduct(p, p) - tubeRadiusSquare - centralRadiusSquare;

        // Quatric polynomial coefficients
        a4 = MathUtils.sqr(alpha);
        a3 = 2 * alpha * beta;
        a2 = (MathUtils.sqr(beta)) + (2 * alpha * gamma) + (4 * centralRadiusSquare * MathUtils.sqr(d[2]));
        a1 = (2 * beta * gamma) + (8 * centralRadiusSquare * p[2] * d[2]);
        a0 = MathUtils.sqr(gamma) + (4 * centralRadiusSquare * MathUtils.sqr(p[2])) - (4 * centralRadiusSquare * tubeRadiusSquare);

        // Solve quatric
        double[] coefficients = {a0, a1, a2, a3, a4};
        double[] roots = RootFinder.SolveQuartic(coefficients);

        if (roots == null || roots.length == 0) return Double.POSITIVE_INFINITY;

        // Find the closest intersecting point
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < roots.length; i++) {
            if (roots[i] < min) {
                min = roots[i];
            }
        }

        return (min == Double.POSITIVE_INFINITY) ? Double.POSITIVE_INFINITY : min;
    }


    @Override
    public void postInit(List<IEntity> entities) {
        super.postInit(entities);

        // normalize the normal vector
        MathUtils.normalize(normal);

        // Preprocess some stuff
        centralRadiusSquare = MathUtils.sqr(centralRadius);
        tubeRadiusSquare = MathUtils.sqr(tubeRadius);
    }


    @Override
    public void setParameter(String name, String[] args) throws Exception {
        if (getSurface().parseParameter(name, args)) return;
        if ("center".equals(name)) center = MathUtils.parseVector(args);
        if ("central-radius".equals(name)) centralRadius = Double.parseDouble(args[0]);
        if ("tube-radius".equals(name)) tubeRadius = Double.parseDouble(args[0]);
        if ("normal".equals(name)) normal = MathUtils.parseVector(args);
    }

    @Override
    public double[] getCenter() {
        return center;
    }

    @Override
    public void setCenter(double[] center) {
        this.center = center;
    }
}
