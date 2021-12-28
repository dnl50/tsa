package dev.mieser.tsa.datetime.config;

import java.time.Clock;
import java.time.ZoneId;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.mieser.tsa.datetime.CurrentDateTimeServiceImpl;
import dev.mieser.tsa.datetime.DateConverterImpl;
import dev.mieser.tsa.datetime.api.CurrentDateTimeService;
import dev.mieser.tsa.datetime.api.DateConverter;

@Configuration
public class DateTimeConfiguration {

    @Bean
    CurrentDateTimeService currentDateTimeService() {
        return new CurrentDateTimeServiceImpl(Clock.systemDefaultZone());
    }

    @Bean
    DateConverter dateConverter() {
        return new DateConverterImpl(ZoneId.systemDefault());
    }

}
