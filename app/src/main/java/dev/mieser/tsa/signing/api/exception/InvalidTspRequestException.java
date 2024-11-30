package dev.mieser.tsa.signing.api.exception;

public class InvalidTspRequestException extends Exception {

    public InvalidTspRequestException(Throwable cause) {
        super(cause);
    }

    public InvalidTspRequestException(String message) {
        super(message);
    }

}
