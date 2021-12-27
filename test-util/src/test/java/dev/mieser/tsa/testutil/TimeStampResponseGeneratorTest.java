package dev.mieser.tsa.testutil;

import dev.mieser.tsa.testutil.TimeStampResponseGenerator.ResponseProperties;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenInfo;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Hashtable;

import static dev.mieser.tsa.domain.FailureInfo.BAD_ALGORITHM;
import static dev.mieser.tsa.domain.HashAlgorithm.SHA256;
import static dev.mieser.tsa.domain.ResponseStatus.*;
import static dev.mieser.tsa.testutil.TestCertificateLoader.loadRsaCertificate;
import static dev.mieser.tsa.testutil.TimeStampResponseGenerator.generateTimeStampResponseMock;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

class TimeStampResponseGeneratorTest {

    @Test
    void throwsExceptionWhenPropertiesIsNull() {
        // given / when / then
        assertThatIllegalArgumentException()
                .isThrownBy(() -> generateTimeStampResponseMock(null))
                .withMessage("Properties cannot be null.");
    }

    @Test
    void generatesExpectedResponseWithoutToken() throws Exception {
        // given
        ResponseProperties rejectedResponseProperties = ResponseProperties.builder()
                .status(REJECTION)
                .statusString("Algorithm not supported")
                .failureInfo(BAD_ALGORITHM)
                .build();

        // when
        TimeStampResponse generatedResponseMock = generateTimeStampResponseMock(rejectedResponseProperties);

        // then
        assertSoftly(softly -> {
            softly.assertThat(generatedResponseMock.getStatus()).isEqualTo(REJECTION.getValue());
            softly.assertThat(generatedResponseMock.getStatusString()).isEqualTo("Algorithm not supported");
            softly.assertThat(generatedResponseMock.getFailInfo().intValue()).isEqualTo(BAD_ALGORITHM.getValue());
            softly.assertThat(generatedResponseMock.getTimeStampToken()).isNull();
        });
    }

    @Test
    void generatesExpectedResponseWithToken() {
        // given
        Date genTime = new Date();
        byte[] sha256Hash = repeat("a", 32).getBytes(UTF_8);
        AttributeTable emptyAttributeTable = new AttributeTable(new Hashtable<>());

        ResponseProperties grantedResponseProperties = ResponseProperties.builder()
                .status(GRANTED)
                .statusString("OK")
                .nonce(BigInteger.TEN)
                .hashAlgorithm(SHA256)
                .hash(sha256Hash)
                .serialNumber(12345)
                .genTime(genTime)
                .signedAttributes(emptyAttributeTable)
                .build();

        // when
        TimeStampResponse generatedResponseMock = generateTimeStampResponseMock(grantedResponseProperties);

        // then
        TimeStampToken timeStampToken = generatedResponseMock.getTimeStampToken();
        TimeStampTokenInfo timeStampInfo = timeStampToken.getTimeStampInfo();

        assertSoftly(softly -> {
            softly.assertThat(generatedResponseMock.getStatus()).isEqualTo(GRANTED.getValue());
            softly.assertThat(generatedResponseMock.getStatusString()).isEqualTo("OK");

            softly.assertThat(timeStampToken.getCertificates().getMatches(null)).isEmpty();
            softly.assertThat(timeStampToken.getSignedAttributes()).isEqualTo(emptyAttributeTable);

            softly.assertThat(timeStampInfo.getMessageImprintAlgOID()).isEqualTo(new ASN1ObjectIdentifier(SHA256.getObjectIdentifier()));
            softly.assertThat(timeStampInfo.getHashAlgorithm()).isEqualTo(new AlgorithmIdentifier(new ASN1ObjectIdentifier(SHA256.getObjectIdentifier())));
            softly.assertThat(timeStampInfo.getMessageImprintDigest()).isEqualTo(sha256Hash);
            softly.assertThat(timeStampInfo.getSerialNumber()).isEqualTo(12345);
            softly.assertThat(timeStampInfo.getGenTime()).isEqualTo(genTime);
            softly.assertThat(timeStampInfo.getNonce()).isEqualTo(BigInteger.TEN);
        });
    }

    @Test
    void generatesExpectedResponseWithTokenContainingCertificate() throws Exception {
        // given
        X509Certificate signingCertificate = loadRsaCertificate();

        ResponseProperties grantedResponseProperties = ResponseProperties.builder()
                .status(GRANTED_WITH_MODS)
                .hashAlgorithm(SHA256)
                .hash(repeat("a", 32).getBytes(UTF_8))
                .serialNumber(12345)
                .genTime(new Date())
                .signingCertificate(signingCertificate)
                .build();

        // when
        TimeStampResponse generatedResponseMock = generateTimeStampResponseMock(grantedResponseProperties);

        // then
        TimeStampToken timeStampToken = generatedResponseMock.getTimeStampToken();

        assertThat(timeStampToken.getCertificates().getMatches(null)).map(X509CertificateHolder::getEncoded).containsExactly(signingCertificate.getEncoded());
    }

}
