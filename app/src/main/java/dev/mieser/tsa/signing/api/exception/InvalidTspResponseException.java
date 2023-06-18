package dev.mieser.tsa.signing.api.exception;

public class InvalidTspResponseException extends Exception {

    public InvalidTspResponseException(String message) {
        super(message);
    }

    public InvalidTspResponseException(String message, Throwable cause) {
        super(message, cause);
    }

}
