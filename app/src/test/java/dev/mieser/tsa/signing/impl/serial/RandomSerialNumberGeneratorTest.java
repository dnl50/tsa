package dev.mieser.tsa.signing.impl.serial;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

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
