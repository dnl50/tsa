package dev.mieser.tsa.signing.api.exception;

public class TsaInitializationException extends RuntimeException {

    public TsaInitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TsaInitializationException(String message) {
        super(message);
    }

}
