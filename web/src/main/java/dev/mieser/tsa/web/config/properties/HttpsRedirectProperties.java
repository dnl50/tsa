package dev.mieser.tsa.web.config.properties;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "tsa.http")
public class HttpsRedirectProperties {

    /**
     * The TCP port of the Tomcat Connector which redirects all incoming requests to the default connector configured by
     * Spring's {@code server.*} Properties. Defaults to port {@code 80}.
     * <p/>
     * Must be a value between {@code 1} and {@code 65535} (inclusive) which differs from the default Tomcat Connector port
     * used for other requests.
     */
    @Min(1L)
    @Max(65535L)
    private int port = 80;

}
