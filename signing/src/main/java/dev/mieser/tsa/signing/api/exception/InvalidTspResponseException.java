package dev.mieser.tsa.signing.api.exception;

public class InvalidTspResponseException extends RuntimeException {

    public InvalidTspResponseException(String message, Throwable cause) {
        super(message, cause);
    }

}
