package ex02.raytracer.parser;

/**
 * @author Samael Bate (singingbush)
 * created on 18/08/17
 */
public class ParserException extends Exception {

    public ParserException(final String message) {
        super(message);
    }

    public ParserException(String message, Throwable cause) {
        super(message, cause);
    }
}
