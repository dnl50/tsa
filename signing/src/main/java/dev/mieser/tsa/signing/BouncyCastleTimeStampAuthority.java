package dev.mieser.tsa.signing;

import static dev.mieser.tsa.signing.cert.PublicKeyAlgorithm.*;
import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.*;
import org.bouncycastle.util.CollectionStore;
import org.bouncycastle.util.Store;

import dev.mieser.tsa.datetime.api.CurrentDateTimeService;
import dev.mieser.tsa.domain.HashAlgorithm;
import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.signing.api.TimeStampAuthority;
import dev.mieser.tsa.signing.api.exception.TsaInitializationException;
import dev.mieser.tsa.signing.api.exception.TsaNotInitializedException;
import dev.mieser.tsa.signing.api.exception.TspResponseException;
import dev.mieser.tsa.signing.api.exception.UnknownHashAlgorithmException;
import dev.mieser.tsa.signing.cert.PublicKeyAlgorithm;
import dev.mieser.tsa.signing.cert.PublicKeyAnalyzer;
import dev.mieser.tsa.signing.cert.SigningCertificateLoader;
import dev.mieser.tsa.signing.config.TsaProperties;
import dev.mieser.tsa.signing.mapper.TimestampResponseMapper;
import dev.mieser.tsa.signing.serial.SerialNumberGenerator;

/**
 * {@link TimeStampAuthority} implementation using Bouncy Castle's TSP implementation.
 */
@Slf4j
@RequiredArgsConstructor
public class BouncyCastleTimeStampAuthority implements TimeStampAuthority {

    private static final Set<PublicKeyAlgorithm> SUPPORTED_PUBLIC_KEY_ALGORITHMS = EnumSet.of(DSA, RSA, EC);

    private final TsaProperties tsaProperties;

    private final TspParser tspParser;

    private final TspValidator tspValidator;

    private final SigningCertificateLoader signingCertificateLoader;

    private final CurrentDateTimeService currentDateTimeService;

    private final SerialNumberGenerator serialNumberGenerator;

    private final TimestampResponseMapper timestampResponseMapper;

    private final PublicKeyAnalyzer publicKeyAnalyzer;

    private TimeStampResponseGenerator timeStampResponseGenerator;

    @Override
    public TimestampResponseData signRequest(InputStream tspRequestInputStream) {
        verifyTsaIsInitialized();

        TimeStampRequest timeStampRequest = tspParser.parseRequest(tspRequestInputStream);
        if (!tspValidator.isKnownHashAlgorithm(timeStampRequest.getMessageImprintAlgOID())) {
            throw new UnknownHashAlgorithmException(
                format("Unknown hash algorithm OID '%s'.", timeStampRequest.getMessageImprintAlgOID().getId()));
        }

        return generateTspResponse(timeStampRequest);
    }

    /**
     * Verifies that the TSA has been initialized via the {@link #initialize()} method.
     *
     * @throws TsaNotInitializedException
     *     When the TSA has not yet been initialized.
     */
    private void verifyTsaIsInitialized() {
        if (timeStampResponseGenerator == null) {
            throw new TsaNotInitializedException();
        }
    }

    private TimestampResponseData generateTspResponse(TimeStampRequest timeStampRequest) {
        try {
            BigInteger tspResponseSerial = BigInteger.valueOf(serialNumberGenerator.generateSerialNumber());
            Date receptionTime = currentDateTimeService.now();
            TimeStampResponse tspResponse = timeStampResponseGenerator.generate(timeStampRequest, tspResponseSerial,
                receptionTime);
            log.info("Successfully signed TSP request. TSP request serial number: {}", tspResponseSerial);
            return timestampResponseMapper.map(timeStampRequest, tspResponse, receptionTime);
        } catch (TSPException tspException) {
            throw new TspResponseException("Could not sign TSP request.", tspException);
        }
    }

    @Override
    public void initialize() {
        try {
            log.info("Starting TSA initialization.");

            DigestCalculator signerCertDigestCalculator = buildSignerCertDigestCalculator();
            SignerInfoGenerator signerInfoGenerator = buildSignerInfoGenerator();
            var timeStampTokenGenerator = new TimeStampTokenGenerator(signerInfoGenerator, signerCertDigestCalculator,
                new ASN1ObjectIdentifier(tsaProperties.getPolicyOid()));
            timeStampTokenGenerator.addCertificates(tokenGeneratorCertificateStore());

            this.timeStampResponseGenerator = new TimeStampResponseGenerator(timeStampTokenGenerator,
                acceptedHashAlgorithmIdentifiers());

            log.info(
                "Successfully initialized TSA. Tokens are issued under policy OID '{}'. The following hash algorithms are accepted: {}",
                tsaProperties.getPolicyOid(), tsaProperties.getAcceptedHashAlgorithms());
        } catch (Exception e) {
            throw new TsaInitializationException("Could not initialize TSA.", e);
        }
    }

    /**
     * @return The {@link DigestCalculator} which is used to calculate the {@code ESSCertID} which is included in TSA
     * responses.
     * @throws Exception
     *     When an error occurs building the digest calculator.
     */
    private DigestCalculator buildSignerCertDigestCalculator() throws Exception {
        String hashAlgorithmOid = tsaProperties.getEssCertIdAlgorithm().getObjectIdentifier();
        var hashAlgorithmIdentifier = new AlgorithmIdentifier(new ASN1ObjectIdentifier(hashAlgorithmOid));

        return new JcaDigestCalculatorProviderBuilder().build()
            .get(hashAlgorithmIdentifier);
    }

    /**
     * @return The {@link SignerInfoGenerator} which is used to sign the TSP request.
     * @throws Exception
     *     When an error occurs while building the signer info generator.
     */
    private SignerInfoGenerator buildSignerInfoGenerator() throws Exception {
        X509Certificate signingCertificate = signingCertificateLoader.loadCertificate();
        PublicKeyAlgorithm publicKeyAlgorithm = publicKeyAnalyzer.publicKeyAlgorithm(signingCertificate);
        if (!isSupportedPublicKeyAlgorithm(publicKeyAlgorithm)) {
            throw new TsaInitializationException(
                format("Public Key algorithm '%s' is not supported.", publicKeyAlgorithm.getJcaName()));
        }

        PrivateKey signingPrivateKey = signingCertificateLoader.loadPrivateKey();
        String signingAlgorithmName = bouncyCastleSignatureAlgorithmName(publicKeyAlgorithm);
        log.info("Public key algorithm is '{}', using signing algorithm '{}'.", publicKeyAlgorithm.getJcaName(),
            signingAlgorithmName);

        return new JcaSimpleSignerInfoGeneratorBuilder().build(signingAlgorithmName, signingPrivateKey, signingCertificate);
    }

    /**
     * @param publicKeyAlgorithm
     *     The algorithm of the public key whose corresponding private key is used to sign the TSP requests with, not
     *     {@code null}.
     * @return {@code true}, iff the public key's algorithm is supported by this TSA.
     */
    private boolean isSupportedPublicKeyAlgorithm(PublicKeyAlgorithm publicKeyAlgorithm) {
        return SUPPORTED_PUBLIC_KEY_ALGORITHMS.contains(publicKeyAlgorithm);
    }

    /**
     * @param publicKeyAlgorithm
     *     The algorithm of the public key whose corresponding private key is used to sign the TSP requests with, not
     *     {@code null}.
     * @return The name of the Bouncy Castle signature algorithm used to sign TSP requests.
     */
    private String bouncyCastleSignatureAlgorithmName(PublicKeyAlgorithm publicKeyAlgorithm) {
        String signatureAlgorithmSuffix = switch (publicKeyAlgorithm) {
        case EC -> "ECDSA";
        case RSA, DSA -> publicKeyAlgorithm.getJcaName();
        };

        return format("%swith%s", tsaProperties.getSigningDigestAlgorithm().name(), signatureAlgorithmSuffix);
    }

    /**
     * @return The OIDs of the accepted hash algorithms.
     */
    private Set<String> acceptedHashAlgorithmIdentifiers() {
        return tsaProperties.getAcceptedHashAlgorithms().stream()
            .map(HashAlgorithm::getObjectIdentifier)
            .collect(Collectors.toSet());
    }

    /**
     * @return A store of {@link X509CertificateHolder X.509 Certificates} which will be included when the {@code certReq}
     * Flag is set.
     */
    private Store<X509CertificateHolder> tokenGeneratorCertificateStore() throws IOException, CertificateEncodingException {
        X509CertificateHolder signingCertificate = new X509CertificateHolder(
            signingCertificateLoader.loadCertificate().getEncoded());
        return new CollectionStore<>(List.of(signingCertificate));
    }

}
