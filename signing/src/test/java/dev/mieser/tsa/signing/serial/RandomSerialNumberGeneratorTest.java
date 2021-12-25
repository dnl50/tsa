package dev.mieser.tsa.signing.serial;

import org.junit.jupiter.api.Test;

import java.security.SecureRandom;

import static org.assertj.core.api.Assertions.assertThat;

class RandomSerialNumberGeneratorTest {

    private final RandomSerialNumberGenerator testSubject;

    RandomSerialNumberGeneratorTest() {
        this.testSubject = new RandomSerialNumberGenerator(() -> 10L);
    }

    @Test
    void generateSerialNumberReturnsExpectedSerial() {
        // given / when
        long serial = testSubject.generateSerialNumber();

        // then
        assertThat(serial).isEqualTo(10L);
    }

}
