package dev.mieser.tsa.signing.mapper;

import dev.mieser.tsa.datetime.api.DateConverter;
import dev.mieser.tsa.domain.TimestampRequestData;
import dev.mieser.tsa.domain.TimestampResponseData;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.cmp.PKIStatus;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.Date;

import static dev.mieser.tsa.domain.HashAlgorithm.SHA256;
import static dev.mieser.tsa.domain.HashAlgorithm.SHA512;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class TimestampResponseMapperTest {

    private final DateConverter dateConverterMock;

    private final TimestampResponseMapper testSubject;

    public TimestampResponseMapperTest(@Mock DateConverter dateConverterMock) {
        this.dateConverterMock = dateConverterMock;
        this.testSubject = new TimestampResponseMapper(dateConverterMock);
    }

    @Test
    void mapThrowsExceptionWhenTspRequestCannotBeConvertedToAsnObject(@Mock TimeStampRequest timeStampRequestMock,
            @Mock TimeStampResponse timeStampResponseMock) throws IOException {
        // given
        var hashAlgorithmOid = new ASN1ObjectIdentifier(SHA256.getObjectIdentifier());

        IOException thrownException = new IOException();

        given(timeStampRequestMock.getMessageImprintAlgOID()).willReturn(hashAlgorithmOid);
        given(timeStampRequestMock.getEncoded()).willThrow(thrownException);

        // when / then
        assertThatIllegalStateException()
                .isThrownBy(() -> testSubject.map(timeStampRequestMock, timeStampResponseMock))
                .withMessage("Error converting object to ASN.1.")
                .withCause(thrownException);
    }

    @Test
    void mapMapsUnsuccessfulResponseToExpectedObject(@Mock TimeStampRequest timeStampRequestMock, @Mock TimeStampResponse timeStampResponseMock)
            throws IOException {
        // given
        var statusString = "Unsupported Hash Algorithm";
        var hashAlgorithmOid = new ASN1ObjectIdentifier(SHA256.getObjectIdentifier());
        byte[] asnEncodedResponse = "TSP response".getBytes(UTF_8);

        given(timeStampRequestMock.getMessageImprintAlgOID()).willReturn(hashAlgorithmOid);

        given(timeStampResponseMock.getStatus()).willReturn(PKIStatus.REJECTION);
        given(timeStampResponseMock.getStatusString()).willReturn(statusString);
        given(timeStampResponseMock.getFailInfo()).willReturn(new PKIFailureInfo(PKIFailureInfo.badAlg));
        given(timeStampResponseMock.getEncoded()).willReturn(asnEncodedResponse);

        // when
        TimestampResponseData actualResponseData = testSubject.map(timeStampRequestMock, timeStampResponseMock);

        // then
        TimestampRequestData expectedRequestData = TimestampRequestData.builder()
                .hashAlgorithm(SHA256)
                .build();

        TimestampResponseData expectedResponseData = TimestampResponseData.builder()
                .status(PKIStatus.REJECTION)
                .failureInfo(PKIFailureInfo.badAlg)
                .statusString(statusString)
                .request(expectedRequestData)
                .asnEncoded(asnEncodedResponse)
                .build();

        assertThat(actualResponseData).isEqualTo(expectedResponseData);
    }

    @Test
    void mapMapsSuccessfulResponseToExpectedObject(@Mock TimeStampRequest timeStampRequestMock, @Mock TimeStampToken timeStampTokenMock,
            @Mock TimeStampTokenInfo tokenInfoMock, @Mock TimeStampResponse timeStampResponseMock) throws IOException {
        // given
        var hashAlgorithmOid = new ASN1ObjectIdentifier(SHA256.getObjectIdentifier());
        byte[] asnEncodedResponse = "TSP response".getBytes(UTF_8);
        BigInteger responseSerialNumber = BigInteger.valueOf(1337L);
        ZonedDateTime genTime = ZonedDateTime.parse("2021-11-13T16:20:51+01:00");
        Date genTimeDate = Date.from(genTime.toInstant());

        given(timeStampRequestMock.getMessageImprintAlgOID()).willReturn(hashAlgorithmOid);

        given(timeStampResponseMock.getStatus()).willReturn(PKIStatus.GRANTED);
        given(timeStampResponseMock.getTimeStampToken()).willReturn(timeStampTokenMock);
        given(timeStampResponseMock.getEncoded()).willReturn(asnEncodedResponse);

        given(timeStampTokenMock.getTimeStampInfo()).willReturn(tokenInfoMock);

        given(tokenInfoMock.getSerialNumber()).willReturn(responseSerialNumber);
        given(tokenInfoMock.getGenTime()).willReturn(genTimeDate);

        given(dateConverterMock.toZonedDateTime(genTimeDate)).willReturn(genTime);

        // when
        TimestampResponseData actualResponseData = testSubject.map(timeStampRequestMock, timeStampResponseMock);

        // then
        TimestampRequestData expectedRequestData = TimestampRequestData.builder()
                .hashAlgorithm(SHA256)
                .build();

        TimestampResponseData expectedResponseData = TimestampResponseData.builder()
                .status(PKIStatus.GRANTED)
                .serialNumber(responseSerialNumber)
                .generationTime(genTime)
                .request(expectedRequestData)
                .asnEncoded(asnEncodedResponse)
                .build();

        assertThat(actualResponseData).isEqualTo(expectedResponseData);
    }

    @Test
    void mapMapsRequestData(@Mock TimeStampRequest timeStampRequestMock, @Mock TimeStampResponse timeStampResponseMock) throws IOException {
        // given
        var hashAlgorithmOid = new ASN1ObjectIdentifier(SHA512.getObjectIdentifier());
        byte[] asnEncodedRequest = "TSP request".getBytes(UTF_8);
        byte[] hash = "signed hash".getBytes(UTF_8);
        BigInteger nonce = BigInteger.valueOf(515L);
        String policyOid = "1.2.3.4";

        given(timeStampRequestMock.getMessageImprintAlgOID()).willReturn(hashAlgorithmOid);
        given(timeStampRequestMock.getMessageImprintDigest()).willReturn(hash);
        given(timeStampRequestMock.getNonce()).willReturn(nonce);
        given(timeStampRequestMock.getCertReq()).willReturn(true);
        given(timeStampRequestMock.getReqPolicy()).willReturn(new ASN1ObjectIdentifier(policyOid));
        given(timeStampRequestMock.getEncoded()).willReturn(asnEncodedRequest);

        // when
        TimestampResponseData actualResponseData = testSubject.map(timeStampRequestMock, timeStampResponseMock);

        // then
        TimestampRequestData expectedRequestData = TimestampRequestData.builder()
                .nonce(nonce)
                .hashAlgorithm(SHA512)
                .tsaPolicyId(policyOid)
                .hash(hash)
                .certificateRequested(true)
                .asnEncoded(asnEncodedRequest)
                .build();

        TimestampResponseData expectedResponseData = TimestampResponseData.builder()
                .request(expectedRequestData)
                .build();

        assertThat(actualResponseData).isEqualTo(expectedResponseData);
    }

}
