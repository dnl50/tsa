package dev.mieser.tsa.rest.providers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;

/**
 * {@link ErrorResponseMapper} for {@link InvalidTspResponseException}s.
 */
@Provider
public class InvalidTspResponseExceptionMapper extends ErrorResponseMapper<InvalidTspResponseException> {

    @Override
    protected int statusCode() {
        return Response.Status.BAD_REQUEST.getStatusCode();
    }

}
