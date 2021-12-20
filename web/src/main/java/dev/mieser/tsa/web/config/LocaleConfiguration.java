package dev.mieser.tsa.web.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import static java.util.Locale.ENGLISH;

@Configuration
class LocaleConfiguration {

    @Bean
    LocaleResolver localeResolver() {
        var localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setDefaultLocale(ENGLISH);

        return localeResolver;
    }

}
