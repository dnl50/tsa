package dev.mieser.tsa.rest.providers;

import jakarta.ws.rs.ext.Provider;

import dev.mieser.tsa.signing.api.exception.InvalidCertificateException;

/**
 * {@link BadRequestResponseMapper} for {@link InvalidCertificateException}s.
 */
@Provider
public class InvalidCertificateExceptionMapper extends BadRequestResponseMapper<InvalidCertificateException> {

}
