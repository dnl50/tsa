package dev.mieser.tsa.signing.impl.mapper;

import static java.lang.String.format;

import java.io.IOException;
import java.util.function.Function;

import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponse;

import dev.mieser.tsa.domain.FailureInfo;
import dev.mieser.tsa.domain.ResponseStatus;

/**
 * Abstract class for mapping Bouncy Castle-specific objects to domain objects.
 */
abstract class AbstractTspMapper {

    /**
     * @param status
     *     The status to get the corresponding {@link ResponseStatus} for.
     * @return The corresponding {@link ResponseStatus}.
     * @throws IllegalStateException
     *     When no corresponding {@link ResponseStatus} is defined.
     */
    protected ResponseStatus mapToResponseStatus(int status) {
        return ResponseStatus.fromIntValue(status)
            .orElseThrow(() -> new IllegalStateException(format("Unknown status '%d'.", status)));
    }

    /**
     * @param value
     *     The value to get the corresponding {@link FailureInfo} constant for.
     * @return The corresponding {@link FailureInfo} constant.
     * @throws IllegalStateException
     *     When no corresponding {@link FailureInfo} constant is defined.
     */
    protected FailureInfo mapToFailureInfo(int value) {
        return FailureInfo.fromIntValue(value)
            .orElseThrow(() -> new IllegalStateException(format("Unknown PKI Failure Info '%d'.", value)));
    }

    /**
     * @param input
     *     The value to extract a value from.
     * @param mapper
     *     The mapper to apply when the input is not {@code null}.
     * @param <T>
     *     The input type.
     * @param <R>
     *     The mapped type.
     * @return The extracted value returned by the specified {@code mapper} or {@code null}, when the input is {@code null}.
     */
    protected <T, R> R mapIfNotNull(T input, Function<T, R> mapper) {
        if (input == null) {
            return null;
        }

        return mapper.apply(input);
    }

    /**
     * @param obj
     *     The object to get the ASN.1 encoding of, not {@code null}.
     * @param converter
     *     The converter which should be used to convert the specified input object.
     * @param <T>
     *     The type of the object to get the ASN.1 encoding of.
     * @return The ASN.1 representation of the input object.
     */
    protected <T> byte[] asnEncoded(T obj, AsnEncodingConverter<T> converter) {
        try {
            return converter.convertToAsn(obj);
        } catch (IOException e) {
            throw new IllegalStateException("Error converting object to ASN.1.", e);
        }
    }

    /**
     * Interface abstraction of the {@link TimeStampRequest#getEncoded()}/{@link TimeStampResponse#getEncoded()} methods.
     *
     * @param <T>
     *     The type of the object to get the ASN.1 encoding of.
     */
    public interface AsnEncodingConverter<T> {

        byte[] convertToAsn(T obj) throws IOException;

    }

}
