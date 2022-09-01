module ex02.raytracer {

    requires java.desktop;

    requires org.slf4j;
    requires org.jetbrains.annotations;

    requires ex02.blas;
    requires ex02.entities;

    exports ex02.raytracer;
    exports ex02.raytracer.parser;
}
