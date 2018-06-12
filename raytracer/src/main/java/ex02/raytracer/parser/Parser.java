package ex02.raytracer.parser;


/**
 * @author Samael Bate (singingbush)
 * created on 18/08/17
 */
public interface Parser<T> {

    T parse() throws ParserException;
}
