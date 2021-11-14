package dev.mieser.tsa.signing;

import dev.mieser.tsa.signing.api.exception.InvalidTspRequestException;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.tsp.TimeStampReq;
import org.bouncycastle.tsp.TimeStampRequest;

import java.io.InputStream;

/**
 * Reads an ASN.1 DER encoded TSP request from an input stream.
 */
public class TspRequestParser {

    /**
     * @param requestInputStream The input stream of an ASN.1 DER encoded TSP request, not {@code null}. The input stream will <b>not</b> be closed.
     * @return The parsed TSP request.
     * @throws InvalidTspRequestException When the input stream cannot be parsed to an TSP request.
     */
    public TimeStampRequest parseRequest(InputStream requestInputStream) {
        try (ASN1InputStream asnInputStream = new ASN1InputStream(CloseShieldInputStream.wrap(requestInputStream))) {
            TimeStampReq timeStampReq = TimeStampReq.getInstance(asnInputStream.readObject());

            return new TimeStampRequest(timeStampReq);
        } catch (Exception e) {
            throw new InvalidTspRequestException("Could not parse TSP request.", e);
        }
    }

}
