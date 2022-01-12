package dev.mieser.tsa.signing;

import static dev.mieser.tsa.domain.HashAlgorithm.SHA512;
import static org.assertj.core.api.Assertions.assertThat;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.junit.jupiter.api.Test;

class TspValidatorTest {

    private final TspValidator testSubject = new TspValidator();

    @Test
    void isKnownHashAlgorithmReturnsFalseWhenHashAlgorithmNotKnown() {
        // given
        String oid = "1.2.3.4.5";

        // when
        boolean known = testSubject.isKnownHashAlgorithm(new ASN1ObjectIdentifier(oid));

        // then
        assertThat(known).isFalse();
    }

    @Test
    void isKnownHashAlgorithmReturnsTrueWhenHashAlgorithmIsKnown() {
        // given
        String oid = SHA512.getObjectIdentifier();

        // when
        boolean known = testSubject.isKnownHashAlgorithm(new ASN1ObjectIdentifier(oid));

        // then
        assertThat(known).isTrue();
    }

}
