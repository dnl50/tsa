package dev.mieser.tsa.rest.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * MIME Types as defined in <a href="https://datatracker.ietf.org/doc/html/rfc3161">RFC 3161</a>.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TsaMediaType {

    public static final String TIMESTAMP_QUERY = "application/timestamp-query";

    public static final String TIMESTAMP_REPLY = "application/timestamp-reply";

}
