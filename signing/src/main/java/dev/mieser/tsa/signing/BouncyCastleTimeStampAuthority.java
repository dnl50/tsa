package dev.mieser.tsa.signing;

import dev.mieser.tsa.datetime.api.CurrentDateTimeService;
import dev.mieser.tsa.domain.HashAlgorithm;
import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.signing.api.TimeStampAuthority;
import dev.mieser.tsa.signing.api.exception.TsaInitializationException;
import dev.mieser.tsa.signing.api.exception.TsaNotInitializedException;
import dev.mieser.tsa.signing.api.exception.TspResponseException;
import dev.mieser.tsa.signing.api.exception.UnknownHashAlgorithmException;
import dev.mieser.tsa.signing.cert.SigningCertificateLoader;
import dev.mieser.tsa.signing.config.properties.TsaProperties;
import dev.mieser.tsa.signing.mapper.TimestampResponseMapper;
import dev.mieser.tsa.signing.serial.SerialNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.tsp.*;

import java.io.InputStream;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
public class BouncyCastleTimeStampAuthority implements TimeStampAuthority {

    private static final Set<String> SUPPORTED_PUBLIC_KEY_ALGORITHMS = Set.of("RSA", "ECDSA");

    private final TsaProperties tsaProperties;

    private final TspRequestParser tspRequestParser;

    private final TspRequestValidator tspRequestValidator;

    private final SigningCertificateLoader signingCertificateLoader;

    private final CurrentDateTimeService currentDateTimeService;

    private final SerialNumberGenerator serialNumberGenerator;

    private final TimestampResponseMapper timestampResponseMapper;

    private TimeStampResponseGenerator timeStampResponseGenerator;

    @Override
    public TimestampResponseData signRequest(InputStream tspRequest) {
        verifyTsaIsInitialized();

        TimeStampRequest timeStampRequest = tspRequestParser.parseRequest(tspRequest);
        if (!tspRequestValidator.isKnownHashAlgorithm(timeStampRequest)) {
            throw new UnknownHashAlgorithmException(format("Unknown hash algorithm OID '%s'.", timeStampRequest.getMessageImprintAlgOID().getId()));
        }

        return generateTspResponse(timeStampRequest);
    }

    /**
     * Verifies that the TSA has been initialzed via the {@link #initialize()} method.
     *
     * @throws TsaNotInitializedException When the TSA has not yet been initialized.
     */
    private void verifyTsaIsInitialized() {
        if (timeStampResponseGenerator == null) {
            throw new TsaNotInitializedException();
        }
    }

    private TimestampResponseData generateTspResponse(TimeStampRequest timeStampRequest) {
        try {
            BigInteger tspResponseSerial = serialNumberGenerator.generateSerialNumber();
            TimeStampResponse tspResponse = timeStampResponseGenerator.generate(timeStampRequest, tspResponseSerial, currentDateTimeService.now());
            log.info("Successfully signed TSP request. TSP request serial number: {}", tspResponseSerial);
            return timestampResponseMapper.map(timeStampRequest, tspResponse);
        } catch (TSPException tspException) {
            throw new TspResponseException("Could not sign TSP request.", tspException);
        }
    }

    @Override
    public void initialize() {
        try {
            DigestCalculator signerCertDigestCalculator = buildSignerCertDigestCalculator();
            SignerInfoGenerator signerInfoGenerator = buildSignerInfoGenerator();
            var timeStampTokenGenerator = new TimeStampTokenGenerator(signerInfoGenerator, signerCertDigestCalculator, new ASN1ObjectIdentifier(tsaProperties.getPolicyOid()));

            this.timeStampResponseGenerator = new TimeStampResponseGenerator(timeStampTokenGenerator, acceptedHashAlgorithmIdentifiers());
        } catch (Exception e) {
            throw new TsaInitializationException("Could not initialize TSA.", e);
        }
    }

    /**
     * @return The {@link DigestCalculator} which is used to calculate the {@code ESSCertID} which is included TSA responses.
     * @throws Exception When an error occurs building the digest calculator.
     */
    private DigestCalculator buildSignerCertDigestCalculator() throws Exception {
        String hashAlgorithmOid = tsaProperties.getEssCertIdAlgorithm().getObjectIdentifier();
        var hashAlgorithmIdentifier = new AlgorithmIdentifier(new ASN1ObjectIdentifier(hashAlgorithmOid));

        return new JcaDigestCalculatorProviderBuilder().build()
                .get(hashAlgorithmIdentifier);
    }

    /**
     * @return The {@link SignerInfoGenerator} which is used to sign the TSP request.
     * @throws Exception When an error occurs while building the signer info generator.
     */
    private SignerInfoGenerator buildSignerInfoGenerator() throws Exception {
        X509Certificate signingCertificate = signingCertificateLoader.loadCertificate();
        PublicKey signingCertificatePublicKey = signingCertificate.getPublicKey();
        if (!hasSupportedPublicKeyAlgorithm(signingCertificatePublicKey)) {
            throw new IllegalStateException(format("Public Key algorithm '%s' is not supported.", signingCertificatePublicKey.getAlgorithm()));
        }

        PrivateKey signingPrivateKey = signingCertificateLoader.loadPrivateKey();
        String signingAlgorithmName = bouncyCastleSigningAlgorithmName(signingCertificatePublicKey);
        log.info("Using public key algorithm '{}' and signing algorithm '{}'", signingCertificatePublicKey.getAlgorithm(), signingCertificate);

        return new JcaSimpleSignerInfoGeneratorBuilder().build(signingAlgorithmName, signingPrivateKey, signingCertificate);
    }

    /**
     * @param signingCertificatePublicKey The public key of the certificate used to sign the TSP requests with, not {@code null}.
     * @return {@code true}, iff the public key's algorithm name is contained in {@link #SUPPORTED_PUBLIC_KEY_ALGORITHMS} set.
     */
    private boolean hasSupportedPublicKeyAlgorithm(PublicKey signingCertificatePublicKey) {
        String publicKeyAlgorithmName = signingCertificatePublicKey.getAlgorithm();
        return SUPPORTED_PUBLIC_KEY_ALGORITHMS.contains(publicKeyAlgorithmName);
    }

    /**
     * @param signingCertificatePublicKey The {@link #hasSupportedPublicKeyAlgorithm(PublicKey) supported} public key, not {@code null}.
     * @return The name of the Bouncycastle signature algorithm.
     */
    private String bouncyCastleSigningAlgorithmName(PublicKey signingCertificatePublicKey) {
        return format("%swith%s", tsaProperties.getSigningDigestAlgorithm().name(), signingCertificatePublicKey.getAlgorithm());
    }

    /**
     * @return The OIDs of the accepted hash algorithms.
     */
    private Set<String> acceptedHashAlgorithmIdentifiers() {
        return tsaProperties.getAcceptedHashAlgorithms().stream()
                .map(HashAlgorithm::getObjectIdentifier)
                .collect(Collectors.toSet());
    }

}
