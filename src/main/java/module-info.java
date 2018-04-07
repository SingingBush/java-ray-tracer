module ex02 {
    requires ex02.blas;
    requires ex02.entities;

    requires log4j.api;
    requires log4j.core;

    requires swt;

    exports ex02;
    exports ex02.parser to junit;
}