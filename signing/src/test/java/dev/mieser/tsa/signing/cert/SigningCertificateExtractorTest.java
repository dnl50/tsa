package dev.mieser.tsa.signing.cert;

import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.CollectionStore;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Hashtable;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class SigningCertificateExtractorTest {

    private final SigningCertificateExtractor testSubject = new SigningCertificateExtractor();

    @Test
    void extractSigningCertificateReturnsEmptyOptionalWhenNoTimeStampTokenIsIncluded(@Mock TimeStampResponse timeStampResponseMock) {
        // given / when
        Optional<X509CertificateHolder> signingCertificate = testSubject.extractSigningCertificate(timeStampResponseMock);

        // then
        assertThat(signingCertificate).isEmpty();
    }

    @Test
    void extractSigningCertificateReturnsEmptyOptionalWhenNoCertificatesAreIncluded(@Mock TimeStampResponse timeStampResponseMock,
            @Mock TimeStampToken timeStampTokenMock) {
        // given
        given(timeStampResponseMock.getTimeStampToken()).willReturn(timeStampTokenMock);
        given(timeStampTokenMock.getCertificates()).willReturn(new CollectionStore<>(emptyList()));

        // when
        Optional<X509CertificateHolder> signingCertificate = testSubject.extractSigningCertificate(timeStampResponseMock);

        // then
        assertThat(signingCertificate).isEmpty();
    }

    @Test
    void extractSigningInformationThrowsExceptionWhenCertificatesAreIncludedButNoSignedCertAttributeIsPresent(@Mock TimeStampResponse timeStampResponseMock,
            @Mock TimeStampToken timeStampTokenMock) {
        // given
        given(timeStampResponseMock.getTimeStampToken()).willReturn(timeStampTokenMock);
        given(timeStampTokenMock.getCertificates()).willReturn(new CollectionStore<>(List.of(mock(X509CertificateHolder.class))));
        given(timeStampTokenMock.getSignedAttributes()).willReturn(new AttributeTable(new Hashtable<>()));

        // when / then
        assertThatExceptionOfType(InvalidTspResponseException.class)
                .isThrownBy(() -> testSubject.extractSigningCertificate(timeStampResponseMock))
                .withMessage("The timestamp token neither contains a signed 'SigningCertificate' nor a signed 'SigningCertificateV2' attribute.");
    }

    @Nested
    class SigningCertificateTest {

        // TODO

    }

    @Nested
    class SigningCertificateV2Test {

        // TODO

    }

}
