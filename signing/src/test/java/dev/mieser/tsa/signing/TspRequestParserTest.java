package dev.mieser.tsa.signing;

import dev.mieser.tsa.signing.api.exception.InvalidTspRequestException;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.tsp.MessageImprint;
import org.bouncycastle.asn1.tsp.TimeStampReq;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.tsp.TimeStampRequest;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import static dev.mieser.tsa.domain.HashAlgorithm.SHA512;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;

class TspRequestParserTest {

    private final TspRequestParser testSubject = new TspRequestParser();

    @Test
    void parseRequestThrowsExceptionWhenRequestCannotBeParsed() {
        // given
        byte[] invalidTspRequest = "tsp request".getBytes(UTF_8);
        InputStream tspRequestInputStream = new ByteArrayInputStream(invalidTspRequest);

        // when / then
        assertThatExceptionOfType(InvalidTspRequestException.class)
                .isThrownBy(() -> testSubject.parseRequest(tspRequestInputStream))
                .withMessage("Could not parse TSP request.");
    }

    @Test
    void parseRequestReturnsExpectedRequest() throws IOException {
        // given
        TimeStampReq timeStampRequest = createTimeStampRequest();
        InputStream tspRequestInputStream = new ByteArrayInputStream(timeStampRequest.getEncoded());

        // when
        TimeStampRequest parsedRequest = testSubject.parseRequest(tspRequestInputStream);

        // then
        assertThat(parsedRequest.getEncoded()).isEqualTo(timeStampRequest.getEncoded());
    }

    @Test
    void parseRequestDoesNotCloseInputStream() throws IOException {
        // given
        TimeStampReq timeStampRequest = createTimeStampRequest();
        InputStream tspRequestInputStreamSpy = spy(new ByteArrayInputStream(timeStampRequest.getEncoded()));

        // when
        testSubject.parseRequest(tspRequestInputStreamSpy);

        // then
        then(tspRequestInputStreamSpy).should(never()).close();
    }

    private TimeStampReq createTimeStampRequest() {
        var policyId = new ASN1ObjectIdentifier("1.2.3.4");
        var hashAlgorithmIdentifier = new AlgorithmIdentifier(new ASN1ObjectIdentifier(SHA512.getObjectIdentifier()));
        var messageImprint = new MessageImprint(hashAlgorithmIdentifier, "test".getBytes(UTF_8));
        var nonce = new ASN1Integer(BigInteger.TEN);

        return new TimeStampReq(messageImprint, policyId, nonce, ASN1Boolean.TRUE, null);
    }

}
