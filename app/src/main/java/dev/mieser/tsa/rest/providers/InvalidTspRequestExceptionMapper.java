package dev.mieser.tsa.rest.providers;

import jakarta.ws.rs.ext.Provider;

import dev.mieser.tsa.signing.api.exception.InvalidTspRequestException;

/**
 * {@link BadRequestResponseMapper} for {@link InvalidTspRequestException}s.
 */
@Provider
public class InvalidTspRequestExceptionMapper extends BadRequestResponseMapper<InvalidTspRequestException> {

}
