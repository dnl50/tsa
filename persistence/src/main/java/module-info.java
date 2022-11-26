module tsa.persistence {
    requires jakarta.persistence;

    requires jakarta.validation;

    requires java.compiler;

    requires spring.data.jpa;

    requires spring.data.commons;

    requires org.mapstruct;

    requires org.apache.commons.codec;

    requires tsa.domain;

    requires tsa.signing;

    requires static lombok;

    exports dev.mieser.tsa.persistence.api;

    exports dev.mieser.tsa.persistence.config;

    opens dev.mieser.tsa.persistence.config;
}
