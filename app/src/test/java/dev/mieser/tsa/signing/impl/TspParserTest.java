package dev.mieser.tsa.signing.impl;

import static dev.mieser.tsa.signing.config.HashAlgorithm.SHA512;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.tsp.MessageImprint;
import org.bouncycastle.asn1.tsp.TimeStampReq;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponse;
import org.junit.jupiter.api.Test;

import dev.mieser.tsa.signing.api.exception.InvalidTspRequestException;
import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;

class TspParserTest {

    private final TspParser testSubject = new TspParser();

    @Test
    void parseRequestThrowsExceptionWhenRequestCannotBeParsed() {
        // given
        byte[] invalidTspRequest = "tsp request".getBytes(UTF_8);
        InputStream tspRequestInputStream = new ByteArrayInputStream(invalidTspRequest);

        // when / then
        assertThatExceptionOfType(InvalidTspRequestException.class)
            .isThrownBy(() -> testSubject.parseRequest(tspRequestInputStream));
    }

    @Test
    void parseRequestReturnsExpectedRequest() throws Exception {
        // given
        TimeStampReq timeStampRequest = createTimeStampRequest();
        InputStream tspRequestInputStream = new ByteArrayInputStream(timeStampRequest.getEncoded());

        // when
        TimeStampRequest parsedRequest = testSubject.parseRequest(tspRequestInputStream);

        // then
        assertThat(parsedRequest.getEncoded()).isEqualTo(timeStampRequest.getEncoded());
    }

    @Test
    void parseRequestDoesNotCloseInputStream() throws Exception {
        // given
        TimeStampReq timeStampRequest = createTimeStampRequest();
        CloseAwareInputStream tspRequestInputStream = new CloseAwareInputStream(
            new ByteArrayInputStream(timeStampRequest.getEncoded()));

        // when
        testSubject.parseRequest(tspRequestInputStream);

        // then
        assertThat(tspRequestInputStream.isClosed()).isFalse();
    }

    @Test
    void parseResponseThrowsExceptionWhenRequestCannotBeParsed() {
        // given
        byte[] invalidResponse = "tsp response".getBytes(UTF_8);
        InputStream tspResponseInputStream = new ByteArrayInputStream(invalidResponse);

        // when / then
        assertThatExceptionOfType(InvalidTspResponseException.class)
            .isThrownBy(() -> testSubject.parseResponse(tspResponseInputStream))
            .withMessage("Could not parse TSP response");
    }

    @Test
    void parseResponseReturnsExpectedResponse() throws Exception {
        // given
        byte[] timeStampResponse = readAsnEncodedTimeStampResponse();
        InputStream tspResponseInputStream = new ByteArrayInputStream(timeStampResponse);

        // when
        TimeStampResponse parsedResponse = testSubject.parseResponse(tspResponseInputStream);

        // then
        assertThat(parsedResponse.getEncoded()).isEqualTo(timeStampResponse);
    }

    @Test
    void parseResponseDoesNotCloseInputStream() throws Exception {
        // given
        byte[] timeStampResponse = readAsnEncodedTimeStampResponse();
        CloseAwareInputStream tspResponseInputStream = new CloseAwareInputStream(new ByteArrayInputStream(timeStampResponse));

        // when
        testSubject.parseResponse(tspResponseInputStream);

        // then
        assertThat(tspResponseInputStream.isClosed()).isFalse();
    }

    private TimeStampReq createTimeStampRequest() {
        var policyId = new ASN1ObjectIdentifier("1.2.3.4");
        var hashAlgorithmIdentifier = new AlgorithmIdentifier(new ASN1ObjectIdentifier(SHA512.getObjectIdentifier()));
        var messageImprint = new MessageImprint(hashAlgorithmIdentifier, "test".getBytes(UTF_8));
        var nonce = new ASN1Integer(BigInteger.TEN);

        return new TimeStampReq(messageImprint, policyId, nonce, ASN1Boolean.TRUE, null);
    }

    private byte[] readAsnEncodedTimeStampResponse() throws IOException {
        try (InputStream resourceInputStream = getClass().getResourceAsStream("digicert-response-2023-08-13.asn1")) {
            return resourceInputStream.readAllBytes();
        }
    }

    /**
     * @implNote Spying an Input Stream causes the input stream to return -1 when calling the {@code read()} methods (Java
     * 17.0.1, Mockito 4.0.0).
     */
    private static class CloseAwareInputStream extends FilterInputStream {

        private boolean closed;

        private CloseAwareInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
            super.close();
            this.closed = true;
        }

        public boolean isClosed() {
            return closed;
        }

    }

}
