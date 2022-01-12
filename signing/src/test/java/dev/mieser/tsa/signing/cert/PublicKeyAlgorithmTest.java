package dev.mieser.tsa.signing.cert;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PublicKeyAlgorithmTest {

    @Test
    void fromJcaNameThrowsExceptionWhenAlgorithmIsNull() {
        // given / when / then
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PublicKeyAlgorithm.fromJcaName(null))
            .withMessage("JCA name cannot be blank.");
    }

    @Test
    void fromJcaNameThrowsExceptionWhenAlgorithmIsEmpty() {
        // given
        String jcaName = "\t";

        // when / then
        // given / when / then
        assertThatIllegalArgumentException()
            .isThrownBy(() -> PublicKeyAlgorithm.fromJcaName(jcaName))
            .withMessage("JCA name cannot be blank.");
    }

    @Test
    void fromJcaNameReturnsEmptyOptionalWhenAlgorithmDoesNotExist() {
        // given
        String jcaName = "unknown JCA algorithm";

        // when
        Optional<PublicKeyAlgorithm> actualAlgorithm = PublicKeyAlgorithm.fromJcaName(jcaName);

        // then
        assertThat(actualAlgorithm).isEmpty();
    }

    @Test
    void fromJcaNameReturnsExpectedAlgorithm() {
        // given
        String jcaName = "EC";

        // when
        Optional<PublicKeyAlgorithm> actualAlgorithm = PublicKeyAlgorithm.fromJcaName(jcaName);

        // then
        assertThat(actualAlgorithm).contains(PublicKeyAlgorithm.EC);
    }

    @ParameterizedTest
    @MethodSource("enumToJcaName")
    void algorithmHasExpectedJcaName(PublicKeyAlgorithm algorithm, String expectedJcaName) {
        // given / when / then
        assertThat(algorithm.getJcaName()).isEqualTo(expectedJcaName);
    }

    static Stream<Arguments> enumToJcaName() {
        return Stream.of(
            arguments(PublicKeyAlgorithm.DSA, "DSA"),
            arguments(PublicKeyAlgorithm.RSA, "RSA"),
            arguments(PublicKeyAlgorithm.EC, "EC"));
    }

}
