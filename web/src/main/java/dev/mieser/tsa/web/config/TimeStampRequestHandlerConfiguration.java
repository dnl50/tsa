package dev.mieser.tsa.web.config;

import javax.servlet.Filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import dev.mieser.tsa.web.filter.TimeStampProtocolFilter;

/**
 * {@link WebServerFactoryCustomizer} to add a Tomcat Connector especially for handling Time Stamp Protocol requests. A
 * {@link TimeStampProtocolFilter} is bound to the same port to prevent the connector from responding to other requests.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(TimeStampRequestHandlerProperties.class)
public class TimeStampRequestHandlerConfiguration implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    private final TimeStampRequestHandlerProperties timeStampRequestHandlerProperties;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        var connector = new Connector();
        connector.setPort(timeStampRequestHandlerProperties.getPort());

        factory.addAdditionalTomcatConnectors(connector);
    }

    @Bean
    Filter timeStampProtocolFilter() {
        return new TimeStampProtocolFilter(timeStampRequestHandlerProperties.getPort());
    }

}
