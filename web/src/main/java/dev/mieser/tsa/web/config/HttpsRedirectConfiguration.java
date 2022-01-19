package dev.mieser.tsa.web.config;

import lombok.RequiredArgsConstructor;

import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Configuration;

import dev.mieser.tsa.web.config.properties.HttpsRedirectProperties;

/**
 * {@link WebServerFactoryCustomizer} to add a Tomcat {@link Connector} which redirects all traffic to the default
 * Connector configured by Spring's {@code server.*} properties.
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(HttpsRedirectProperties.class)
@ConditionalOnProperty(name = "server.ssl.key-store")
class HttpsRedirectConfiguration implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    /**
     * The default Spring server port when the {@code server.port} property is not set explicitly.
     */
    private static final int DEFAULT_SERVER_PORT = 8080;

    private final HttpsRedirectProperties httpsRedirectProperties;

    private final ServerProperties serverProperties;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        factory.addAdditionalTomcatConnectors(redirectConnector());
        factory.addContextCustomizers(confidentialSecurityConstraintContextCustomizer());
    }

    /**
     * @return A {@link TomcatContextCustomizer} adding a {@link SecurityConstraint} which enforces {@code HTTPS} for all
     * frontend requests.
     */
    private TomcatContextCustomizer confidentialSecurityConstraintContextCustomizer() {
        return context -> {
            var securityCollection = new SecurityCollection();
            securityCollection.addPattern("/web/*");

            var securityConstraint = new SecurityConstraint();
            securityConstraint.setUserConstraint("CONFIDENTIAL");
            securityConstraint.addCollection(securityCollection);

            context.addConstraint(securityConstraint);
        };
    }

    /**
     * @return A Tomcat {@link Connector} redirecting all requests to the default Spring default Connector configured by
     * Spring's {@code server.*} properties.
     */
    private Connector redirectConnector() {
        var connector = new Connector();
        connector.setPort(httpsRedirectProperties.getPort());
        connector.setRedirectPort(serverPort());

        return connector;
    }

    /**
     * @return The configured default Tomcat Connector port.
     * @implNote {@link ServerProperties#getPort()} returns {@code null} when the {@code server.port} property is not set
     * explicitly.
     */
    private int serverPort() {
        return serverProperties.getPort() != null ? serverProperties.getPort() : DEFAULT_SERVER_PORT;
    }

}
