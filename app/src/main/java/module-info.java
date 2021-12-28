module tsa.app {
    requires spring.core;

    requires spring.boot;

    requires spring.beans;

    requires spring.context;

    requires spring.boot.autoconfigure;

    requires tsa.signing;

    requires static lombok;

    opens dev.mieser.tsa.app;
}
