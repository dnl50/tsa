package dev.mieser.tsa.signing.impl.mapper;

import java.io.IOException;
import java.util.Base64;

import lombok.RequiredArgsConstructor;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampTokenInfo;

import dev.mieser.tsa.datetime.api.DateConverter;
import dev.mieser.tsa.domain.SigningCertificateIdentifier;
import dev.mieser.tsa.domain.SigningCertificateInformation;
import dev.mieser.tsa.domain.TimeStampValidationResult;
import dev.mieser.tsa.signing.impl.cert.SigningCertificateHolder;

/**
 * Maps Bouncy Castle-specific TSP response objects to domain objects.
 */
@RequiredArgsConstructor
public class TimeStampValidationResultMapper extends AbstractTspMapper {

    private final DateConverter dateConverter;

    /**
     * @param timeStampResponse
     *     The time stamp response to map, not {@code null}.
     * @param signingCertificateHolder
     *     The signing certificate referenced in the time stamp response, can be null.
     * @param signatureValid
     *     A flag whether the time stamp response was signed by the certificate currently in use by this TSA.
     * @return The mapped result.
     */
    public TimeStampValidationResult map(TimeStampResponse timeStampResponse, SigningCertificateHolder signingCertificateHolder,
        boolean signatureValid) {
        TimeStampTokenInfo timeStampInfo = timeStampResponse.getTimeStampToken() != null
            ? timeStampResponse.getTimeStampToken().getTimeStampInfo()
            : null;

        return TimeStampValidationResult.builder()
            .status(mapToResponseStatus(timeStampResponse.getStatus()))
            .statusString(timeStampResponse.getStatusString())
            .failureInfo(mapIfNotNull(timeStampResponse.getFailInfo(), failInfo -> mapToFailureInfo(failInfo.intValue())))
            .generationTime(mapIfNotNull(timeStampInfo, info -> dateConverter.toZonedDateTime(info.getGenTime())))
            .serialNumber(mapIfNotNull(timeStampInfo, TimeStampTokenInfo::getSerialNumber))
            .nonce(mapIfNotNull(timeStampInfo, TimeStampTokenInfo::getNonce))
            .hashAlgorithmIdentifier(mapIfNotNull(timeStampInfo, info -> info.getMessageImprintAlgOID().getId()))
            .hash(mapIfNotNull(timeStampInfo, TimeStampTokenInfo::getMessageImprintDigest))
            .signingCertificateIdentifier(mapIfNotNull(signingCertificateHolder, this::mapCertificateIdentifier))
            .signingCertificateInformation(mapIfNotNull(signingCertificateHolder, this::mapCertificateInformation))
            .signatureValid(signatureValid)
            .build();
    }

    private SigningCertificateIdentifier mapCertificateIdentifier(SigningCertificateHolder signingCertificateHolder) {
        return SigningCertificateIdentifier.builder()
            .hashAlgorithmOid(signingCertificateHolder.getAlgorithmIdentifier().getAlgorithm().getId())
            .hash(signingCertificateHolder.getHash())
            .build();
    }

    private SigningCertificateInformation mapCertificateInformation(SigningCertificateHolder signingCertificateHolder) {
        X509CertificateHolder signingCertificate = signingCertificateHolder.getSigningCertificate();
        if (signingCertificate == null) {
            return null;
        }

        return SigningCertificateInformation.builder()
            .serialNumber(signingCertificate.getSerialNumber())
            .expirationDate(dateConverter.toZonedDateTime(signingCertificate.getNotAfter()))
            .issuer(signingCertificate.getIssuer().toString())
            .base64EncodedCertificate(encodeAsBase64String(signingCertificate))
            .build();
    }

    private String encodeAsBase64String(X509CertificateHolder signingCertificate) {
        try {
            return Base64.getEncoder().encodeToString(signingCertificate.getEncoded());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to retrieve encoded signing certificate.", e);
        }
    }

}
