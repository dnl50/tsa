package dev.mieser.tsa.signing.mapper;

import dev.mieser.tsa.domain.HashAlgorithm;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponse;

import java.io.IOException;
import java.util.function.Function;

import static java.lang.String.format;

/**
 * Abstract class for mapping Bouncy Castle-specific objects to domain objects.
 */
abstract class AbstractTspMapper {

    /**
     * @param asnAlgorithmIdentifier The ASN1 OID of the hash algorithm.
     * @return The corresponding {@link HashAlgorithm}.
     * @throws IllegalStateException When the hash algorithm is not known.
     */
    HashAlgorithm mapToHashAlgorithm(ASN1ObjectIdentifier asnAlgorithmIdentifier) {
        String hashAlgorithmOid = asnAlgorithmIdentifier.getId();

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
    <T, R> R mapIfNotNull(T input, Function<T, R> mapper) {
        if (input == null) {
            return null;
        }

        return mapper.apply(input);
    }

    /**
     * @param obj       The object to get the ASN.1 encoding of, not {@code null}.
     * @param converter The converter which should be used to convert the specified input object.
     * @param <T>       The type of the object to get the ASN.1 encoding of.
     * @return The ASN.1 representation of the input object.
     */
    <T> byte[] asnEncoded(T obj, AsnEncodingConverter<T> converter) {
        try {
            return converter.convertToAsn(obj);
        } catch (IOException e) {
            throw new IllegalStateException("Error converting object to ASN.1.", e);
        }
    }

    /**
     * Interface abstraction of the {@link TimeStampRequest#getEncoded()}/{@link TimeStampResponse#getEncoded()} methods.
     *
     * @param <T> The type of the object to get the ASN.1 encoding of.
     */
    interface AsnEncodingConverter<T> {

        byte[] convertToAsn(T obj) throws IOException;

    }

}