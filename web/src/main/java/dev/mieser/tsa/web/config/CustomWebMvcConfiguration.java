package dev.mieser.tsa.web.config;

import static java.util.Locale.ENGLISH;
import static java.util.Locale.GERMAN;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import dev.mieser.tsa.web.formatter.Base64Formatter;
import dev.mieser.tsa.web.formatter.HexFormatter;

@Configuration
class CustomWebMvcConfiguration implements WebMvcConfigurer {

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addFormatter(new Base64Formatter());
        registry.addFormatter(new HexFormatter());
    }

    @Bean
    LocaleResolver localeResolver() {
        var localeResolver = new AcceptHeaderLocaleResolver();
        localeResolver.setSupportedLocales(List.of(ENGLISH, GERMAN));
        localeResolver.setDefaultLocale(ENGLISH);

        return localeResolver;
    }

}
