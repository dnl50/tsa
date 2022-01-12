package dev.mieser.tsa.signing.api.exception;

public class InvalidTspRequestException extends RuntimeException {

    public InvalidTspRequestException(String message, Throwable cause) {
        super(message, cause);
    }

}
