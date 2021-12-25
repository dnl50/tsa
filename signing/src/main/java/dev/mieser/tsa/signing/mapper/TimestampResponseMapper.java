package dev.mieser.tsa.signing.mapper;

import dev.mieser.tsa.datetime.api.DateConverter;
import dev.mieser.tsa.domain.TimestampRequestData;
import dev.mieser.tsa.domain.TimestampResponseData;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponse;

import java.util.Date;

/**
 * Maps Bouncy Castle-specific TSP request/response objects to domain objects.
 */
@RequiredArgsConstructor
public class TimestampResponseMapper extends AbstractTspMapper {

    private final DateConverter dateConverter;

    /**
     * @param timeStampRequest  The Bouncy Castle TSP request for which the response was generated, not {@code null}.
     * @param timeStampResponse The corresponding response, not {@code null}.
     * @param receptionTime     The time the TSP request was received, not {@code null}.
     * @return The corresponding domain object.
     */
    public TimestampResponseData map(TimeStampRequest timeStampRequest, TimeStampResponse timeStampResponse, Date receptionTime) {
        TimestampRequestData requestData = TimestampRequestData.builder()
                .hashAlgorithm(mapToHashAlgorithm(timeStampRequest.getMessageImprintAlgOID()))
                .hash(timeStampRequest.getMessageImprintDigest())
                .nonce(timeStampRequest.getNonce())
                .certificateRequested(timeStampRequest.getCertReq())
                .tsaPolicyId(mapIfNotNull(timeStampRequest.getReqPolicy(), ASN1ObjectIdentifier::getId))
                .asnEncoded(asnEncoded(timeStampRequest, TimeStampRequest::getEncoded))
                .build();

        return TimestampResponseData.builder()
                .status(mapToResponseStatus(timeStampResponse.getStatus()))
                .statusString(timeStampResponse.getStatusString())
                .failureInfo(mapIfNotNull(timeStampResponse.getFailInfo(), PKIFailureInfo::intValue))
                .serialNumber(
                        mapIfNotNull(timeStampResponse.getTimeStampToken(), timeStampToken -> timeStampToken.getTimeStampInfo().getSerialNumber().longValue()))
                .request(requestData)
                .receptionTime(dateConverter.toZonedDateTime(receptionTime))
                .generationTime(
                        mapIfNotNull(timeStampResponse.getTimeStampToken(), token -> dateConverter.toZonedDateTime(token.getTimeStampInfo().getGenTime())))
                .asnEncoded(asnEncoded(timeStampResponse, TimeStampResponse::getEncoded))
                .build();
    }

}
