package dev.mieser.tsa.signing;

import dev.mieser.tsa.domain.TimestampValidationResult;
import dev.mieser.tsa.signing.api.TimeStampValidator;
import dev.mieser.tsa.signing.api.exception.TsaInitializationException;
import dev.mieser.tsa.signing.api.exception.TsaNotInitializedException;
import dev.mieser.tsa.signing.api.exception.UnknownHashAlgorithmException;
import dev.mieser.tsa.signing.cert.PublicKeyAlgorithm;
import dev.mieser.tsa.signing.cert.PublicKeyAnalyzer;
import dev.mieser.tsa.signing.cert.SigningCertificateLoader;
import dev.mieser.tsa.signing.mapper.TimestampVerificationResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.DefaultCMSSignatureAlgorithmNameGenerator;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.operator.ContentVerifierProvider;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DefaultSignatureAlgorithmIdentifierFinder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.operator.bc.BcECContentVerifierProviderBuilder;
import org.bouncycastle.operator.bc.BcRSAContentVerifierProviderBuilder;
import org.bouncycastle.tsp.TSPException;
import org.bouncycastle.tsp.TimeStampResponse;

import java.io.IOException;
import java.io.InputStream;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import static java.lang.String.format;

@Slf4j
@RequiredArgsConstructor
public class BouncyCastleTimeStampValidator implements TimeStampValidator {

    private final TspParser tspParser;

    private final SigningCertificateLoader signingCertificateLoader;

    private final PublicKeyAnalyzer publicKeyAnalyzer;

    private final TimestampVerificationResultMapper timestampVerificationResultMapper;

    private final TspValidator tspValidator;

    private SignerInformationVerifier signerInformationVerifier;

    @Override
    public void initialize() {
        this.signerInformationVerifier =
                new SignerInformationVerifier(new DefaultCMSSignatureAlgorithmNameGenerator(), new DefaultSignatureAlgorithmIdentifierFinder(),
                        buildContentVerifierProvider(), new BcDigestCalculatorProvider());
    }

    @Override
    public TimestampValidationResult validateResponse(InputStream tspResponseInputStream) {
        verifyInitialized();

        TimeStampResponse timeStampResponse = tspParser.parseResponse(tspResponseInputStream);
        ASN1ObjectIdentifier hashAlgorithmOid = timeStampResponse.getTimeStampToken().getTimeStampInfo().getMessageImprintAlgOID();
        if (!tspValidator.isKnownHashAlgorithm(hashAlgorithmOid)) {
            throw new UnknownHashAlgorithmException(format("Unknown hash algorithm OID '%s'.", hashAlgorithmOid.getId()));
        }

        return timestampVerificationResultMapper.map(timeStampResponse, wasSignedByThisTsa(timeStampResponse));
    }

    /**
     * Verifies that the validator is initialized.
     *
     * @throws TsaNotInitializedException When the {@link #initialize()} method has not yet been executed.
     */
    private void verifyInitialized() {
        if (signerInformationVerifier == null) {
            throw new TsaNotInitializedException();
        }
    }

    private ContentVerifierProvider buildContentVerifierProvider() {
        try {
            X509Certificate signingCertificate = signingCertificateLoader.loadCertificate();
            PublicKeyAlgorithm publicKeyAlgorithm = publicKeyAnalyzer.publicKeyAlgorithm(signingCertificate);
            var signingCertificateHolder = new X509CertificateHolder(signingCertificate.getEncoded());

            return switch (publicKeyAlgorithm) {
                case RSA -> new BcRSAContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder())
                        .build(signingCertificateHolder);
                case EC -> new BcECContentVerifierProviderBuilder(new DefaultDigestAlgorithmIdentifierFinder())
                        .build(signingCertificateHolder);
                default -> throw new TsaInitializationException(format("Public key algorithm '%s' is not supported.", publicKeyAlgorithm.getJcaName()));
            };

        } catch (IOException | CertificateEncodingException | OperatorCreationException e) {
            throw new TsaInitializationException("Failed to initialize content verifier provider.", e);
        }
    }

    private boolean wasSignedByThisTsa(TimeStampResponse timeStampResponse) {
        try {
            timeStampResponse.getTimeStampToken().validate(signerInformationVerifier);
            return true;
        } catch (TSPException e) {
            log.info("TSP Response was not signed by this TSA", e);
            return false;
        }
    }

}
