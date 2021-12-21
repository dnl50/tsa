package dev.mieser.tsa.signing.mapper;

import dev.mieser.tsa.domain.HashAlgorithm;
import dev.mieser.tsa.domain.ResponseStatus;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.function.Function;

import static dev.mieser.tsa.domain.HashAlgorithm.SHA512;
import static dev.mieser.tsa.domain.ResponseStatus.GRANTED;
import static dev.mieser.tsa.signing.mapper.AbstractTspMapper.AsnEncodingConverter;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AbstractTspMapperTest {

    private final AbstractTspMapper testSubject = new AbstractTspMapperImpl();

    @Test
    void mapToHashAlgorithmThrowsExceptionWhenHashAlgorithmIsUnknown() {
        // given
        var md5Oid = new ASN1ObjectIdentifier("1.2.840.113549.2.5");

        // when / then
        assertThatIllegalStateException()
                .isThrownBy(() -> testSubject.mapToHashAlgorithm(md5Oid))
                .withMessage("Unknown hash algorithm with OID '1.2.840.113549.2.5'.");
    }

    @Test
    void mapToHashAlgorithmReturnsExpectedHashAlgorithm() {
        // given /
        HashAlgorithm algorithm = testSubject.mapToHashAlgorithm(new ASN1ObjectIdentifier(SHA512.getObjectIdentifier()));

        // then
        assertThat(algorithm).isEqualTo(SHA512);
    }

    @Test
    void mapIfNotNullReturnsNullWhenInputIsNull(@Mock Function<String, Integer> mapperFunctionMock) {
        // given / when
        Integer mappedValue = testSubject.mapIfNotNull(null, mapperFunctionMock);

        // then
        assertThat(mappedValue).isNull();

        then(mapperFunctionMock).should(never()).apply(any());
    }

    @Test
    void mapIfNotNullReturnsMappedValueWhenInputIsNotNull(@Mock Function<String, Integer> mapperFunctionMock) {
        // given
        String value = "1337";

        given(mapperFunctionMock.apply(value)).willReturn(1337);

        //  when
        Integer mappedValue = testSubject.mapIfNotNull(value, mapperFunctionMock);

        // then
        assertThat(mappedValue).isEqualTo(1337);
    }

    @Test
    void asnEncodedThrowsExceptionWhenObjectCannotBeConvertedToAsn(@Mock AsnEncodingConverter<String> asnEncodingConverterMock) throws IOException {
        // given
        String value = "test";
        var thrownException = new IOException("error!!!1!");

        given(asnEncodingConverterMock.convertToAsn(value)).willThrow(thrownException);

        // when / then
        assertThatIllegalStateException()
                .isThrownBy(() -> testSubject.asnEncoded(value, asnEncodingConverterMock))
                .withMessage("Error converting object to ASN.1.")
                .withCause(thrownException);
    }

    @Test
    void asnEncodedReturnsAsnEncodedValue(@Mock AsnEncodingConverter<String> asnEncodingConverterMock) throws IOException {
        // given
        String value = "test";

        given(asnEncodingConverterMock.convertToAsn(value)).willAnswer(invocation -> ((String) invocation.getArgument(0)).getBytes(UTF_8));

        // when
        byte[] asnEncodedValue = testSubject.asnEncoded(value, asnEncodingConverterMock);

        // then
        assertThat(asnEncodedValue).isEqualTo(value.getBytes(UTF_8));
    }

    @Test
    void mapToResponseStatusReturnsExpectedStatus() {
        // given / when
        ResponseStatus responseStatus = testSubject.mapToResponseStatus(0);

        // then
        assertThat(responseStatus).isEqualTo(GRANTED);
    }

    @Test
    void mapToResponseStatusThrowsExceptionWhenNoStatusIsDefined() {
        // given / when / then
        assertThatIllegalStateException()
                .isThrownBy(() -> testSubject.mapToResponseStatus(-1))
                .withMessage("Unknown status '-1'.");
    }

    private static class AbstractTspMapperImpl extends AbstractTspMapper {

    }

}
