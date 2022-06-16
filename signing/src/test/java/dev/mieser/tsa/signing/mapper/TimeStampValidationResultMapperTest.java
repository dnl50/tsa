package dev.mieser.tsa.signing.mapper;

import static dev.mieser.tsa.domain.FailureInfo.BAD_ALGORITHM;
import static dev.mieser.tsa.domain.HashAlgorithm.SHA512;
import static dev.mieser.tsa.domain.ResponseStatus.*;
import static dev.mieser.tsa.testutil.TestCertificateLoader.loadEcCertificate;
import static dev.mieser.tsa.testutil.TimeStampResponseGenerator.generateTimeStampResponseMock;
import static java.math.BigInteger.TWO;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bouncycastle.asn1.x509.X509ObjectIdentifiers.id_SHA1;
import static org.mockito.BDDMockito.given;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.tsp.TimeStampResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.mieser.tsa.datetime.api.DateConverter;
import dev.mieser.tsa.domain.SigningCertificateIdentifier;
import dev.mieser.tsa.domain.SigningCertificateInformation;
import dev.mieser.tsa.domain.TimeStampValidationResult;
import dev.mieser.tsa.signing.cert.SigningCertificateHolder;
import dev.mieser.tsa.testutil.TimeStampResponseGenerator.ResponseProperties;

@ExtendWith(MockitoExtension.class)
class TimeStampValidationResultMapperTest {

    private final DateConverter dateConverterMock;

    private final TimeStampValidationResultMapper testSubject;

    TimeStampValidationResultMapperTest(@Mock DateConverter dateConverterMock) {
        this.dateConverterMock = dateConverterMock;
        this.testSubject = new TimeStampValidationResultMapper(dateConverterMock);
    }

    @Test
    void mapsRejectedResponseWithStatusStringAndFailInfoToExpectedDomainObject() {
        // given
        ResponseProperties rejectedResponseProperties = ResponseProperties.builder()
            .status(REJECTION)
            .statusString("Unsupported Algorithm!!!!")
            .failureInfo(BAD_ALGORITHM)
            .build();

        TimeStampResponse timeStampResponse = generateTimeStampResponseMock(rejectedResponseProperties);

        // when
        TimeStampValidationResult mappedValidationResult = testSubject.map(timeStampResponse, null, false);

        // then
        TimeStampValidationResult expectedValidationResult = TimeStampValidationResult.builder()
            .status(REJECTION)
            .statusString("Unsupported Algorithm!!!!")
            .failureInfo(BAD_ALGORITHM)
            .signedByThisTsa(false)
            .build();

        assertThat(mappedValidationResult).isEqualTo(expectedValidationResult);
    }

    @Test
    void mapsRejectedResponseWithoutStatusStringAndFailInfoToExpectedDomainObject() {
        // given
        ResponseProperties rejectedResponseProperties = ResponseProperties.builder()
            .status(REJECTION)
            .build();

        TimeStampResponse timeStampResponse = generateTimeStampResponseMock(rejectedResponseProperties);

        // when
        TimeStampValidationResult mappedValidationResult = testSubject.map(timeStampResponse, null, false);

        // then
        TimeStampValidationResult expectedValidationResult = TimeStampValidationResult.builder()
            .status(REJECTION)
            .signedByThisTsa(false)
            .build();

        assertThat(mappedValidationResult).isEqualTo(expectedValidationResult);
    }

    @Test
    void mapsTimeStampTokenInformation() {
        // given
        ZonedDateTime genTime = ZonedDateTime.parse("2021-12-27T16:41:43+01:00");
        Date genTimeAsDate = Date.from(genTime.toInstant());
        byte[] sha512Hash = repeat("a", 64).getBytes(UTF_8);

        ResponseProperties grantedResponseProperties = ResponseProperties.builder()
            .status(GRANTED_WITH_MODS)
            .hashAlgorithm(SHA512)
            .hash(sha512Hash)
            .genTime(genTimeAsDate)
            .serialNumber(1337L)
            .nonce(TWO)
            .build();
        TimeStampResponse timeStampResponse = generateTimeStampResponseMock(grantedResponseProperties);

        given(dateConverterMock.toZonedDateTime(genTimeAsDate)).willReturn(genTime);

        // when
        TimeStampValidationResult mappedValidationResult = testSubject.map(timeStampResponse, null, true);

        // then
        TimeStampValidationResult expectedValidationResult = TimeStampValidationResult.builder()
            .status(GRANTED_WITH_MODS)
            .generationTime(genTime)
            .hash(sha512Hash)
            .hashAlgorithmIdentifier(SHA512.getObjectIdentifier())
            .serialNumber(BigInteger.valueOf(1337L))
            .nonce(TWO)
            .signedByThisTsa(true)
            .build();

        assertThat(mappedValidationResult).isEqualTo(expectedValidationResult);
    }

    @Test
    void mapsSigningCertificateIdentifier() {
        // given
        byte[] signingCertificateHash = "hash".getBytes(UTF_8);
        SigningCertificateHolder signingCertificateHolder = new SigningCertificateHolder(new AlgorithmIdentifier(id_SHA1),
            signingCertificateHash, null);

        ResponseProperties grantedResponseProperties = ResponseProperties.builder()
            .status(GRANTED)
            .hashAlgorithm(SHA512)
            .hash(repeat("a", 64).getBytes(UTF_8))
            .genTime(new Date())
            .serialNumber(1337L)
            .build();
        TimeStampResponse timeStampResponse = generateTimeStampResponseMock(grantedResponseProperties);

        // when
        TimeStampValidationResult mappedValidationResult = testSubject.map(timeStampResponse, signingCertificateHolder, true);

        // then
        SigningCertificateIdentifier expectedCertificateIdentifier = SigningCertificateIdentifier.builder()
            .hashAlgorithmOid(id_SHA1.getId())
            .hash(signingCertificateHash)
            .build();

        assertThat(mappedValidationResult.getSigningCertificateIdentifier()).isEqualTo(expectedCertificateIdentifier);
    }

    @Test
    void mapsSigningCertificateInformation() throws Exception {
        // given
        X509Certificate certificate = loadEcCertificate();
        X509CertificateHolder certificateHolder = new X509CertificateHolder(certificate.getEncoded());
        ZonedDateTime mappedExpirationDate = ZonedDateTime.parse("2030-12-27T12:00:00+01:00");
        SigningCertificateHolder signingCertificateHolder = new SigningCertificateHolder(new AlgorithmIdentifier(id_SHA1),
            "hash".getBytes(UTF_8), certificateHolder);

        ResponseProperties grantedResponseProperties = ResponseProperties.builder()
            .status(GRANTED)
            .signingCertificate(certificate)
            .hashAlgorithm(SHA512)
            .hash(repeat("a", 64).getBytes(UTF_8))
            .genTime(new Date())
            .serialNumber(1337L)
            .build();
        TimeStampResponse timeStampResponse = generateTimeStampResponseMock(grantedResponseProperties);

        given(dateConverterMock.toZonedDateTime(certificateHolder.getNotAfter())).willReturn(mappedExpirationDate);

        // when
        TimeStampValidationResult mappedValidationResult = testSubject.map(timeStampResponse, signingCertificateHolder, true);

        // then
        String expectedBase64EncodedCertificate = Base64.encodeBase64String(certificate.getEncoded());

        SigningCertificateInformation expectedCertificateInformation = SigningCertificateInformation.builder()
            .expirationDate(mappedExpirationDate)
            .serialNumber(certificate.getSerialNumber())
            .issuer(certificateHolder.getIssuer().toString())
            .base64EncodedCertificate(expectedBase64EncodedCertificate)
            .build();

        assertThat(mappedValidationResult.getSigningCertificateInformation()).isEqualTo(expectedCertificateInformation);
    }

}
