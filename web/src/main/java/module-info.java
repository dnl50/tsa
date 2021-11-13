module tsa.web {
    requires spring.web;
    requires spring.tx;

    requires tsa.integration;
    requires tsa.domain;

    requires static lombok;
}
