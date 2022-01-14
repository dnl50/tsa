package dev.mieser.tsa.web.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.*;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatContextCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;

import dev.mieser.tsa.web.config.properties.HttpsRedirectProperties;

@ExtendWith(MockitoExtension.class)
class HttpsRedirectConfigurationTest {

    private final HttpsRedirectProperties httpsRedirectProperties;

    private final ServerProperties serverProperties;

    private final HttpsRedirectConfiguration testSubject;

    HttpsRedirectConfigurationTest() {
        this.httpsRedirectProperties = new HttpsRedirectProperties();
        this.serverProperties = new ServerProperties();

        this.testSubject = new HttpsRedirectConfiguration(httpsRedirectProperties, serverProperties);
    }

    @Test
    void customizeAddsConnectorWithDefaultRedirectPortWhenServerPropertyNotSet(@Mock TomcatServletWebServerFactory factoryMock) {
        // given / when
        testSubject.customize(factoryMock);

        // then
        ArgumentCaptor<Connector> connectorCaptor = ArgumentCaptor.forClass(Connector.class);
        then(factoryMock).should().addAdditionalTomcatConnectors(connectorCaptor.capture());

        assertThat(connectorCaptor.getValue().getRedirectPort()).isEqualTo(8080);
    }

    @Test
    void customizeAddsConnectorWithConfiguredRedirectPort(@Mock TomcatServletWebServerFactory factoryMock) {
        // given
        serverProperties.setPort(420);

        // when
        testSubject.customize(factoryMock);

        // then
        ArgumentCaptor<Connector> connectorCaptor = ArgumentCaptor.forClass(Connector.class);
        then(factoryMock).should().addAdditionalTomcatConnectors(connectorCaptor.capture());

        assertThat(connectorCaptor.getValue().getRedirectPort()).isEqualTo(420);
    }

    @Test
    void customizeAddsConnectorWithExpectedPort(@Mock TomcatServletWebServerFactory factoryMock) {
        // given
        httpsRedirectProperties.setPort(1337);

        // when
        testSubject.customize(factoryMock);

        // then
        ArgumentCaptor<Connector> connectorCaptor = ArgumentCaptor.forClass(Connector.class);
        then(factoryMock).should().addAdditionalTomcatConnectors(connectorCaptor.capture());

        assertThat(connectorCaptor.getValue().getPort()).isEqualTo(1337);
    }

    @Test
    void customizeAddsCustomizerEnforcingHttpsForAllConnections(@Mock TomcatServletWebServerFactory factoryMock,
        @Mock Context contextMock) {
        // given
        willAnswer(invocation -> {
            invocation.getArgument(0, TomcatContextCustomizer.class).customize(contextMock);
            return null;
        }).given(factoryMock).addContextCustomizers(any());

        // when
        testSubject.customize(factoryMock);

        // then
        ArgumentCaptor<SecurityConstraint> securityConstraintCaptor = ArgumentCaptor.forClass(SecurityConstraint.class);
        then(contextMock).should().addConstraint(securityConstraintCaptor.capture());

        assertSoftly(softly -> {
            SecurityConstraint addedSecurityConstraint = securityConstraintCaptor.getValue();

            softly.assertThat(addedSecurityConstraint.getUserConstraint()).isEqualTo("CONFIDENTIAL");
            softly.assertThat(addedSecurityConstraint.findCollections()).hasSize(1);
            softly.assertThat(addedSecurityConstraint.findCollections()[0].findPatterns()).containsExactly("/web/*");
        });
    }

}
