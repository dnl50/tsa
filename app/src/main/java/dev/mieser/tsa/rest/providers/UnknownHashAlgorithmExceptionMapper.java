package dev.mieser.tsa.rest.providers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import dev.mieser.tsa.signing.api.exception.UnknownHashAlgorithmException;

/**
 * {@link ErrorResponseMapper} for {@link UnknownHashAlgorithmException}s.
 */
@Provider
public class UnknownHashAlgorithmExceptionMapper extends ErrorResponseMapper<UnknownHashAlgorithmException> {

    @Override
    protected int statusCode() {
        return Response.Status.BAD_REQUEST.getStatusCode();
    }

}
