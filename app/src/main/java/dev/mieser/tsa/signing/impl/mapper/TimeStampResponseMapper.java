package dev.mieser.tsa.signing.impl.mapper;

import java.util.Date;

import lombok.RequiredArgsConstructor;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponse;

import dev.mieser.tsa.datetime.api.DateConverter;
import dev.mieser.tsa.domain.TimeStampRequestData;
import dev.mieser.tsa.domain.TimeStampResponseData;

/**
 * Maps Bouncy Castle-specific TSP request/response objects to domain objects.
 */
@RequiredArgsConstructor
public class TimeStampResponseMapper extends AbstractTspMapper {

    private final DateConverter dateConverter;

    /**
     * @param timeStampRequest
     *     The Bouncy Castle TSP request for which the response was generated, not {@code null}.
     * @param timeStampResponse
     *     The corresponding response, not {@code null}.
     * @param receptionTime
     *     The time the TSP request was received, not {@code null}.
     * @return The corresponding domain object.
     */
    public TimeStampResponseData map(TimeStampRequest timeStampRequest, TimeStampResponse timeStampResponse, Date receptionTime) {
        TimeStampRequestData requestData = TimeStampRequestData
            .builder(mapToHashAlgorithm(timeStampRequest.getMessageImprintAlgOID()), timeStampRequest.getMessageImprintDigest())
            .nonce(timeStampRequest.getNonce())
            .certificateRequested(timeStampRequest.getCertReq())
            .tsaPolicyId(mapIfNotNull(timeStampRequest.getReqPolicy(), ASN1ObjectIdentifier::getId))
            .asnEncoded(asnEncoded(timeStampRequest, TimeStampRequest::getEncoded))
            .build();

        return TimeStampResponseData.builder(
            mapToResponseStatus(timeStampResponse.getStatus()),
            dateConverter.toZonedDateTime(receptionTime),
            requestData,
            asnEncoded(timeStampResponse, TimeStampResponse::getEncoded))
            .statusString(timeStampResponse.getStatusString())
            .failureInfo(mapIfNotNull(timeStampResponse.getFailInfo(), failInfo -> mapToFailureInfo(failInfo.intValue())))
            .serialNumber(
                mapIfNotNull(timeStampResponse.getTimeStampToken(),
                    timeStampToken -> timeStampToken.getTimeStampInfo().getSerialNumber()))
            .generationTime(
                mapIfNotNull(timeStampResponse.getTimeStampToken(),
                    token -> dateConverter.toZonedDateTime(token.getTimeStampInfo().getGenTime())))
            .build();
    }

}
