package dev.mieser.tsa.signing.mapper;

import dev.mieser.tsa.datetime.api.DateConverter;
import dev.mieser.tsa.domain.HashAlgorithm;
import dev.mieser.tsa.domain.TimestampRequestData;
import dev.mieser.tsa.domain.TimestampResponseData;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponse;

import java.io.IOException;
import java.util.function.Function;

import static java.lang.String.format;

/**
 * Maps Bouncycastle-specific TSP request/response objects to domain objects.
 */
@RequiredArgsConstructor
public class TimestampResponseMapper {

    private final DateConverter dateConverter;

    public TimestampResponseData map(TimeStampRequest timeStampRequest, TimeStampResponse timeStampResponse) {
        TimestampRequestData requestData = TimestampRequestData.builder()
                .hashAlgorithm(extractHashAlgorithm(timeStampRequest))
                .hash(timeStampRequest.getMessageImprintDigest())
                .nonce(timeStampRequest.getNonce())
                .certificateRequested(timeStampRequest.getCertReq())
                .tsaPolicyId(mapIfNotNull(timeStampRequest.getReqPolicy(), ASN1ObjectIdentifier::getId))
                .asnEncoded(asnEncoded(timeStampRequest, TimeStampRequest::getEncoded))
                .build();

        return TimestampResponseData.builder()
                .status(timeStampResponse.getStatus())
                .statusString(timeStampResponse.getStatusString())
                .failureInfo(mapIfNotNull(timeStampResponse.getFailInfo(), PKIFailureInfo::intValue))
                .serialNumber(mapIfNotNull(timeStampResponse.getTimeStampToken(), token -> token.getTimeStampInfo().getSerialNumber()))
                .requestData(requestData)
                .generationTime(mapIfNotNull(timeStampResponse.getTimeStampToken(), token -> dateConverter.toZonedDateTime(token.getTimeStampInfo().getGenTime())))
                .asnEncoded(asnEncoded(timeStampResponse, TimeStampResponse::getEncoded))
                .build();
    }

    /**
     * @param obj       The object to get the ASN.1 encoding of, not {@code null}.
     * @param converter The converter which should be used to convert the specified input object.
     * @param <T>       The type of the object to get the ASN.1 encoding of.
     * @return The ASN.1 representation of the input object.
     */
    private <T> byte[] asnEncoded(T obj, AsnEncodingConverter<T> converter) {
        try {
            return converter.convertToAsn(obj);
        } catch (IOException e) {
            throw new IllegalStateException("Error converting object to ASN.1.", e);
        }
    }

    /**
     * @param timeStampRequest The request to extract the hash algorithm from.
     * @return The corresponding {@link HashAlgorithm}.
     * @throws IllegalStateException When the hash algorithm is not known.
     */
    private HashAlgorithm extractHashAlgorithm(TimeStampRequest timeStampRequest) {
        String hashAlgorithmOid = timeStampRequest.getMessageImprintAlgOID().getId();

        return HashAlgorithm.fromObjectIdentifier(hashAlgorithmOid)
                .orElseThrow(() -> new IllegalStateException(format("Unknown hash algorithm with OID '%s'.", hashAlgorithmOid)));
    }

    /**
     * @param input  The value to extract a value from.
     * @param mapper The mapper to apply when the input is not {@code null}.
     * @param <T>    The input type.
     * @param <R>    The mapped type.
     * @return The extracted value returned by the specified {@code mapper} or {@code null}, when the input is {@code null}.
     */
    private <T, R> R mapIfNotNull(T input, Function<T, R> mapper) {
        if (input == null) {
            return null;
        }

        return mapper.apply(input);
    }

    /**
     * Interface abstraction of the {@link TimeStampRequest#getEncoded()}/{@link TimeStampResponse#getEncoded()} methods.
     *
     * @param <T> The type of the object to get the ASN.1 encoding of.
     */
    private interface AsnEncodingConverter<T> {

        byte[] convertToAsn(T obj) throws IOException;

    }

}
