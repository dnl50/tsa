module tsa.web {
    requires spring.web;
    requires spring.tx;
    requires spring.data.commons;

    requires tsa.signing;
    requires tsa.integration;
    requires tsa.domain;

    requires static lombok;
}
