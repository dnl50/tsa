package dev.mieser.tsa.signing.impl;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Date;
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

import dev.mieser.tsa.datetime.api.CurrentDateService;
import dev.mieser.tsa.domain.HashAlgorithm;
import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.signing.api.TimeStampAuthority;
import dev.mieser.tsa.signing.api.exception.*;
import dev.mieser.tsa.signing.config.TsaProperties;
import dev.mieser.tsa.signing.impl.cert.PublicKeyAlgorithm;
import dev.mieser.tsa.signing.impl.cert.SigningCertificateLoader;
import dev.mieser.tsa.signing.impl.mapper.TimeStampResponseMapper;
import dev.mieser.tsa.signing.impl.serial.SerialNumberGenerator;

/**
 * {@link TimeStampAuthority} implementation using Bouncy Castle's TSP implementation.
 */
@Slf4j
@RequiredArgsConstructor
public class BouncyCastleTimeStampAuthority implements TimeStampAuthority {

    private final TsaProperties tsaProperties;

    private final TspParser tspParser;

    private final TspValidator tspValidator;

    private final SigningCertificateLoader signingCertificateLoader;

    private final CurrentDateService currentDateService;

    private final SerialNumberGenerator serialNumberGenerator;

    private final TimeStampResponseMapper timeStampResponseMapper;

    private TimeStampResponseGenerator timeStampResponseGenerator;

    @Override
    public TimeStampResponseData signRequest(
        InputStream tspRequestInputStream) throws InvalidTspRequestException, UnknownHashAlgorithmException {
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

    private TimeStampResponseData generateTspResponse(TimeStampRequest timeStampRequest) {
        try {
            BigInteger tspResponseSerial = BigInteger.valueOf(serialNumberGenerator.generateSerialNumber());
            Date receptionTime = currentDateService.now();
            TimeStampResponse tspResponse = timeStampResponseGenerator.generate(timeStampRequest, tspResponseSerial,
                receptionTime);
            log.info("Successfully signed TSP request. TSP request serial number: {}", tspResponseSerial);
            return timeStampResponseMapper.map(timeStampRequest, tspResponse, receptionTime);
        } catch (TSPException tspException) {
            throw new TspResponseException("Could not sign TSP request.", tspException);
        }
    }

    @Override
    public void initialize() {
        try {
            log.info("Starting TSA initialization...");

            DigestCalculator signerCertDigestCalculator = buildSignerCertDigestCalculator();
            SignerInfoGenerator signerInfoGenerator = buildSignerInfoGenerator();
            var timeStampTokenGenerator = new TimeStampTokenGenerator(signerInfoGenerator, signerCertDigestCalculator,
                new ASN1ObjectIdentifier(tsaProperties.policyOid()));
            timeStampTokenGenerator.addCertificates(tokenGeneratorCertificateStore());

            this.timeStampResponseGenerator = new TimeStampResponseGenerator(timeStampTokenGenerator,
                acceptedHashAlgorithmIdentifiers());

            log.info(
                "Successfully initialized TSA. Tokens are issued under policy OID '{}'. The following hash algorithms are accepted: {}",
                tsaProperties.policyOid(), tsaProperties.acceptedHashAlgorithms());
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
        String hashAlgorithmOid = tsaProperties.essCertIdAlgorithm().getObjectIdentifier();
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
        String jcaAlgorithmName = signingCertificate.getPublicKey().getAlgorithm();
        PublicKeyAlgorithm publicKeyAlgorithm = PublicKeyAlgorithm.fromJcaName(jcaAlgorithmName)
            .orElseThrow(
                () -> new TsaInitializationException(format("Public Key algorithm '%s' is not supported.", jcaAlgorithmName)));

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
     * @return The name of the Bouncy Castle signature algorithm used to sign TSP requests.
     */
    private String bouncyCastleSignatureAlgorithmName(PublicKeyAlgorithm publicKeyAlgorithm) {
        String signatureAlgorithmSuffix = switch (publicKeyAlgorithm) {
        case EC -> "ECDSA";
        case RSA, DSA -> publicKeyAlgorithm.getJcaName();
        };

        return format("%swith%s", tsaProperties.signingDigestAlgorithm().name(), signatureAlgorithmSuffix);
    }

    /**
     * @return The OIDs of the accepted hash algorithms.
     */
    private Set<String> acceptedHashAlgorithmIdentifiers() {
        return tsaProperties.acceptedHashAlgorithms().stream()
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
