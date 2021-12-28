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
import static org.mockito.BDDMockito.given;

import java.math.BigInteger;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.Date;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.tsp.TimeStampResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.mieser.tsa.datetime.api.DateConverter;
import dev.mieser.tsa.domain.SigningCertificateInformation;
import dev.mieser.tsa.domain.TimestampValidationResult;
import dev.mieser.tsa.testutil.TimeStampResponseGenerator.ResponseProperties;

@ExtendWith(MockitoExtension.class)
class TimestampVerificationResultMapperTest {

    private final DateConverter dateConverterMock;

    private final TimestampVerificationResultMapper testSubject;

    TimestampVerificationResultMapperTest(@Mock DateConverter dateConverterMock) {
        this.dateConverterMock = dateConverterMock;
        this.testSubject = new TimestampVerificationResultMapper(dateConverterMock);
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
        TimestampValidationResult mappedValidationResult = testSubject.map(timeStampResponse, null, false);

        // then
        TimestampValidationResult expectedValidationResult = TimestampValidationResult.builder()
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
        TimestampValidationResult mappedValidationResult = testSubject.map(timeStampResponse, null, false);

        // then
        TimestampValidationResult expectedValidationResult = TimestampValidationResult.builder()
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
        TimestampValidationResult mappedValidationResult = testSubject.map(timeStampResponse, null, true);

        // then
        TimestampValidationResult expectedValidationResult = TimestampValidationResult.builder()
            .status(GRANTED_WITH_MODS)
            .generationTime(genTime)
            .hash(sha512Hash)
            .hashAlgorithm(SHA512)
            .serialNumber(BigInteger.valueOf(1337L))
            .nonce(TWO)
            .signedByThisTsa(true)
            .build();

        assertThat(mappedValidationResult).isEqualTo(expectedValidationResult);
    }

    @Test
    void mapsSigningCertificateInformation() throws Exception {
        // given
        X509Certificate certificate = loadEcCertificate();
        X509CertificateHolder certificateHolder = new X509CertificateHolder(certificate.getEncoded());
        ZonedDateTime mappedExpirationDate = ZonedDateTime.parse("2030-12-27T12:00:00+01:00");

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
        TimestampValidationResult mappedValidationResult = testSubject.map(timeStampResponse, certificateHolder, true);

        // then
        SigningCertificateInformation expectedCertificateInformation = SigningCertificateInformation.builder()
            .expirationDate(mappedExpirationDate)
            .serialNumber(certificate.getSerialNumber())
            .issuer(certificateHolder.getIssuer().toString())
            .build();

        assertThat(mappedValidationResult.getSigningCertificateInformation()).isEqualTo(expectedCertificateInformation);
    }

}
