package ex02.parser;

import ex02.entities.Scene;
import org.junit.Test;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * @author Samael Bate (singingbush)
 * created on 18/08/17
 */
public class ParserTest {

    @Test
    public void testParseSpheresSceneFile() throws Exception {
        Parser parser = new SceneParser(loadTestResource("scenes/spheres.txt"));

        final Object result = parser.parse();

        assertTrue("parser should've returned a scene", Scene.class.isAssignableFrom(result.getClass()));

        final Scene scene = Scene.class.cast(result);

        assertNotNull(scene.getCamera());
        assertEquals("There should be x2 lights in the scene", 2, scene.getLights().size());
        assertEquals("There should be x3 primitives in the scene", 3, scene.getPrimitives().size());
    }

    @Test(expected = ParserException.class)
    public void testParserThrowsExceptionForNullReader() throws Exception {
        Parser parser = new SceneParser(null);
        parser.parse();
        fail("Shouldn't make it this far if exception thrown");
    }

    private Reader loadTestResource(final String resource) throws IOException {
        final InputStream stream = ClassLoader.getSystemResourceAsStream(resource);

        return stream != null ?
                new BufferedReader(new InputStreamReader(stream, Charset.forName("UTF-8"))) :
                Files.newBufferedReader(Paths.get("src/test/resources/" + resource));
    }
}