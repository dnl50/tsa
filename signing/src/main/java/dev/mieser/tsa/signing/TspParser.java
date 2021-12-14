package dev.mieser.tsa.signing;

import dev.mieser.tsa.signing.api.exception.InvalidTspRequestException;
import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;
import org.apache.commons.io.input.CloseShieldInputStream;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.tsp.TimeStampReq;
import org.bouncycastle.asn1.tsp.TimeStampResp;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponse;

import java.io.InputStream;

/**
 * Reads an ASN.1 DER encoded TSP requests and responses from an input stream.
 */
public class TspParser {

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

    /**
     * @param inputStream The input stream of an ASN.1 DER encoded TSP response, not {@code null}. The input stream will <b>not</b> be closed.
     * @return The parsed TSP response.
     * @throws InvalidTspResponseException When the input stream cannot be parsed to an TSP response.
     */
    public TimeStampResponse parseResponse(InputStream inputStream) {
        try (ASN1InputStream asnInputStream = new ASN1InputStream(CloseShieldInputStream.wrap(inputStream))) {
            TimeStampResp timeStampResp = TimeStampResp.getInstance(asnInputStream.readObject());

            return new TimeStampResponse(timeStampResp);
        } catch (Exception e) {
            throw new InvalidTspResponseException("Could not parse TSP response", e);
        }
    }

}
