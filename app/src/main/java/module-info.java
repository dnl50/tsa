module tsa.app {
    requires spring.beans;
    requires spring.context;
    requires spring.boot;
    requires spring.boot.autoconfigure;

    requires tsa.signing;

    requires static lombok;
}
