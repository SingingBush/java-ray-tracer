package ex02;

import ex02.raytracer.RayTracer;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import java.io.File;

/**
 * Utility class allowing to batch render an entire directory
 * todo: this class has problems, needs a rethink
 */
public class BatchRender {

    private static final Logger LOG = LoggerFactory.getLogger(BatchRender.class);

    static RayTracer tracer;

    public static void renderDirectory(File inputDir) throws Exception {
        final File[] files = inputDir.listFiles((dir, name) -> name.indexOf(".txt") > -1);

        for (final File file : files) {
            LOG.info("Rendering: " + file.getAbsolutePath());
            Process proc = Runtime.getRuntime().exec("javaw.exe -classpath C:\\work\\ex02\\bin;C:\\work\\ex02\\lib\\swt.jar ex02.raytracer.RayTracer " + file.getAbsolutePath());
            proc.waitFor();
        }
    }

    public static void main(String[] args) throws Exception {
        final File inputDir = new File(args[0]);

        renderDirectory(inputDir);
    }

}
