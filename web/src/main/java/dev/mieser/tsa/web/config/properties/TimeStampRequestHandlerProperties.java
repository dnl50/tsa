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
@ConfigurationProperties(prefix = "tsa.server")
public class TimeStampRequestHandlerProperties {

    /**
     * The TCP port of the Tomcat Connector which handles Time Stamp Query HTTP requests. Defaults to port {@code 318}.
     * <p/>
     * Must be a value between {@code 1} and {@code 65535} (inclusive) which differs from the default Tomcat Connector port
     * used for other requests.
     */
    @Min(1L)
    @Max(65535L)
    private int port = 318;

}
