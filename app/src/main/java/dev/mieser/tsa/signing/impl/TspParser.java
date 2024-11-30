package dev.mieser.tsa.signing.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.commons.lang3.ArrayUtils;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.tsp.TimeStampReq;
import org.bouncycastle.asn1.tsp.TimeStampResp;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponse;

import dev.mieser.tsa.signing.api.exception.InvalidTspRequestException;
import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;

/**
 * Reads an ASN.1 DER encoded TSP requests and responses from an input stream.
 */
public class TspParser {

    /**
     * @param requestInputStream
     *     The input stream of an ASN.1 DER encoded TSP request, not {@code null}. The input stream will <b>not</b> be
     *     closed.
     * @return The parsed TSP request.
     * @throws InvalidTspRequestException
     *     When the input stream cannot be parsed as a TSP request.
     */
    public TimeStampRequest parseRequest(InputStream requestInputStream) throws InvalidTspRequestException {
        byte[] asn1EncodedTspRequest = readStream(requestInputStream)
            .orElseThrow(() -> new InvalidTspRequestException("TSP request data is missing"));

        try {
            TimeStampReq timeStampReq = TimeStampReq.getInstance(ASN1Sequence.fromByteArray(asn1EncodedTspRequest));
            return new TimeStampRequest(timeStampReq);
        } catch (Exception e) {
            throw new InvalidTspRequestException(e);
        }
    }

    /**
     * @param inputStream
     *     The input stream of an ASN.1 DER encoded TSP response, not {@code null}. The input stream will <b>not</b> be
     *     closed.
     * @return The parsed TSP response.
     * @throws InvalidTspResponseException
     *     When the input stream cannot be parsed as a TSP response.
     */
    public TimeStampResponse parseResponse(InputStream inputStream) throws InvalidTspResponseException {
        byte[] asn1EncodedTspResponse = readStream(inputStream)
            .orElseThrow(() -> new InvalidTspResponseException("TSP response data is missing"));

        try {
            TimeStampResp timeStampResp = TimeStampResp.getInstance(ASN1Sequence.fromByteArray(asn1EncodedTspResponse));
            return new TimeStampResponse(timeStampResp);
        } catch (Exception e) {
            throw new InvalidTspResponseException("Could not parse TSP response", e);
        }
    }

    private Optional<byte[]> readStream(InputStream stream) {
        try {
            byte[] content = stream.readAllBytes();
            if (ArrayUtils.isEmpty(content)) {
                return Optional.empty();
            }

            return Optional.of(content);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read stream", e);
        }
    }

}
