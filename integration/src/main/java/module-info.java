module tsa.integration {
    requires spring.context;
    requires spring.beans;

    requires tsa.domain;
    requires tsa.signing;
    requires tsa.persistence;

    requires static lombok;

    exports dev.mieser.tsa.integration.api;
}
