package dev.mieser.tsa.domain;

import java.math.BigInteger;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Holds the Information which is included in a {@code TimeStampReq} as defined in
 * <a href="https://www.ietf.org/rfc/rfc3161.txt">RFC3161</a>.
 */
@Data
@Builder(builderMethodName = "")
@AllArgsConstructor
public class TimeStampRequestData {

    /**
     * The hash algorithm which was used.
     * <p/>
     * Not {@code null}.
     */
    private final HashAlgorithm hashAlgorithm;

    /**
     * The data which should be signed.
     * <p/>
     * Not {@code null} nor empty.
     */
    private final byte[] hash;

    /**
     * The nonce which was included.
     * <p/>
     * Can be {@code null}.
     */
    private final BigInteger nonce;

    /**
     * Specifies whether the TSA's public key should be included in the response.
     */
    private final boolean certificateRequested;

    /**
     * The OID of the TSA policy, under which the {@code TimeStampToken} should be provided.
     */
    private final String tsaPolicyId;

    /**
     * The ASN.1 encoded request.
     */
    private final byte[] asnEncoded;

    public static TimeStampRequestDataBuilder builder(HashAlgorithm hashAlgorithm, byte[] hash) {
        return new TimeStampRequestDataBuilder()
            .hashAlgorithm(hashAlgorithm)
            .hash(hash);
    }

}
