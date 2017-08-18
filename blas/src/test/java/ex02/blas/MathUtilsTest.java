package ex02.blas;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Samael Bate (singingbush)
 * created on 18/08/17
 */
public class MathUtilsTest {

    @Test
    public void sqr() throws Exception {
        assertEquals(4, MathUtils.sqr(2), 0);
        assertEquals(0.001*0.001, MathUtils.sqr(0.001), 0);
    }

    @Test
    public void sqrDiff() throws Exception {
        assertEquals(4, MathUtils.sqrDiff(4, 2), 0);
        assertEquals(6.25, MathUtils.sqrDiff(5, 2.5), 0);
        assertEquals(0.3600000000000001, MathUtils.sqrDiff(2.1, 1.5), 0);
    }

    @Test
    public void norm() throws Exception {
        assertEquals(3.4641016151377544, MathUtils.norm(new double[] {2.0, 2.0, 2.0}), 0);
    }

    @Test
    public void normalize() throws Exception {
        double[] vector = new double[] {1, 1, 1};
        MathUtils.normalize(vector);

        assertEquals(0.5773502691896258, vector[0], 0);
        assertEquals(0.5773502691896258, vector[1], 0);
        assertEquals(0.5773502691896258, vector[2], 0);
    }

    @Test
    public void crossProduct() throws Exception {
        final double[] crossProduct1 = MathUtils.crossProduct(new double[]{1, 1, 1}, new double[]{1, 1, 1});

        assertEquals(0.0, crossProduct1[0], 0);
        assertEquals(0.0, crossProduct1[1], 0);
        assertEquals(0.0, crossProduct1[2], 0);

        final double[] crossProduct2 = MathUtils.crossProduct(new double[]{1,2,3}, new double[]{8,9,10});

        assertEquals(-7.0, crossProduct2[0], 0);
        assertEquals(14.0, crossProduct2[1], 0);
        assertEquals(-7.0, crossProduct2[2], 0);
    }

}