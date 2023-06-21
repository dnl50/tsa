package dev.mieser.tsa.signing.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import dev.mieser.tsa.domain.HashAlgorithm;

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

    @ParameterizedTest
    @EnumSource
    void isKnownHashAlgorithmReturnsTrueWhenHashAlgorithmIsKnown(HashAlgorithm hashAlgorithm) {
        // given
        String oid = hashAlgorithm.getObjectIdentifier();

        // when
        boolean known = testSubject.isKnownHashAlgorithm(new ASN1ObjectIdentifier(oid));

        // then
        assertThat(known).isTrue();
    }

}
