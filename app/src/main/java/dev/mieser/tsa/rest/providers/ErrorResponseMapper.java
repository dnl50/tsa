package dev.mieser.tsa.rest.providers;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * JAX-RS {@link ExceptionMapper} which translates exceptions to {@link ErrorResponse}s.
 *
 * @param <T>
 *     The type of the thrown exception.
 */
abstract class ErrorResponseMapper<T extends Exception> implements ExceptionMapper<T> {

    @Override
    public Response toResponse(T exception) {
        return Response.status(statusCode())
            .entity(new ErrorResponse(exception.getMessage()))
            .build();
    }

    /**
     * @return The status code of the resulting
     */
    protected abstract int statusCode();

}
