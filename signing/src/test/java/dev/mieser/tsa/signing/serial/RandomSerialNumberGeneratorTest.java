package dev.mieser.tsa.signing.serial;

import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class RandomSerialNumberGeneratorTest {

    private final RandomSerialNumberGenerator testSubject = new RandomSerialNumberGenerator();

    @Test
    void generateSerialNumberDoesNotReturnNull() {
        // given / when
        BigInteger serial = testSubject.generateSerialNumber();

        // then
        assertThat(serial).isNotNull();
    }

}
