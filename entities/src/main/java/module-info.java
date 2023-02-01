module ex02.entities {

    requires java.desktop;

    requires org.apache.commons.math4.legacy;

    requires ex02.blas;

    exports ex02.entities;
    exports ex02.entities.lights;
    exports ex02.entities.primitives;
}
