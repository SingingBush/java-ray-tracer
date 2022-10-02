module ex02.app {

    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    //requires javafx.fxml;
    requires javafx.swing;

    requires swt;

    requires org.jetbrains.annotations;
    requires org.slf4j;

    requires ex02.entities;
    requires ex02.raytracer;

    // If FXML is added then open packages to it
    //opens ex02.components to javafx.fxml;
    //opens ex02 to javafx.fxml;

    exports ex02;
}
