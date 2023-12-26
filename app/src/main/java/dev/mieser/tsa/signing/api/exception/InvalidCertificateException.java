package dev.mieser.tsa.signing.api.exception;

/**
 * Indicates that the certificate can either not be parsed or uses an unsupported public key algorithm.
 */
public class InvalidCertificateException extends Exception {

    public InvalidCertificateException(String message) {
        super(message);
    }

    public InvalidCertificateException(Throwable cause) {
        super(cause);
    }

}
