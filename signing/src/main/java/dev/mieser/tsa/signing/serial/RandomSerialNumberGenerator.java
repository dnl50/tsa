package dev.mieser.tsa.signing.serial;

import java.math.BigInteger;
import java.security.SecureRandom;

/**
 * {@link SerialNumberGenerator} generating random serial numbers.
 */
public class RandomSerialNumberGenerator implements SerialNumberGenerator {

    private final SecureRandom secureRandom = new SecureRandom();

    @Override
    public BigInteger generateSerialNumber() {
        return BigInteger.valueOf(secureRandom.nextLong());
    }

}
