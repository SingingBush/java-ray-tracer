package ex02.blas;


/**
 * Static mathematical utility class for linear algebra and other things
 */
public class MathUtils {

    /**
     * This was originally in the Parser class of the main package. Moved out as
     * that was the wrong place for it.
     * @param vecElems a String array containing 3 double values
     * @return Vector
     */
    public static double[] parseVector(final String[] vecElems) {
        if (vecElems == null || vecElems.length != 3) {
            throw new IllegalArgumentException("Invalid vector string");
        }

        double[] result = new double[3];

        for (int i = 0; i < 3; i++) {
            result[i] = Double.parseDouble(vecElems[i]);
        }

        return result;
    }

	// Returns the square of a
	public static double sqr(double a) {
		return a * a;
	}
	
	// Returns the square of a - b
	public static double sqrDiff(double a, double b) {
		return (a - b) * (a - b);
	}
	
	// Vector addition, adds addition to vec
	public static void addVector(double[] vec, double addition[]) {
        checkSize(vec);
        checkSize(addition);

		vec[0] += addition[0]; 
		vec[1] += addition[1];
		vec[2] += addition[2];
	}
	
	// Multiplies addition by a scalar and then adds the result to vec
	public static void addVectorAndMultiply(double[] vec, double addition[], double scalar) {
        checkSize(vec);
        checkSize(addition);

		vec[0] += addition[0] * scalar; 
		vec[1] += addition[1] * scalar;
		vec[2] += addition[2] * scalar;
	}
	
	// Multiplies vec by a scalar
	public static void multiplyVectorByScalar(double[] vec, double scalar) {
	    checkSize(vec);

		vec[0] *= scalar; 
		vec[1] *= scalar;
		vec[2] *= scalar;
	}
	
	
	/**
	 * Calculates a dot product between two given vectors
	 * @param vec1 Vector
	 * @param vec2 Vector
	 * @return dot product
	 */
	public static double dotProduct(double[] vec1, double[] vec2) {
        checkSize(vec1);
        checkSize(vec2);

		return vec1[0] * vec2[0]  +  vec1[1] * vec2[1]  +  vec1[2] * vec2[2] ;
	}	
	
	/**
	 * Calculates the difference between two point in 3D space
	 * @param p1 Vector
	 * @param p2 Vector
	 * @return Vector (the difference)
	 */
	public static double[] calcPointsDiff(double[] p1, double[] p2) {
        checkSize(p1);
        checkSize(p2);

		return new double [] { p2[0] - p1[0] , p2[1] - p1[1] , p2[2] - p1[2] };
	}

	// Returns the norm of the difference between this vector's position point and another point
	public static double norm(double[] p) {
        checkSize(p);

		return Math.sqrt(sqr(p[0]) + sqr(p[1]) + sqr(p[2]));
	}

    /**
     * @param vec a Vector (or point)
     */
	public static void normalize(double[] vec) {
        checkSize(vec);

		double norm = norm(vec);
		
		if (norm == 0)
			return;
		
		vec[0] /= norm;
		vec[1] /= norm;
		vec[2] /= norm;
	}

    // Returns the cross product of 2 vectors
	public static double[] crossProduct(double[] d1, double[] d2) {
        checkSize(d1);
        checkSize(d2);

        return new double[]{ (d1[1] * d2[2]) - (d1[2] * d2[1]), (d1[2] * d2[0]) - (d1[0] * d2[2]), (d1[0] * d2[1]) - (d1[1] * d2[0]) };
	}	
	
	// Reflects a vector around a normal vector. both vectors are assumed to have the same shift from the origin
	public static double[] reflectVector(double[] vec, double[] normal) {
        checkSize(vec);
        checkSize(normal);

		double dotProduct = MathUtils.dotProduct(vec, normal);

        return new double[] { -vec[0] + 2 * normal[0] * dotProduct,
                                  -vec[1] + 2 * normal[1] * dotProduct,
                                  -vec[2] + 2 * normal[2] * dotProduct };
	}
	
	// Returns the vector opposite to vec
	public static double[] oppositeVector(double[] vec) {
        return new double[] { -vec[0], -vec[1], -vec[2] };
	}
	
	/**
	 * Given three points, this method returns true if they are collinear, and false otherwise.
	 * @param p0 Vector
	 * @param p1 Vector
	 * @param p2 Vector
	 * @return true if they are collinear
	 */
	public static boolean arePointsCollinear(double[] p0, double[] p1, double[] p2) {
        checkSize(p0);
        checkSize(p1);
        checkSize(p2);

		// coefficients for testing collinearity
		double a,b,c;
		
		// Define the vectors between pairs of the given points 
		double[] vec1 = { p1[0] - p0[0] , p1[1] - p0[1] , p1[2] - p0[2] };
		double[] vec2 = { p2[0] - p0[0] , p2[1] - p0[1] , p2[2] - p0[2] };

		a = vec1[0] / vec2[0];
		b = vec1[1] / vec2[1];
		c = vec1[2] / vec2[2];

		// If all coefficients are equal then some scalar exists which scales between the vectors
		// e.g. they are linearly dependent and all 3 points are on the same line
		if (a == b  && b == c) return true;
		
		return false;
	}

	
	/**
	 * Solves a quadratic equation with coefficients a, b, c. <br /> 
	 * Returns Double.POSITIVE_INFINITY if no roots exist. <br />
	 * Returns (-b) / (2 * a) if only one root exists. <br />
	 * Returns the minimum root if two roots exist.
	 * @param a coefficient
	 * @param b coefficient
	 * @param c coefficient
	 * @return the minimum root if two roots exist
	 */
	public static double[] solveQuadraticEquation(double a, double b, double c) {
		
		double[] roots = new double[2];
		if(a == 0)
		{
			roots[0] = -c / b;
		}
		else
		{
			double discriminant = MathUtils.sqr(b) - 4 * a * c;
	
			if (discriminant < 0)
			{
				roots[0] = Double.POSITIVE_INFINITY;
			}
			else if(discriminant == 0)
			{
				roots[0] =  (-b) / (2 * a);
			}
			else
			{
				discriminant = Math.sqrt(discriminant);
				double denominator = 2 * a;
				roots[0] = (-b + discriminant) / (denominator);
				roots[1] = (-b - discriminant) / (denominator);
	
				// Return the closest intersecting point
				// The primitives are convex so the closer point necessarily occludes the the farther point
//				if(root1 > 0 && root2 > 0) return Math.min(root1, root2);
				
				// The camera is positioned inside the geometric primitive or the primitive is completely behind us
//				return Math.max(root1, root2);
				
				// TODO: need to deal with negative roots?
			}
		}
		return roots;
	}

	
	/**
	 * Multiplies the given vector by the given scalar
	 * @param vec Vector (or point)
	 * @param t	  Scalar
	 * @return Vector
	 */
	public static double[] multiplyScalar(double[] vec, double t) {
        checkSize(vec);

		vec[0] *= t;
		vec[1] *= t;
		vec[2] *= t;
		return vec;
	}

	/**
	 * Add two points in 3D
	 * @param a Vector (or point)
	 * @param b Vector (or point)
	 * @return Vector
	 */
	public static double[] addPoints(double[] a, double[] b) {
        checkSize(a);
        checkSize(b);

        return new double[]{ a[0] + b[0] , a[1] + b[1] , a[2] + b[2] };
	}
	
	public static double[] subtractPoints(double[] a, double[] b) {
	    checkSize(a);
	    checkSize(b);

		double[] c = { a[0] - b[0] , a[1] - b[1] , a[2] - b[2] };
		return c;
	}

    private static void checkSize(double[] vec) {
        if(vec == null || vec.length != 3) {
            throw new IllegalArgumentException("vector should have 3 values to prevent ArrayIndexOutOfBoundsException");
        }
    }

}
