package dev.mieser.tsa.signing.impl.cert;

import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.id_aa_signingCertificate;
import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.id_aa_signingCertificateV2;
import static org.bouncycastle.asn1.x509.X509ObjectIdentifiers.id_SHA1;

import java.util.Collection;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
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

import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;

/**
 * Extracts the {@link X509CertificateHolder X.509 Certificate} whose corresponding private key was used to sign a TSP
 * Response. As specified in <a href="https://datatracker.ietf.org/doc/html/rfc3161.html">RFC 3161</a> (and
 * <a href="https://datatracker.ietf.org/doc/html/rfc5816">RFC 5816</a>), the public key certificate whose key was used
 * to sign the response with is referenced in the {@code SigningCertificate}/{@code SigningCertificateV2} attribute.
 */
@Slf4j
public class SigningCertificateExtractor {

    /**
     * @param timeStampResponse
     *     The response to extract the signing certificate from, not {@code null}.
     * @return The signing certificate in case certificates are included in the specified response or
     * {@link Optional#empty()}, when no timestamp token is present.
     * @throws InvalidTspResponseException
     *     When certificates are included in the response, but the signing certificate referenced in the
     *     {@code ESSCertID}/{@code ESSCertIDv2} is not included.
     */
    public Optional<SigningCertificateHolder> extractSigningCertificate(
        TimeStampResponse timeStampResponse) throws InvalidTspResponseException {
        TimeStampToken timeStampToken = timeStampResponse.getTimeStampToken();
        if (timeStampToken == null) {
            log.debug("The response does not contain a time stamp token.");
            return Optional.empty();
        }

        SigningCertificateIdentifier signingCertificateIdentifier = essCertIdHash(timeStampToken);
        Selector<X509CertificateHolder> signingCertificateSelector = new HashBasedCertificateSelector(
            signingCertificateIdentifier.algorithmIdentifier(), signingCertificateIdentifier.hash());

        Collection<X509CertificateHolder> matchingCertificates = timeStampToken.getCertificates()
            .getMatches(signingCertificateSelector);
        if (matchingCertificates.isEmpty() && containsCertificates(timeStampToken)) {
            throw new InvalidTspResponseException(
                "The signing certificate is not contained in the response, thus violating RFC 3161 / RFC 5816.");
        }

        X509CertificateHolder signingCertificate = matchingCertificates.isEmpty() ? null : matchingCertificates.iterator().next();
        return Optional.of(new SigningCertificateHolder(signingCertificateIdentifier.algorithmIdentifier(),
            signingCertificateIdentifier.hash(), signingCertificate));
    }

    /**
     * @param timeStampToken
     *     The timestamp token to check.
     * @return {@code true}, iff the time stamp token contains certificates.
     */
    private boolean containsCertificates(TimeStampToken timeStampToken) {
        return !timeStampToken.getCertificates().getMatches(null).isEmpty();
    }

    /**
     * @param timeStampToken
     *     The Time Stamp Token to extract the algorithm identifier from, not {@code null}.
     * @return The Object Identifier (OID) of the hash algorithm which was used to calculate the hash of the public key
     * certificate with, which is included in the {@code ESSCertID}/{@code ESSCertIDv2} attribute.
     * @throws InvalidTspResponseException
     *     When the TimeStamp Token violates RFC constraints.
     */
    private SigningCertificateIdentifier essCertIdHash(TimeStampToken timeStampToken) throws InvalidTspResponseException {
        if (hasSignedAttribute(timeStampToken, id_aa_signingCertificate)) {
            log.debug(
                "Signed 'SigningCertificate' attribute present in timestamp token. Using SHA-1 in accordance with RFC 2634.");
            return essCertIdHashFromSigningCertificate(timeStampToken);
        } else if (hasSignedAttribute(timeStampToken, id_aa_signingCertificateV2)) {
            log.debug("Signed 'SigningCertificateV2' attribute present.");
            return essCertIdHashFromSigningCertificateV2(timeStampToken);
        }

        throw new InvalidTspResponseException(
            "The timestamp token neither contains a signed 'SigningCertificate' nor a signed 'SigningCertificateV2' attribute.");
    }

    /**
     * @param timeStampToken
     *     The timestamp token to check, not {@code null}.
     * @return {@code true}, iff the timestamp token has a signed attribute with the specified OID.
     */
    private boolean hasSignedAttribute(TimeStampToken timeStampToken, ASN1ObjectIdentifier attributeIdentifier) {
        return timeStampToken.getSignedAttributes().get(attributeIdentifier) != null;
    }

    /**
     * @param timeStampToken
     *     The timestamp token which has a signed {@code SigningCertificate} (RFC 2634) attribute, not {@code null}.
     * @return A pair of the hash algorithm identifier and the hash of the signing certificate.
     * @throws InvalidTspResponseException
     *     When the timestamp token contains multiple {@code ESSCertID} identifiers.
     */
    private SigningCertificateIdentifier essCertIdHashFromSigningCertificate(
        TimeStampToken timeStampToken) throws InvalidTspResponseException {
        Attribute signingCertificateAttribute = timeStampToken.getSignedAttributes().get(id_aa_signingCertificate);
        ASN1Sequence signingCertificateSequence = (ASN1Sequence) signingCertificateAttribute.getAttrValues().getObjectAt(0);
        SigningCertificate signingCertificate = SigningCertificate.getInstance(signingCertificateSequence);
        ESSCertID[] certificateIdentifiers = signingCertificate.getCerts();
        if (certificateIdentifiers.length > 1) {
            throw new InvalidTspResponseException("Multiple ESSCertID identifiers present thus violating RFC 3161.");
        }

        return new SigningCertificateIdentifier(new AlgorithmIdentifier(id_SHA1), certificateIdentifiers[0].getCertHash());
    }

    /**
     * @param timeStampToken
     *     The timestamp token which has a signed {@code SigningCertificateV2} (RFC 5035) attribute, not {@code null}.
     * @return A pair of the hash algorithm identifier and the hash of the signing certificate.
     * @throws InvalidTspResponseException
     *     When the timestamp token contains multiple {@code ESSCertIDv2} identifiers.
     */
    private SigningCertificateIdentifier essCertIdHashFromSigningCertificateV2(
        TimeStampToken timeStampToken) throws InvalidTspResponseException {
        Attribute signingCertificateAttribute = timeStampToken.getSignedAttributes().get(id_aa_signingCertificateV2);
        ASN1Sequence attributeValues = new DLSequence(signingCertificateAttribute.getAttributeValues());
        ASN1Sequence signingCertificateSequence = (ASN1Sequence) attributeValues.getObjectAt(0);
        SigningCertificateV2 signingCertificate = SigningCertificateV2.getInstance(signingCertificateSequence);
        ESSCertIDv2[] certificateIdentifiers = signingCertificate.getCerts();
        if (certificateIdentifiers.length > 1) {
            throw new InvalidTspResponseException("Multiple ESSCertIDv2 identifiers present thus violating RFC 5816.");
        }

        AlgorithmIdentifier hashAlgorithm = certificateIdentifiers[0].getHashAlgorithm();
        byte[] hash = certificateIdentifiers[0].getCertHash();

        return new SigningCertificateIdentifier(hashAlgorithm, hash);
    }

    /**
     * Encapsulates the required information to identify the signing certificate.
     *
     * @param algorithmIdentifier
     *     The hash algorithm identifier, not {@code null}.
     * @param hash
     *     The hash value of the signing certificate, not {@code null}.
     */
    private record SigningCertificateIdentifier(AlgorithmIdentifier algorithmIdentifier, byte[] hash) {

    }

}
