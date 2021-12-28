package dev.mieser.tsa.signing.cert;

import static dev.mieser.tsa.domain.HashAlgorithm.SHA256;
import static dev.mieser.tsa.domain.ResponseStatus.GRANTED;
import static dev.mieser.tsa.testutil.TestCertificateLoader.loadRsaCertificate;
import static dev.mieser.tsa.testutil.TimeStampResponseGenerator.generateTimeStampResponseMock;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.repeat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.id_aa_signingCertificate;
import static org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers.id_aa_signingCertificateV2;
import static org.bouncycastle.asn1.x509.X509ObjectIdentifiers.id_SHA1;

import java.security.MessageDigest;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.asn1.DLSet;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.ess.ESSCertID;
import org.bouncycastle.asn1.ess.ESSCertIDv2;
import org.bouncycastle.asn1.ess.SigningCertificate;
import org.bouncycastle.asn1.ess.SigningCertificateV2;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.tsp.TimeStampResponse;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;
import dev.mieser.tsa.testutil.TimeStampResponseGenerator.ResponseProperties;

@ExtendWith(MockitoExtension.class)
class SigningCertificateExtractorTest {

    private final SigningCertificateExtractor testSubject = new SigningCertificateExtractor();

    @Test
    void extractSigningCertificateReturnsEmptyOptionalWhenNoTimeStampTokenIsIncluded(
        @Mock TimeStampResponse timeStampResponseMock) {
        // given / when
        Optional<X509CertificateHolder> signingCertificate = testSubject.extractSigningCertificate(timeStampResponseMock);

        // then
        assertThat(signingCertificate).isEmpty();
    }

    @Test
    void extractSigningCertificateReturnsEmptyOptionalWhenNoCertificatesAreIncluded() {
        // given
        ResponseProperties responseProperties = ResponseProperties.builder()
            .status(GRANTED)
            .genTime(new Date())
            .hashAlgorithm(SHA256)
            .hash(repeat("b", 32).getBytes(UTF_8))
            .build();

        TimeStampResponse timeStampResponseMock = generateTimeStampResponseMock(responseProperties);

        // when
        Optional<X509CertificateHolder> signingCertificate = testSubject.extractSigningCertificate(timeStampResponseMock);

        // then
        assertThat(signingCertificate).isEmpty();
    }

    @Test
    void extractSigningInformationThrowsExceptionWhenCertificatesAreIncludedButNoSignedCertAttributeIsPresent() {
        // given
        ResponseProperties responseProperties = ResponseProperties.builder()
            .status(GRANTED)
            .genTime(new Date())
            .hashAlgorithm(SHA256)
            .hash(repeat("b", 32).getBytes(UTF_8))
            .signingCertificate(loadRsaCertificate())
            .build();

        TimeStampResponse timeStampResponseMock = generateTimeStampResponseMock(responseProperties);

        // when / then
        assertThatExceptionOfType(InvalidTspResponseException.class)
            .isThrownBy(() -> testSubject.extractSigningCertificate(timeStampResponseMock))
            .withMessage(
                "The timestamp token neither contains a signed 'SigningCertificate' nor a signed 'SigningCertificateV2' attribute.");
    }

    @Nested
    class SigningCertificateTest {

        @Test
        void throwsExceptionWhenMultipleCertificateIdentifiersArePresent() {
            // given
            ESSCertID firstCertId = new ESSCertID(repeat("a", 20).getBytes(UTF_8));
            ESSCertID secondCertId = new ESSCertID(repeat("b", 20).getBytes(UTF_8));
            ASN1Sequence certSequence = new DLSequence(new ASN1Encodable[] { firstCertId, secondCertId });
            ASN1Sequence signingCertificateSequence = new DLSequence(certSequence);
            var signingCertificateAttribute = new Attribute(id_aa_signingCertificate, new DLSet(signingCertificateSequence));

            AttributeTable signedAttributeTable = new AttributeTable(new Hashtable<>(Map.of(
                id_aa_signingCertificate, signingCertificateAttribute)));

            ResponseProperties responseProperties = ResponseProperties.builder()
                .status(GRANTED)
                .genTime(new Date())
                .hashAlgorithm(SHA256)
                .hash(repeat("b", 32).getBytes(UTF_8))
                .signingCertificate(loadRsaCertificate())
                .signedAttributes(signedAttributeTable)
                .build();

            TimeStampResponse timeStampResponseMock = generateTimeStampResponseMock(responseProperties);

            // when / then
            assertThatExceptionOfType(InvalidTspResponseException.class)
                .isThrownBy(() -> testSubject.extractSigningCertificate(timeStampResponseMock))
                .withMessage("Multiple ESSCertID identifiers present thus violating RFC 3161.");
        }

        @Test
        void throwsExceptionWhenSigningCertificateNotPresent() {
            // given
            SigningCertificate signingCertificate = new SigningCertificate(new ESSCertID(repeat("a", 20).getBytes(UTF_8)));
            var signingCertificateAttribute = new Attribute(id_aa_signingCertificate,
                new DLSet(signingCertificate.toASN1Primitive()));

            AttributeTable signedAttributeTable = new AttributeTable(new Hashtable<>(Map.of(
                id_aa_signingCertificate, signingCertificateAttribute)));

            ResponseProperties responseProperties = ResponseProperties.builder()
                .status(GRANTED)
                .genTime(new Date())
                .hashAlgorithm(SHA256)
                .hash(repeat("b", 32).getBytes(UTF_8))
                .signingCertificate(loadRsaCertificate())
                .signedAttributes(signedAttributeTable)
                .build();

            TimeStampResponse timeStampResponseMock = generateTimeStampResponseMock(responseProperties);

            // when / then
            assertThatExceptionOfType(InvalidTspResponseException.class)
                .isThrownBy(() -> testSubject.extractSigningCertificate(timeStampResponseMock))
                .withMessage("The signing certificate is not contained in the response, thus violating RFC 3161 / RFC 5816.");
        }

        @Test
        void returnsSigningCertificateWhenPresent() throws Exception {
            // given
            X509Certificate certificate = loadRsaCertificate();
            byte[] encodedCertificate = certificate.getEncoded();
            byte[] sha1CertificateHash = MessageDigest.getInstance("SHA1").digest(encodedCertificate);

            SigningCertificate signingCertificate = new SigningCertificate(new ESSCertID(sha1CertificateHash));
            var signingCertificateAttribute = new Attribute(id_aa_signingCertificate,
                new DLSet(signingCertificate.toASN1Primitive()));

            AttributeTable signedAttributeTable = new AttributeTable(new Hashtable<>(Map.of(
                id_aa_signingCertificate, signingCertificateAttribute)));

            ResponseProperties responseProperties = ResponseProperties.builder()
                .status(GRANTED)
                .genTime(new Date())
                .hashAlgorithm(SHA256)
                .hash(repeat("b", 32).getBytes(UTF_8))
                .signingCertificate(certificate)
                .signedAttributes(signedAttributeTable)
                .build();

            TimeStampResponse timeStampResponseMock = generateTimeStampResponseMock(responseProperties);

            // when
            Optional<X509CertificateHolder> signingCertificateHolder = testSubject
                .extractSigningCertificate(timeStampResponseMock);

            // then
            X509CertificateHolder expectedCertificateHolder = new X509CertificateHolder(encodedCertificate);

            assertThat(signingCertificateHolder).contains(expectedCertificateHolder);
        }

    }

    @Nested
    class SigningCertificateV2Test {

        @Test
        void throwsExceptionWhenMultipleCertificateIdentifiersArePresent() {
            // given
            ESSCertIDv2 firstCertId = new ESSCertIDv2(repeat("a", 20).getBytes(UTF_8));
            ESSCertIDv2 secondCertId = new ESSCertIDv2(repeat("b", 20).getBytes(UTF_8));
            ASN1Sequence certSequence = new DLSequence(new ASN1Encodable[] { firstCertId, secondCertId });
            ASN1Sequence signingCertificateSequence = new DLSequence(certSequence);
            var signingCertificateAttribute = new Attribute(id_aa_signingCertificateV2, new DLSet(signingCertificateSequence));

            AttributeTable signedAttributeTable = new AttributeTable(new Hashtable<>(Map.of(
                id_aa_signingCertificateV2, signingCertificateAttribute)));

            ResponseProperties responseProperties = ResponseProperties.builder()
                .status(GRANTED)
                .genTime(new Date())
                .hashAlgorithm(SHA256)
                .hash(repeat("b", 32).getBytes(UTF_8))
                .signingCertificate(loadRsaCertificate())
                .signedAttributes(signedAttributeTable)
                .build();

            TimeStampResponse timeStampResponseMock = generateTimeStampResponseMock(responseProperties);

            // when / then
            assertThatExceptionOfType(InvalidTspResponseException.class)
                .isThrownBy(() -> testSubject.extractSigningCertificate(timeStampResponseMock))
                .withMessage("Multiple ESSCertIDv2 identifiers present thus violating RFC 5816.");
        }

        @Test
        void throwsExceptionWhenSigningCertificateNotPresent() {
            // given
            SigningCertificateV2 signingCertificate = new SigningCertificateV2(new ESSCertIDv2(repeat("a", 20).getBytes(UTF_8)));
            var signingCertificateAttribute = new Attribute(id_aa_signingCertificateV2,
                new DLSet(signingCertificate.toASN1Primitive()));

            AttributeTable signedAttributeTable = new AttributeTable(new Hashtable<>(Map.of(
                id_aa_signingCertificateV2, signingCertificateAttribute)));

            ResponseProperties responseProperties = ResponseProperties.builder()
                .status(GRANTED)
                .genTime(new Date())
                .hashAlgorithm(SHA256)
                .hash(repeat("b", 32).getBytes(UTF_8))
                .signingCertificate(loadRsaCertificate())
                .signedAttributes(signedAttributeTable)
                .build();

            TimeStampResponse timeStampResponseMock = generateTimeStampResponseMock(responseProperties);

            // when / then
            assertThatExceptionOfType(InvalidTspResponseException.class)
                .isThrownBy(() -> testSubject.extractSigningCertificate(timeStampResponseMock))
                .withMessage("The signing certificate is not contained in the response, thus violating RFC 3161 / RFC 5816.");
        }

        @Test
        void returnsSigningCertificateWithMatchingSha256HashWhenHashAlgorithmNotSpecified() throws Exception {
            // given
            X509Certificate certificate = loadRsaCertificate();
            byte[] encodedCertificate = certificate.getEncoded();
            byte[] sha256CertificateHash = MessageDigest.getInstance("SHA256").digest(encodedCertificate);

            SigningCertificateV2 signingCertificate = new SigningCertificateV2(new ESSCertIDv2(sha256CertificateHash));
            var signingCertificateAttribute = new Attribute(id_aa_signingCertificateV2,
                new DLSet(signingCertificate.toASN1Primitive()));

            AttributeTable signedAttributeTable = new AttributeTable(new Hashtable<>(Map.of(
                id_aa_signingCertificateV2, signingCertificateAttribute)));

            ResponseProperties responseProperties = ResponseProperties.builder()
                .status(GRANTED)
                .genTime(new Date())
                .hashAlgorithm(SHA256)
                .hash(repeat("b", 32).getBytes(UTF_8))
                .signingCertificate(certificate)
                .signedAttributes(signedAttributeTable)
                .build();

            TimeStampResponse timeStampResponseMock = generateTimeStampResponseMock(responseProperties);

            // when
            Optional<X509CertificateHolder> signingCertificateHolder = testSubject
                .extractSigningCertificate(timeStampResponseMock);

            // then
            X509CertificateHolder expectedCertificateHolder = new X509CertificateHolder(encodedCertificate);

            assertThat(signingCertificateHolder).contains(expectedCertificateHolder);
        }

        @Test
        void returnsSigningCertificateWithSpecifiedHashAlgorithm() throws Exception {
            // given
            X509Certificate certificate = loadRsaCertificate();
            byte[] encodedCertificate = certificate.getEncoded();
            byte[] sha1CertificateHash = MessageDigest.getInstance("SHA1").digest(encodedCertificate);

            SigningCertificateV2 signingCertificate = new SigningCertificateV2(
                new ESSCertIDv2(new AlgorithmIdentifier(id_SHA1), sha1CertificateHash));
            var signingCertificateAttribute = new Attribute(id_aa_signingCertificateV2,
                new DLSet(signingCertificate.toASN1Primitive()));

            AttributeTable signedAttributeTable = new AttributeTable(new Hashtable<>(Map.of(
                id_aa_signingCertificateV2, signingCertificateAttribute)));

            ResponseProperties responseProperties = ResponseProperties.builder()
                .status(GRANTED)
                .genTime(new Date())
                .hashAlgorithm(SHA256)
                .hash(repeat("b", 32).getBytes(UTF_8))
                .signingCertificate(certificate)
                .signedAttributes(signedAttributeTable)
                .build();

            TimeStampResponse timeStampResponseMock = generateTimeStampResponseMock(responseProperties);

            // when
            Optional<X509CertificateHolder> signingCertificateHolder = testSubject
                .extractSigningCertificate(timeStampResponseMock);

            // then
            X509CertificateHolder expectedCertificateHolder = new X509CertificateHolder(encodedCertificate);

            assertThat(signingCertificateHolder).contains(expectedCertificateHolder);
        }

    }

}
