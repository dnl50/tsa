package dev.mieser.tsa.signing.mapper;

import dev.mieser.tsa.datetime.api.DateConverter;
import dev.mieser.tsa.domain.SigningCertificateInformation;
import dev.mieser.tsa.domain.TimestampValidationResult;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampTokenInfo;

/**
 * Maps Bouncy Castle-specific TSP response objects to domain objects.
 */
@RequiredArgsConstructor
public class TimestampVerificationResultMapper extends AbstractTspMapper {

    private final DateConverter dateConverter;

    /**
     * @param timeStampResponse  The time stamp response to map, not {@code null}.
     * @param signingCertificate The signing certificate included in the time stamp response, can be null.
     * @param signedByThisTsa    A flag whether the time stamp response was signed by the certificate currently in use by this TSA.
     * @return The mapped result.
     */
    public TimestampValidationResult map(TimeStampResponse timeStampResponse, X509CertificateHolder signingCertificate, boolean signedByThisTsa) {
        TimeStampTokenInfo timeStampInfo = timeStampResponse.getTimeStampToken() != null ? timeStampResponse.getTimeStampToken().getTimeStampInfo() : null;

        return TimestampValidationResult.builder()
                .status(mapToResponseStatus(timeStampResponse.getStatus()))
                .statusString(timeStampResponse.getStatusString())
                .failureInfo(mapIfNotNull(timeStampResponse.getFailInfo(), failInfo -> mapToFailureInfo(failInfo.intValue())))
                .generationTime(mapIfNotNull(timeStampInfo, info -> dateConverter.toZonedDateTime(info.getGenTime())))
                .serialNumber(mapIfNotNull(timeStampInfo, TimeStampTokenInfo::getSerialNumber))
                .nonce(mapIfNotNull(timeStampInfo, TimeStampTokenInfo::getNonce))
                .hashAlgorithm(mapIfNotNull(timeStampInfo, info -> mapToHashAlgorithm(info.getMessageImprintAlgOID())))
                .hash(mapIfNotNull(timeStampInfo, TimeStampTokenInfo::getMessageImprintDigest))
                .signingCertificateInformation(mapIfNotNull(signingCertificate, this::mapCertificateInformation))
                .signedByThisTsa(signedByThisTsa)
                .build();
    }

    private SigningCertificateInformation mapCertificateInformation(X509CertificateHolder signingCertificate) {
        return SigningCertificateInformation.builder()
                .serialNumber(signingCertificate.getSerialNumber())
                .expirationDate(dateConverter.toZonedDateTime(signingCertificate.getNotAfter()))
                .issuer(signingCertificate.getIssuer().toString())
                .build();
    }

}
