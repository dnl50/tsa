package dev.mieser.tsa.testutil;

import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TestCertificateLoaderTest {

    @ParameterizedTest
    @MethodSource("methodsToInvoke")
    void canLoadCertificateOrPrivateKey(ThrowingSupplier<?> methodInvocation) throws Throwable {
        // given / when / then
        assertThat(methodInvocation.get()).isNotNull();
    }

    static Stream<Arguments> methodsToInvoke() {
        return Stream.of(
                arguments((ThrowingSupplier<Object>) TestCertificateLoader::loadRsaCertificate),
                arguments((ThrowingSupplier<Object>) TestCertificateLoader::loadRsaCertificate),
                arguments((ThrowingSupplier<Object>) TestCertificateLoader::loadDsaCertificate),
                arguments((ThrowingSupplier<Object>) TestCertificateLoader::loadDsaPrivateKey),
                arguments((ThrowingSupplier<Object>) TestCertificateLoader::loadEcCertificate),
                arguments((ThrowingSupplier<Object>) TestCertificateLoader::loadEcPrivateKey)
        );
    }

}
