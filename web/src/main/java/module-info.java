module tsa.web {
    requires spring.context;
    requires spring.web;
    requires spring.webmvc;
    requires spring.tx;
    requires spring.data.commons;
    requires org.slf4j;
    requires java.servlet;
    requires spring.boot.autoconfigure;

    requires tsa.signing;
    requires tsa.integration;
    requires tsa.domain;

    requires static lombok;

    opens dev.mieser.tsa.web.config;
}
