package ex02.raytracer;

import ex02.entities.Scene;
import ex02.raytracer.parser.SceneParser;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * @author Samael Bate (singingbush)
 * created on 29/06/2020
 */
class RayTracerTest {

    private Scene scene;

    @BeforeEach
    void setUp() throws Exception {
        final SceneParser parser = new SceneParser(loadTestResource("scenes/spheres.txt"));
        this.scene = parser.parse();
    }

    @Test
    @DisplayName("Test a 200x200 pixel render of a scene")
    @Timeout(value = 280L, unit = TimeUnit.MILLISECONDS)
    @Tag("perf")
    void render() throws Exception {
        final double[][][] pixels = RayTracer
                .create(this.scene)
                .render(200, 200);

        assertEquals(200, pixels.length);

        for (int i = 0; i < pixels.length; i++) {
            assertEquals(200, pixels[i].length);
            assertEquals(3, pixels[i][0].length, "Each Pixel has RGB");
        }
    }

    private Reader loadTestResource(final String resource) throws IOException {
        //final InputStream stream = ClassLoader.getSystemResourceAsStream(resource);
        final InputStream stream = this.getClass().getModule().getResourceAsStream(resource);
        //final InputStream stream = this.getClass().getClassLoader().getResourceAsStream(resource);
        return new InputStreamReader(stream);
    }
}
