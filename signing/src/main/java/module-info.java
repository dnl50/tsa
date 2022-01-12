module tsa.signing {
    requires java.validation;

    requires spring.boot;

    requires spring.context;

    requires spring.boot.autoconfigure;

    requires org.bouncycastle.pkix;

    requires org.bouncycastle.util;

    requires org.bouncycastle.provider;

    requires org.apache.commons.io;

    requires org.apache.commons.lang3;

    requires org.slf4j;

    requires tsa.currenttime;

    requires tsa.domain;

    requires static lombok;

    exports dev.mieser.tsa.signing.api;

    exports dev.mieser.tsa.signing.api.exception;

    exports dev.mieser.tsa.signing.config;

    opens dev.mieser.tsa.signing.config;
}
