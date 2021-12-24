package dev.mieser.tsa.signing.cert;

import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.ess.ESSCertID;
import org.bouncycastle.asn1.ess.ESSCertIDv2;
import org.bouncycastle.asn1.ess.SigningCertificate;
import org.bouncycastle.asn1.ess.SigningCertificateV2;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampToken;
import org.bouncycastle.util.Selector;

import java.util.Optional;

import static org.bouncycastle.asn1.nist.NISTObjectIdentifiers.id_sha256;
import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.id_aa_signingCertificate;
import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.id_aa_signingCertificateV2;
import static org.bouncycastle.asn1.x509.X509ObjectIdentifiers.id_SHA1;

/**
 * Extracts the {@link X509CertificateHolder X.509 Certificate} whose corresponding private key was used to sign a TSP Response. As specified in
 * <a href="https://datatracker.ietf.org/doc/html/rfc3161.html">RFC 3161</a> (and <a href="https://datatracker.ietf.org/doc/html/rfc5816">RFC 5816</a>), the
 * public key certificate whose key was used to sign the response with is referenced in the {@code SigningCertificate}/{@code SigningCertificateV2} attribute.
 */
@Slf4j
public class SigningCertificateExtractor {

    // TODO: JavaDoc
    public Optional<X509CertificateHolder> extractSigningCertificate(TimeStampResponse timeStampResponse) {
        TimeStampToken timeStampToken = timeStampResponse.getTimeStampToken();
        if (!containsCertificates(timeStampToken)) {
            log.debug("The timestamp token does not contain certificates.");
            return Optional.empty();
        }

        Pair<AlgorithmIdentifier, byte[]> algorithmIdentifierPair = essCertIdHash(timeStampToken);
        Selector<X509CertificateHolder> signingCertificateSelector =
                new HashBasedCertificateSelector(algorithmIdentifierPair.getLeft(), algorithmIdentifierPair.getRight());

        return timeStampToken.getCertificates().getMatches(signingCertificateSelector)
                .stream()
                .findAny();
    }

    /**
     * @param timeStampToken The timestamp token to check.
     * @return {@code true}, iff the token is not {@code null} and contains certificates.
     */
    private boolean containsCertificates(TimeStampToken timeStampToken) {
        return timeStampToken != null && !timeStampToken.getCertificates().getMatches(null).isEmpty();
    }

    /**
     * @param timeStampToken The Time Stamp Token to extract the algorithm identifier from, not {@code null}.
     * @return The Object Identifier (OID) of the hash algorithm which was used to calculate the hash of the public key certificate with, which is included
     * in the {@code ESSCertID}/{@code ESSCertIDv2} attribute.
     * @throws InvalidTspResponseException When the TimeStamp Token violates RFC constraints.
     */
    private Pair<AlgorithmIdentifier, byte[]> essCertIdHash(TimeStampToken timeStampToken) {
        if (hasSignedAttribute(timeStampToken, id_aa_signingCertificate)) {
            log.debug("Signed 'SigningCertificate' attribute present in timestamp token. Using SHA-1 in accordance with RFC 2634.");
            return essCertIdHashFromSigningCertificate(timeStampToken);
        } else if (hasSignedAttribute(timeStampToken, id_aa_signingCertificateV2)) {
            log.debug("Signed 'SigningCertificateV2' attribute present.");
            return essCertIdHashFromSigningCertificateV2(timeStampToken);
        }

        throw new InvalidTspResponseException(
                "The timestamp token neither contains a signed 'SigningCertificate' nor a signed 'SigningCertificateV2' attribute.");
    }

    /**
     * @param timeStampToken The timestamp token to check, not {@code null}.
     * @return {@code true}, iff the timestamp token has a signed attribute with the specified OID.
     */
    private boolean hasSignedAttribute(TimeStampToken timeStampToken, ASN1ObjectIdentifier attributeIdentifier) {
        return timeStampToken.getSignedAttributes().get(attributeIdentifier) != null;
    }

    /**
     * @param timeStampToken The timestamp token which has a signed {@code SigningCertificate} (RFC 2634) attribute, not {@code null}.
     * @return A pair of the hash algorithm identifier and the hash of the signing certificate.
     * @throws InvalidTspResponseException When the timestamp token contains multiple {@code ESSCertID} identifiers.
     */
    private Pair<AlgorithmIdentifier, byte[]> essCertIdHashFromSigningCertificate(TimeStampToken timeStampToken) {
        Attribute signingCertificateAttribute = timeStampToken.getSignedAttributes().get(id_aa_signingCertificate);
        SigningCertificate signingCertificate = SigningCertificate.getInstance(signingCertificateAttribute.getAttrValues());
        ESSCertID[] certificateIdentifiers = signingCertificate.getCerts();
        if (certificateIdentifiers.length > 1) {
            throw new InvalidTspResponseException("Multiple ESSCertID identifiers present.");
        }

        return Pair.of(new AlgorithmIdentifier(id_SHA1), certificateIdentifiers[0].getCertHash());
    }

    /**
     * @param timeStampToken The timestamp token which has a signed {@code SigningCertificateV2} (RFC 5035) attribute, not {@code null}.
     * @return A pair of the hash algorithm identifier and the hash of the signing certificate.
     * @throws InvalidTspResponseException When the timestamp token contains multiple {@code ESSCertIDv2} identifiers.
     */
    private Pair<AlgorithmIdentifier, byte[]> essCertIdHashFromSigningCertificateV2(TimeStampToken timeStampToken) {
        Attribute signingCertificateAttribute = timeStampToken.getSignedAttributes().get(id_aa_signingCertificateV2);
        SigningCertificateV2 signingCertificate = SigningCertificateV2.getInstance(new DLSequence(signingCertificateAttribute.getAttributeValues()));
        ESSCertIDv2[] certificateIdentifiers = signingCertificate.getCerts();
        if (certificateIdentifiers.length > 1) {
            throw new InvalidTspResponseException("Multiple ESSCertIDv2 identifiers present.");
        }

        AlgorithmIdentifier hashAlgorithm = certificateIdentifiers[0].getHashAlgorithm();
        byte[] hash = certificateIdentifiers[0].getCertHash();
        if (hashAlgorithm == null) {
            log.debug("No hash algorithm in ESSCertIDv2 identifier present. Using SHA-256 in accordance with RFC 5035.");
            return Pair.of(new AlgorithmIdentifier(id_sha256), hash);
        }

        return Pair.of(hashAlgorithm, hash);
    }

}
