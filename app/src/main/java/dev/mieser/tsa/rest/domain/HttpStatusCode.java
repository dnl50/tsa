package dev.mieser.tsa.rest.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * OpenAPI uses strings to document HTTP status codes.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpStatusCode {

    public static final String OK = "200";

    public static final String NOT_FOUND = "404";

    public static final String BAD_REQUEST = "400";

}
