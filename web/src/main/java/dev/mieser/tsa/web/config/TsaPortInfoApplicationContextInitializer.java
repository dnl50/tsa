package dev.mieser.tsa.web.config;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import lombok.extern.slf4j.Slf4j;

import org.apache.catalina.connector.Connector;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * {@link ApplicationListener} listening for Spring Boot's {@link WebServerInitializedEvent} to the store the local port
 * of the Tomcat connector for TSP requests in the application context.
 *
 * @see org.springframework.boot.web.context.ServerPortInfoApplicationContextInitializer
 */
@Slf4j
public class TsaPortInfoApplicationContextInitializer
    implements ApplicationContextInitializer<ConfigurableApplicationContext>, ApplicationListener<WebServerInitializedEvent> {

    /**
     * The name of the property the TSP port is stored in. The name is based on Spring Boot's {@code local.server.port}
     * property.
     */
    private static final String ENVIRONMENT_PROPERTY_NAME = "local.tsa.port";

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        applicationContext.addApplicationListener(this);
    }

    @Override
    public void onApplicationEvent(WebServerInitializedEvent event) {
        if (event.getWebServer()instanceof TomcatWebServer tomcat) {
            ApplicationContext applicationContext = event.getApplicationContext();

            Stream.of(tomcat.getTomcat().getService().findConnectors())
                .filter(
                    connector -> connector.getRedirectPort() == TimeStampRequestHandlerConfiguration.TSA_CONNECTOR_MARKER_PORT)
                .reduce((first, second) -> {
                    throw new IllegalStateException("Multiple matching connectors found!");
                })
                .map(Connector::getLocalPort)
                .ifPresent(tsaPort -> {
                    log.info("Tomcat connector for TSP requests is running on port {}.", tsaPort);
                    setPortProperty(applicationContext, tsaPort);
                });
        }
    }

    /**
     * @implNote Copied from Spring Boot's
     * {@link org.springframework.boot.web.context.ServerPortInfoApplicationContextInitializer}.
     */
    private void setPortProperty(ApplicationContext context, int port) {
        if (context instanceof ConfigurableApplicationContext configurableContext) {
            setPortProperty(configurableContext.getEnvironment(), port);
        }
        if (context.getParent() != null) {
            setPortProperty(context.getParent(), port);
        }
    }

    /**
     * @implNote Copied from Spring Boot's
     * {@link org.springframework.boot.web.context.ServerPortInfoApplicationContextInitializer}.
     */
    @SuppressWarnings("unchecked")
    private void setPortProperty(ConfigurableEnvironment environment, int port) {
        MutablePropertySources sources = environment.getPropertySources();
        PropertySource<?> source = sources.get("server.ports");
        if (source == null) {
            source = new MapPropertySource("server.ports", new HashMap<>());
            sources.addFirst(source);
        }
        ((Map<String, Object>) source.getSource()).put(ENVIRONMENT_PROPERTY_NAME, port);
    }

}
