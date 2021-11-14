package dev.mieser.tsa.signing;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.tsp.TimeStampRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.mieser.tsa.domain.HashAlgorithm.SHA512;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TspRequestValidatorTest {

    private final TspRequestValidator testSubject = new TspRequestValidator();

    @Test
    void isKnownHashAlgorithmReturnsFalseWhenHashAlgorithmNotKnown(@Mock TimeStampRequest timeStampRequestMock) {
        // given
        String oid = "1.2.3.4.5";

        given(timeStampRequestMock.getMessageImprintAlgOID()).willReturn(new ASN1ObjectIdentifier(oid));

        // when
        boolean known = testSubject.isKnownHashAlgorithm(timeStampRequestMock);

        // then
        assertThat(known).isFalse();
    }

    @Test
    void isKnownHashAlgorithmReturnsTrueWhenHashAlgorithmIsKnown(@Mock TimeStampRequest timeStampRequestMock) {
        // given
        String oid = SHA512.getObjectIdentifier();

        given(timeStampRequestMock.getMessageImprintAlgOID()).willReturn(new ASN1ObjectIdentifier(oid));

        // when
        boolean known = testSubject.isKnownHashAlgorithm(timeStampRequestMock);

        // then
        assertThat(known).isTrue();
    }

}
