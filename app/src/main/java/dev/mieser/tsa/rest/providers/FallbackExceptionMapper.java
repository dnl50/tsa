package dev.mieser.tsa.rest.providers;

import static jakarta.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;

import jakarta.annotation.Priority;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import lombok.extern.slf4j.Slf4j;

import dev.mieser.tsa.rest.domain.BasicErrorResponse;

/**
 * {@link ExceptionMapper} for all unhandled exceptions.
 */
@Slf4j
@Provider
@Priority(Integer.MAX_VALUE)
public class FallbackExceptionMapper implements ExceptionMapper<Exception> {

    @Override
    public Response toResponse(Exception e) {
        if (e instanceof WebApplicationException wae) {
            log.debug("WebApplicationException occurred", wae);

            int statusCode = wae.getResponse().getStatus();
            return Response.status(statusCode)
                .entity(new BasicErrorResponse(statusCode))
                .build();
        }

        log.error("Unhandled Exception occurred", e);

        return Response.serverError()
            .entity(new BasicErrorResponse(INTERNAL_SERVER_ERROR.getStatusCode(), e.getMessage()))
            .build();
    }

}
