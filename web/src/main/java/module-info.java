module tsa.web {
    requires spring.context;
    requires spring.web;
    requires spring.webmvc;
    requires spring.tx;
    requires spring.data.commons;
    requires spring.boot.autoconfigure;
    requires org.slf4j;
    requires java.servlet;
    requires java.validation;
    requires org.apache.commons.codec;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.datatype.jsr310;

    requires tsa.signing;
    requires tsa.integration;
    requires tsa.domain;

    requires static lombok;

    opens dev.mieser.tsa.web.config;
}
