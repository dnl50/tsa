module tsa.currenttime {
    requires static lombok;

    requires spring.context;

    exports dev.mieser.tsa.datetime.api;

    exports dev.mieser.tsa.datetime.config;

    opens dev.mieser.tsa.datetime.config;
}
