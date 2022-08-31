module ex02.entities {

    requires java.desktop;

    requires commons.math3;

    requires ex02.blas;

    exports ex02.entities;
    exports ex02.entities.lights;
    exports ex02.entities.primitives;
}
