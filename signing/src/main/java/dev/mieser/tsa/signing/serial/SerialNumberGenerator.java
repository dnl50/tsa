package dev.mieser.tsa.signing.serial;

/**
 * Generates unique serial numbers for TSP responses.
 */
@FunctionalInterface
public interface SerialNumberGenerator {

    /**
     * @return A unique serial number.
     */
    long generateSerialNumber();

}
