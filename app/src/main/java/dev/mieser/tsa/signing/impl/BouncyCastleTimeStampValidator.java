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
import dev.mieser.tsa.signing.api.exception.InvalidCertificateException;
import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;
import dev.mieser.tsa.signing.api.exception.TsaInitializationException;
import dev.mieser.tsa.signing.api.exception.TsaNotInitializedException;
import dev.mieser.tsa.signing.impl.cert.*;
import dev.mieser.tsa.signing.impl.mapper.TimeStampValidationResultMapper;

@Slf4j
@RequiredArgsConstructor
public class BouncyCastleTimeStampValidator implements TimeStampValidator {

    private final TspParser tspParser;

    private final SigningKeystoreLoader signingKeystoreLoader;

    private final TimeStampValidationResultMapper timeStampValidationResultMapper;

    private final SigningCertificateExtractor signingCertificateExtractor;

    private final CertificateParser certificateParser;

    private SignerInformationVerifier defaultSignatureVerifier;

    @Override
    public void initialize() {
        if (defaultSignatureVerifier != null) {
            return;
        }

        try {
            defaultSignatureVerifier = buildSignerInformationVerifier(signingKeystoreLoader.loadCertificate());
        } catch (InvalidCertificateException e) {
            throw new TsaInitializationException("Failed to initialize signature verifier.", e);
        }
    }

    @Override
    public TimeStampValidationResult validateResponse(InputStream tspResponse) throws InvalidTspResponseException {
        verifyInitialized();

        return validateResponse(tspResponse, defaultSignatureVerifier);
    }

    @Override
    public TimeStampValidationResult validateResponse(InputStream tspResponse,
        InputStream x509Certificate) throws InvalidTspResponseException, InvalidCertificateException {
        return validateResponse(tspResponse, buildSignerInformationVerifier(certificateParser.parseCertificate(x509Certificate)));
    }

    private void verifyInitialized() {
        if (defaultSignatureVerifier == null) {
            throw new TsaNotInitializedException();
        }
    }

    private TimeStampValidationResult validateResponse(InputStream tspResponse,
        SignerInformationVerifier signatureVerifier) throws InvalidTspResponseException {
        TimeStampResponse timeStampResponse = tspParser.parseResponse(tspResponse);

        SigningCertificateHolder signingCertificate = signingCertificateExtractor.extractSigningCertificate(timeStampResponse)
            .orElse(null);

        return timeStampValidationResultMapper.map(timeStampResponse, signingCertificate,
            isSignatureValid(timeStampResponse, signatureVerifier));
    }

    private boolean isSignatureValid(TimeStampResponse timeStampResponse,
        SignerInformationVerifier signatureVerifier) {
        if (timeStampResponse.getTimeStampToken() == null) {
            return false;
        }

        try {
            timeStampResponse.getTimeStampToken().validate(signatureVerifier);
            return true;
        } catch (TSPException e) {
            log.debug("Failed to validate signature.", e);
            return false;
        }
    }

    private SignerInformationVerifier buildSignerInformationVerifier(
        X509Certificate signerCertificate) throws InvalidCertificateException {
        try {
            String jcaAlgorithmName = signerCertificate.getPublicKey().getAlgorithm();
            PublicKeyAlgorithm publicKeyAlgorithm = PublicKeyAlgorithm.fromJcaName(jcaAlgorithmName)
                .orElseThrow(() -> new InvalidCertificateException(
                    String.format("Unsupported public key algorithm '%s'.", jcaAlgorithmName)));

            var signingCertificateHolder = new X509CertificateHolder(signerCertificate.getEncoded());

            ContentVerifierProvider verifierProvider = switch (publicKeyAlgorithm) {
            case RSA -> new BcRSAContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder())
                .build(signingCertificateHolder);
            case EC -> new BcECContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder())
                .build(signingCertificateHolder);
            case DSA -> new BcDSAContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder())
                .build(signingCertificateHolder);
            };

            return new SignerInformationVerifier(new DefaultCMSSignatureAlgorithmNameGenerator(),
                new DefaultSignatureAlgorithmIdentifierFinder(), verifierProvider, new BcDigestCalculatorProvider());
        } catch (IOException | CertificateEncodingException | OperatorCreationException e) {
            throw new IllegalStateException("Failed to initialize content verifier provider.", e);
        }
    }

}
