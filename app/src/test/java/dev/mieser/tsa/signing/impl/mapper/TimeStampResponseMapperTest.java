package dev.mieser.tsa.signing.impl.mapper;

import static dev.mieser.tsa.domain.FailureInfo.BAD_ALGORITHM;
import static dev.mieser.tsa.domain.HashAlgorithm.SHA256;
import static dev.mieser.tsa.domain.HashAlgorithm.SHA512;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.BDDMockito.given;

import java.io.IOException;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Date;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.cmp.PKIStatus;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.mieser.tsa.datetime.api.DateConverter;
import dev.mieser.tsa.domain.ResponseStatus;
import dev.mieser.tsa.domain.TimeStampRequestData;
import dev.mieser.tsa.domain.TimeStampResponseData;

@ExtendWith(MockitoExtension.class)
class TimeStampResponseMapperTest {

    @Mock
    private DateConverter dateConverterMock;

    private TimeStampResponseMapper testSubject;

    @BeforeEach
    void setUp() {
        this.testSubject = new TimeStampResponseMapper(dateConverterMock);
    }

    @Test
    void mapThrowsExceptionWhenTspRequestCannotBeConvertedToAsnObject(@Mock TimeStampRequest timeStampRequestMock,
        @Mock TimeStampResponse timeStampResponseMock) throws IOException {
        // given
        var hashAlgorithmOid = new ASN1ObjectIdentifier(SHA256.getObjectIdentifier());
        Date receptionTime = new Date();

        IOException thrownException = new IOException();

        given(timeStampRequestMock.getMessageImprintAlgOID()).willReturn(hashAlgorithmOid);
        given(timeStampRequestMock.getEncoded()).willThrow(thrownException);

        // when / then
        assertThatIllegalStateException()
            .isThrownBy(() -> testSubject.map(timeStampRequestMock, timeStampResponseMock, receptionTime))
            .withMessage("Error converting object to ASN.1.")
            .withCause(thrownException);
    }

    @Test
    void mapMapsUnsuccessfulResponseToExpectedObject(@Mock TimeStampRequest timeStampRequestMock,
        @Mock TimeStampResponse timeStampResponseMock) throws IOException {
        // given
        var statusString = "Unsupported Hash Algorithm";
        var hashAlgorithmOid = new ASN1ObjectIdentifier(SHA256.getObjectIdentifier());
        byte[] requestHash = "sha256".getBytes(UTF_8);
        byte[] asnEncodedResponse = "TSP response".getBytes(UTF_8);
        ZonedDateTime receptionTime = ZonedDateTime.parse("2021-12-25T20:30:36+01:00");
        Date receptionTimeAsDate = Date.from(receptionTime.toInstant());

        given(timeStampRequestMock.getMessageImprintAlgOID()).willReturn(hashAlgorithmOid);
        given(timeStampRequestMock.getMessageImprintDigest()).willReturn(requestHash);

        given(timeStampResponseMock.getStatus()).willReturn(PKIStatus.REJECTION);
        given(timeStampResponseMock.getStatusString()).willReturn(statusString);
        given(timeStampResponseMock.getFailInfo()).willReturn(new PKIFailureInfo(PKIFailureInfo.badAlg));
        given(timeStampResponseMock.getEncoded()).willReturn(asnEncodedResponse);
        given(dateConverterMock.toZonedDateTime(receptionTimeAsDate)).willReturn(receptionTime);

        // when
        TimeStampResponseData actualResponseData = testSubject.map(timeStampRequestMock, timeStampResponseMock,
            receptionTimeAsDate);

        // then
        TimeStampRequestData expectedRequestData = TimeStampRequestData.builder(SHA256, requestHash).build();

        TimeStampResponseData expectedResponseData = TimeStampResponseData
            .builder(ResponseStatus.REJECTION, receptionTime, expectedRequestData, asnEncodedResponse)
            .failureInfo(BAD_ALGORITHM)
            .statusString(statusString)
            .build();

        assertThat(actualResponseData).isEqualTo(expectedResponseData);
    }

    @Test
    void mapMapsSuccessfulResponseToExpectedObject(@Mock TimeStampRequest timeStampRequestMock,
        @Mock TimeStampToken timeStampTokenMock,
        @Mock TimeStampTokenInfo tokenInfoMock, @Mock TimeStampResponse timeStampResponseMock) throws IOException {
        // given
        var hashAlgorithmOid = new ASN1ObjectIdentifier(SHA256.getObjectIdentifier());
        byte[] requestHash = "sha256".getBytes(UTF_8);
        byte[] asnEncodedResponse = "TSP response".getBytes(UTF_8);
        BigInteger responseSerialNumber = BigInteger.valueOf(1337L);
        ZonedDateTime genTime = ZonedDateTime.parse("2021-11-13T16:20:51+01:00");
        Date genTimeDate = Date.from(genTime.toInstant());

        given(timeStampRequestMock.getMessageImprintAlgOID()).willReturn(hashAlgorithmOid);
        given(timeStampRequestMock.getMessageImprintDigest()).willReturn(requestHash);

        given(timeStampResponseMock.getStatus()).willReturn(PKIStatus.GRANTED);
        given(timeStampResponseMock.getTimeStampToken()).willReturn(timeStampTokenMock);
        given(timeStampResponseMock.getEncoded()).willReturn(asnEncodedResponse);

        given(timeStampTokenMock.getTimeStampInfo()).willReturn(tokenInfoMock);

        given(tokenInfoMock.getSerialNumber()).willReturn(responseSerialNumber);
        given(tokenInfoMock.getGenTime()).willReturn(genTimeDate);

        given(dateConverterMock.toZonedDateTime(genTimeDate)).willReturn(genTime);

        // when
        TimeStampResponseData actualResponseData = testSubject.map(timeStampRequestMock, timeStampResponseMock, genTimeDate);

        // then
        TimeStampRequestData expectedRequestData = TimeStampRequestData.builder(SHA256, requestHash).build();

        TimeStampResponseData expectedResponseData = TimeStampResponseData
            .builder(ResponseStatus.GRANTED, genTime, expectedRequestData, asnEncodedResponse)
            .serialNumber(responseSerialNumber)
            .generationTime(genTime)
            .build();

        assertThat(actualResponseData).isEqualTo(expectedResponseData);
    }

    @Test
    void mapMapsRequestData(@Mock TimeStampRequest timeStampRequestMock,
        @Mock TimeStampResponse timeStampResponseMock) throws IOException {
        // given
        var hashAlgorithmOid = new ASN1ObjectIdentifier(SHA512.getObjectIdentifier());
        byte[] asnEncodedRequest = "TSP request".getBytes(UTF_8);
        byte[] asnEncodedResponse = "TSP response".getBytes(UTF_8);
        byte[] hash = "signed hash".getBytes(UTF_8);
        BigInteger nonce = BigInteger.valueOf(515L);
        String policyOid = "1.2.3.4";
        ZonedDateTime receptionTime = ZonedDateTime.parse("2021-12-25T20:30:36+01:00");
        Date receptionTimeAsDate = Date.from(receptionTime.toInstant());

        given(timeStampRequestMock.getMessageImprintAlgOID()).willReturn(hashAlgorithmOid);
        given(timeStampRequestMock.getMessageImprintDigest()).willReturn(hash);
        given(timeStampRequestMock.getNonce()).willReturn(nonce);
        given(timeStampRequestMock.getCertReq()).willReturn(true);
        given(timeStampRequestMock.getReqPolicy()).willReturn(new ASN1ObjectIdentifier(policyOid));
        given(timeStampRequestMock.getEncoded()).willReturn(asnEncodedRequest);
        given(dateConverterMock.toZonedDateTime(receptionTimeAsDate)).willReturn(receptionTime);

        given(timeStampResponseMock.getEncoded()).willReturn(asnEncodedResponse);

        // when
        TimeStampResponseData actualResponseData = testSubject.map(timeStampRequestMock, timeStampResponseMock,
            receptionTimeAsDate);

        // then
        TimeStampRequestData expectedRequestData = TimeStampRequestData.builder(SHA512, hash)
            .nonce(nonce)
            .tsaPolicyId(policyOid)
            .certificateRequested(true)
            .asnEncoded(asnEncodedRequest)
            .build();

        TimeStampResponseData expectedResponseData = TimeStampResponseData
            .builder(ResponseStatus.GRANTED, receptionTime, expectedRequestData, asnEncodedResponse)
            .request(expectedRequestData)
            .build();

        assertThat(actualResponseData).isEqualTo(expectedResponseData);
    }

}
