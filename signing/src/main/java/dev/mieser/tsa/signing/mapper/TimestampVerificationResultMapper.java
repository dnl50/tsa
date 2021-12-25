package dev.mieser.tsa.signing.mapper;

import dev.mieser.tsa.datetime.api.DateConverter;
import dev.mieser.tsa.domain.SigningCertificateInformation;
import dev.mieser.tsa.domain.TimestampValidationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampTokenInfo;

@Slf4j
@RequiredArgsConstructor
public class TimestampVerificationResultMapper extends AbstractTspMapper {

    private final DateConverter dateConverter;

    public TimestampValidationResult map(TimeStampResponse timeStampResponse, X509CertificateHolder signingCertificate, boolean signedByThisTsa) {
        TimeStampTokenInfo timeStampInfo = timeStampResponse.getTimeStampToken().getTimeStampInfo();

        return TimestampValidationResult.builder()
                .status(mapToResponseStatus(timeStampResponse.getStatus()))
                .statusString(timeStampResponse.getStatusString())
                .failureInfo(mapIfNotNull(timeStampResponse.getFailInfo(), PKIFailureInfo::intValue))
                .generationTime(dateConverter.toZonedDateTime(timeStampInfo.getGenTime()))
                .serialNumber(timeStampInfo.getSerialNumber())
                .nonce(timeStampInfo.getNonce())
                .hashAlgorithm(mapToHashAlgorithm(timeStampInfo.getMessageImprintAlgOID()))
                .hash(timeStampInfo.getMessageImprintDigest())
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
