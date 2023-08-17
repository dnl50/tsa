package dev.mieser.tsa.signing.api;

import java.io.InputStream;

import dev.mieser.tsa.domain.TimeStampValidationResult;
import dev.mieser.tsa.signing.api.exception.InvalidCertificateException;
import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;
import dev.mieser.tsa.signing.api.exception.TsaInitializationException;

/**
 * Interface abstraction of a service verifying that <a href="https://www.ietf.org/rfc/rfc3161.txt">RFC 3161</a>
 * Time-Stamp Protocol responses were signed by this {@link TimeStampAuthority} implementation.
 */
public interface TimeStampValidator {

    /**
     * Initializes the validator.
     *
     * @throws TsaInitializationException
     *     When an error occurs while initializing the validator.
     */
    void initialize();

    /**
     * Verifies whether the TSP response was signed using the private key of the currently configured certificate.
     *
     * @param tspResponse
     *     The input stream of an ASN.1 DER encoded TSP request, not {@code null}.
     * @return The verification result of the TSP response.
     * @throws InvalidTspResponseException
     *     When the specified input stream does not contain a valid ASN.1 DER encoded TSP response.
     */
    TimeStampValidationResult validateResponse(InputStream tspResponse) throws InvalidTspResponseException;

    /**
     * Verifies whether the TSP response was signed using the private key of the currently configured certificate.
     *
     * @param tspResponse
     *     The input stream of an ASN.1 DER encoded TSP request, not {@code null}.
     * @param x509Certificate
     *     An X.509 encoded certificate which the signature will be validated against, not {@code null}.
     * @return The verification result of the TSP response.
     * @throws InvalidTspResponseException
     *     When the specified input stream does not contain a valid ASN.1 DER encoded TSP response.
     * @implNote The specified input streams are not getting closed inside the method.
     */
    TimeStampValidationResult validateResponse(InputStream tspResponse,
        InputStream x509Certificate) throws InvalidTspResponseException, InvalidCertificateException;

}
