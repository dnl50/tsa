package dev.mieser.tsa.signing.mapper;

import dev.mieser.tsa.datetime.api.DateConverter;
import dev.mieser.tsa.domain.SigningCertificateInformation;
import dev.mieser.tsa.domain.TimestampValidationResult;
import dev.mieser.tsa.signing.cert.TimeStampCertificateSelector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.tsp.TimeStampTokenInfo;
import org.bouncycastle.util.Store;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
public class TimestampVerificationResultMapper extends AbstractTspMapper {

    private final DateConverter dateConverter;

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
                .certificateInformation(mapCertificateInformation(timeStampResponse))
                .signedByThisTsa(signedByThisTsa)
                .build();
    }

    private SigningCertificateInformation mapCertificateInformation(TimeStampResponse timeStampResponse) {
        Store<X509CertificateHolder> certificates = timeStampResponse.getTimeStampToken().getCertificates();
        Collection<X509CertificateHolder> matches = certificates.getMatches(new TimeStampCertificateSelector());

        if (matches.isEmpty()) {
            log.info("No certificate with timeStamping purpose present.");
            return null;
        } else if (matches.size() > 1) {
            log.info("Multiple certificates with timeStamping purpose present. The first certificate will be used.");
        }

        X509CertificateHolder timeStampingCertificate = matches.iterator().next();

        return SigningCertificateInformation.builder()
                .serialNumber(timeStampingCertificate.getSerialNumber())
                .expirationDate(dateConverter.toZonedDateTime(timeStampingCertificate.getNotAfter()))
                .issuer(timeStampingCertificate.getIssuer().toString())
                .build();
    }

}
