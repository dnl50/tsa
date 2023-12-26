package dev.mieser.tsa.rest.providers;

import static jakarta.ws.rs.core.Response.Status.BAD_REQUEST;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;

import dev.mieser.tsa.rest.domain.ErrorResponse;

/**
 * JAX-RS {@link ExceptionMapper} which translates exceptions to {@link ErrorResponse}s. The HTTP Status Code is set to
 * {@code 400 Bad Request}.
 *
 * @param <T>
 *     The type of the thrown exception.
 */
abstract class BadRequestResponseMapper<T extends Exception> implements ExceptionMapper<T> {

    @Override
    public Response toResponse(T exception) {
        return Response.status(BAD_REQUEST)
            .entity(new ErrorResponse(getMessage(exception)))
            .build();
    }

    /**
     * 
     * @return The message of the {@link ErrorResponse}.
     */
    protected String getMessage(T exception) {
        return exception.getMessage();
    }

}
