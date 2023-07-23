package dev.mieser.tsa.signing.impl;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDSAContentVerifierProviderBuilder;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.bc.BcECContentVerifierProviderBuilder;
import org.bouncycastle.operator.bc.BcRSAContentVerifierProviderBuilder;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampResponse;

import dev.mieser.tsa.domain.TimeStampValidationResult;
import dev.mieser.tsa.signing.api.TimeStampValidator;
import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;
import dev.mieser.tsa.signing.api.exception.TsaNotInitializedException;
import dev.mieser.tsa.signing.impl.cert.PublicKeyAlgorithm;
import dev.mieser.tsa.signing.impl.cert.SigningCertificateExtractor;
import dev.mieser.tsa.signing.impl.cert.SigningCertificateHolder;
import dev.mieser.tsa.signing.impl.cert.SigningKeystoreLoader;
import dev.mieser.tsa.signing.impl.mapper.TimeStampValidationResultMapper;

@Slf4j
@RequiredArgsConstructor
public class BouncyCastleTimeStampValidator implements TimeStampValidator {

    private final TspParser tspParser;

    private final SigningKeystoreLoader signingKeystoreLoader;

    private final TimeStampValidationResultMapper timeStampValidationResultMapper;

    private final SigningCertificateExtractor signingCertificateExtractor;

    private SignerInformationVerifier signerInformationVerifier;

    @Override
    public void initialize() {
        if (signerInformationVerifier != null) {
            return;
        }

        this.signerInformationVerifier = new SignerInformationVerifier(new DefaultCMSSignatureAlgorithmNameGenerator(),
            new DefaultSignatureAlgorithmIdentifierFinder(),
            buildContentVerifierProvider(), new BcDigestCalculatorProvider());
    }

    @Override
    public TimeStampValidationResult validateResponse(InputStream tspResponseInputStream) throws InvalidTspResponseException {
        verifyInitialized();

        TimeStampResponse timeStampResponse = tspParser.parseResponse(tspResponseInputStream);

        SigningCertificateHolder signingCertificate = signingCertificateExtractor.extractSigningCertificate(timeStampResponse)
            .orElse(null);

        return timeStampValidationResultMapper.map(timeStampResponse, signingCertificate,
            wasSignedByThisTsa(timeStampResponse));
    }

    /**
     * Verifies that the validator is initialized.
     *
     * @throws TsaNotInitializedException
     *     When the {@link #initialize()} method has not yet been executed.
     */
    private void verifyInitialized() {
        if (signerInformationVerifier == null) {
            throw new TsaNotInitializedException();
        }
    }

    private boolean containsTimeStampToken(TimeStampResponse timeStampResponse) {
        return timeStampResponse.getTimeStampToken() != null;
    }

    /**
     * @return The signature verifier backed by the {@link SigningKeystoreLoader current} certificate.
     */
    private ContentVerifierProvider buildContentVerifierProvider() {
        try {
            X509Certificate signingCertificate = signingKeystoreLoader.loadCertificate();
            String jcaAlgorithmName = signingCertificate.getPublicKey().getAlgorithm();
            PublicKeyAlgorithm publicKeyAlgorithm = PublicKeyAlgorithm.fromJcaName(jcaAlgorithmName)
                .orElseThrow(() -> new IllegalStateException(
                    String.format("Unsupported public key algorithm '%s'.", jcaAlgorithmName)));

            var signingCertificateHolder = new X509CertificateHolder(signingCertificate.getEncoded());

            return switch (publicKeyAlgorithm) {
            case RSA -> new BcRSAContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder())
                .build(signingCertificateHolder);
            case EC -> new BcECContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder())
                .build(signingCertificateHolder);
            case DSA -> new BcDSAContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder())
                .build(signingCertificateHolder);
            };

        } catch (IOException | CertificateEncodingException | OperatorCreationException e) {
            throw new IllegalStateException("Failed to initialize content verifier provider.", e);
        }
    }

    /**
     * @param timeStampResponse
     *     The response to check, not {@code null}.
     * @return {@code true}, iff the response contains a token which was signed by this TSA.
     */
    private boolean wasSignedByThisTsa(TimeStampResponse timeStampResponse) {
        if (!containsTimeStampToken(timeStampResponse)) {
            return false;
        }

        try {
            timeStampResponse.getTimeStampToken().validate(signerInformationVerifier);
            return true;
        } catch (TSPException e) {
            log.info("TSP Response was not signed by this TSA", e);
            return false;
        }
    }

}
