package dev.mieser.tsa.rest.providers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import dev.mieser.tsa.signing.api.exception.InvalidTspRequestException;

/**
 * {@link ErrorResponseMapper} for {@link InvalidTspRequestException}s.
 */
@Provider
public class InvalidTspRequestExceptionMapper extends ErrorResponseMapper<InvalidTspRequestException> {

    @Override
    protected int statusCode() {
        return Response.Status.BAD_REQUEST.getStatusCode();
    }

}
