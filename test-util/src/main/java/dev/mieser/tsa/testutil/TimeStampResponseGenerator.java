package dev.mieser.tsa.testutil;

import static dev.mieser.tsa.domain.ResponseStatus.GRANTED;
import static dev.mieser.tsa.domain.ResponseStatus.GRANTED_WITH_MODS;
import static java.util.Collections.emptyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

import java.io.IOException;
import java.math.BigInteger;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.*;

import lombok.*;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.tsp.TimeStampTokenInfo;
import org.bouncycastle.util.CollectionStore;
import org.bouncycastle.util.Store;

import dev.mieser.tsa.domain.FailureInfo;
import dev.mieser.tsa.domain.HashAlgorithm;
import dev.mieser.tsa.domain.ResponseStatus;

/**
 * Utility class to generate Time Stamp Response Mocks. Only supports a subset of the fields in a response.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TimeStampResponseGenerator {

    /**
     * The {@code status} values which indicate that a {@code TimeStampToken} must be present in the response.
     */
    private static final Set<ResponseStatus> STATUS_WITH_TOKEN = EnumSet.of(GRANTED, GRANTED_WITH_MODS);

    /**
     * @param responseProperties
     *     The properties to use to build the response, not {@code null}.
     * @return A Bouncy Castle Time Stamp Response Mock returning the specified values.
     */
    public static TimeStampResponse generateTimeStampResponseMock(ResponseProperties responseProperties) {
        if (responseProperties == null) {
            throw new IllegalArgumentException("Properties cannot be null.");
        }

        try {
            return generateTimeStampResponseInternal(responseProperties);
        } catch (IOException | CertificateEncodingException e) {
            throw new IllegalStateException("Could not generate Time Stamp Response mock.", e);
        }
    }

    private static TimeStampResponse generateTimeStampResponseInternal(
        ResponseProperties responseProperties) throws IOException, CertificateEncodingException {
        TimeStampResponse timeStampResponseMock = mock(TimeStampResponse.class, withSettings().lenient());

        PKIFailureInfo failureInfo = responseProperties.getFailureInfo() != null
            ? new PKIFailureInfo(responseProperties.getFailureInfo().getValue())
            : null;

        given(timeStampResponseMock.getStatus()).willReturn(responseProperties.getStatus().getValue());
        given(timeStampResponseMock.getStatusString()).willReturn(responseProperties.getStatusString());
        given(timeStampResponseMock.getFailInfo()).willReturn(failureInfo);

        if (!STATUS_WITH_TOKEN.contains(responseProperties.getStatus())) {
            return timeStampResponseMock;
        }

        TimeStampTokenInfo timeStampTokenInfoMock = mock(TimeStampTokenInfo.class, withSettings().lenient());

        ASN1ObjectIdentifier hashAlgorithmIdentifier = new ASN1ObjectIdentifier(
            responseProperties.getHashAlgorithm().getObjectIdentifier());
        given(timeStampTokenInfoMock.getMessageImprintAlgOID()).willReturn(hashAlgorithmIdentifier);
        given(timeStampTokenInfoMock.getHashAlgorithm()).willReturn(new AlgorithmIdentifier(hashAlgorithmIdentifier));
        given(timeStampTokenInfoMock.getMessageImprintDigest()).willReturn(responseProperties.getHash());
        given(timeStampTokenInfoMock.getGenTime()).willReturn(responseProperties.getGenTime());
        given(timeStampTokenInfoMock.getSerialNumber()).willReturn(BigInteger.valueOf(responseProperties.getSerialNumber()));
        given(timeStampTokenInfoMock.getNonce()).willReturn(responseProperties.getNonce());

        TimeStampToken timeStampTokenMock = mock(TimeStampToken.class, withSettings().lenient());
        List<X509CertificateHolder> certificates = responseProperties.getSigningCertificate() != null
            ? List.of(new X509CertificateHolder(responseProperties.getSigningCertificate().getEncoded()))
            : emptyList();
        Store<X509CertificateHolder> certificateStore = new CollectionStore<>(certificates);
        AttributeTable signedAttributes = responseProperties.getSignedAttributes() != null
            ? responseProperties.getSignedAttributes()
            : new AttributeTable(new Hashtable<>());

        given(timeStampTokenMock.getCertificates()).willReturn(certificateStore);
        given(timeStampTokenMock.getSignedAttributes()).willReturn(signedAttributes);

        given(timeStampTokenMock.getTimeStampInfo()).willReturn(timeStampTokenInfoMock);
        given(timeStampResponseMock.getTimeStampToken()).willReturn(timeStampTokenMock);

        return timeStampResponseMock;
    }

    @Data
    @Builder
    @AllArgsConstructor
    public static class ResponseProperties {

        /**
         * Cannot be null.
         */
        private ResponseStatus status;

        /**
         * Can be null.
         */
        private String statusString;

        /**
         * Can be null.
         */
        private FailureInfo failureInfo;

        /**
         * Cannot be null, when status indicates success.
         */
        private HashAlgorithm hashAlgorithm;

        /**
         * Cannot be null, when status indicates success.
         */
        private byte[] hash;

        private long serialNumber;

        /**
         * Can be null.
         */
        private BigInteger nonce;

        /**
         * Cannot be null, when status indicates success.
         */
        private Date genTime;

        /**
         * Can be null.
         */
        private AttributeTable signedAttributes;

        /**
         * Can be null.
         */
        private X509Certificate signingCertificate;

    }

}
