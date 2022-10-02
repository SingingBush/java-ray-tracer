package ex02.raytracer.parser;

import ex02.entities.Scene;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Samael Bate (singingbush)
 * created on 18/08/17
 */
class ParserTest {

    @Test
    void testParseSpheresSceneFile() throws ParserException, IOException {
        Parser parser = new SceneParser(loadTestResource("scenes/spheres.txt"));

        final Object result = parser.parse();

        assertTrue(Scene.class.isAssignableFrom(result.getClass()), "parser should've returned a scene");

        final Scene scene = Scene.class.cast(result);

        assertNotNull(scene.getCamera());
        assertEquals(2, scene.getLights().size(), "There should be x2 lights in the scene");
        assertEquals(3, scene.getPrimitives().size(), "There should be x3 primitives in the scene");
    }

/*    @Test(expected = ParserException.class)
    void testParserThrowsExceptionForNullReader() throws Exception {
        Parser parser = new SceneParser(null);
        parser.parse();
        fail("Shouldn't make it this far if exception thrown");
    }*/

    private Reader loadTestResource(final String resource) throws IOException {
        //final InputStream stream = ClassLoader.getSystemResourceAsStream(resource);
        final InputStream stream = this.getClass().getModule().getResourceAsStream(resource);
        //final InputStream stream = this.getClass().getClassLoader().getResourceAsStream(resource);
        return new InputStreamReader(stream);
    }
}
