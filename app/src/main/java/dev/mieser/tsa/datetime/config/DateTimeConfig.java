package dev.mieser.tsa.datetime.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import dev.mieser.tsa.datetime.api.CurrentDateService;
import dev.mieser.tsa.datetime.api.DateConverter;
import dev.mieser.tsa.datetime.impl.CurrentDateServiceImpl;
import dev.mieser.tsa.datetime.impl.DateConverterImpl;

public class DateTimeConfig {

    @Produces
    @ApplicationScoped
    DateConverter dateConverter() {
        return new DateConverterImpl();
    }

    @Produces
    @ApplicationScoped
    CurrentDateService currentDateService() {
        return new CurrentDateServiceImpl();
    }

}
