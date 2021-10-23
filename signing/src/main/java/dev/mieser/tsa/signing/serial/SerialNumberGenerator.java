package dev.mieser.tsa.signing.serial;

import java.math.BigInteger;

/**
 * Generates unique serial numbers for TSP responses.
 */
@FunctionalInterface
public interface SerialNumberGenerator {

    /**
     * @return A unique serial number.
     * @implSpec The returned serial number cannot be {@code null}.
     */
    BigInteger generateSerialNumber();

}
