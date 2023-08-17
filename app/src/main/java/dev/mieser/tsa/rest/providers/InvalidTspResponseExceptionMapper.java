package dev.mieser.tsa.rest.providers;

import jakarta.ws.rs.ext.Provider;

import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;

/**
 * {@link BadRequestResponseMapper} for {@link InvalidTspResponseException}s.
 */
@Provider
public class InvalidTspResponseExceptionMapper extends BadRequestResponseMapper<InvalidTspResponseException> {

}
