package dev.mieser.tsa.signing.mapper;

import dev.mieser.tsa.datetime.api.DateConverter;
import dev.mieser.tsa.domain.SigningCertificateInformation;
import dev.mieser.tsa.domain.TimestampValidationResult;
import dev.mieser.tsa.signing.cert.SigningCertificateExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampTokenInfo;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class TimestampVerificationResultMapper extends AbstractTspMapper {

    private final DateConverter dateConverter;

    private final SigningCertificateExtractor signingCertificateExtractor;

    public TimestampValidationResult map(TimeStampResponse timeStampResponse, boolean signedByThisTsa) {
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
                .signingCertificateInformation(mapCertificateInformation(timeStampResponse))
                .signedByThisTsa(signedByThisTsa)
                .build();
    }

    // TODO: better usage of optional
    private SigningCertificateInformation mapCertificateInformation(TimeStampResponse timeStampResponse) {
        Optional<X509CertificateHolder> signingCertificate = signingCertificateExtractor.extractSigningCertificate(timeStampResponse);
        if (signingCertificate.isEmpty()) {
            return null;
        }

        return SigningCertificateInformation.builder()
                .serialNumber(signingCertificate.get().getSerialNumber())
                .expirationDate(dateConverter.toZonedDateTime(signingCertificate.get().getNotAfter()))
                .issuer(signingCertificate.get().getIssuer().toString())
                .build();
    }

}
