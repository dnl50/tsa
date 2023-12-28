package dev.mieser.tsa.signing.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DigestAlgorithmConverterTest {

    private final DigestAlgorithmConverter testSubject = new DigestAlgorithmConverter();

    @Test
    void returnsNullWhenValueCannotBeConverted() {
        // given / when
        var result = testSubject.convert("I'm neither an OID nor a name!");

        // then
        assertThat(result).isNull();
    }

}
