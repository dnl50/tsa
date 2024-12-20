package dev.mieser.tsa.signing.config.validator;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import org.junit.jupiter.api.Test;

import dev.mieser.tsa.signing.config.TsaProperties;
import io.smallrye.config.PropertiesConfigSource;
import io.smallrye.config.SmallRyeConfig;
import io.smallrye.config.SmallRyeConfigBuilder;

class EitherKeystoreOrPkcs11ValidatorTest {

    @Test
    void noneConfigured() {
        assertThatViolationOccurred(Map.of());
    }

    @Test
    void bothConfigured() {
        // given
        var properties = Map.of(
            "tsa.keystore.path", "/tmp/keystore.p12",
            "tsa.pkcs11.configuration.library", "mypkcs11provider.so");

        // when
        assertThatViolationOccurred(properties);
    }

    @Test
    void onlyKeystoreConfigured() {
        // given
        var properties = Map.of(
            "tsa.keystore.path", "/tmp/keystore.p12");

        // when / then
        assertThatNoViolationOccurred(properties);
    }

    @Test
    void onlyPkcs11Configured() {
        // given
        var properties = Map.of(
            "tsa.pkcs11.configuration.library", "mypkcs11provider.so");

        // when / then
        assertThatNoViolationOccurred(properties);
    }

    private void assertThatViolationOccurred(Map<String, String> properties) {
        Set<ConstraintViolation<TsaProperties>> violations = validate(parseProperties(properties));

        assertThat(violations).map(ConstraintViolation::getMessage)
            .containsExactly("Either a Keystore or a PKCS#11 device must be configured.");
    }

    private void assertThatNoViolationOccurred(Map<String, String> properties) {
        Set<ConstraintViolation<TsaProperties>> violations = validate(parseProperties(properties));

        assertThat(violations).isEmpty();
    }

    private TsaProperties parseProperties(Map<String, String> properties) {
        SmallRyeConfig config = new SmallRyeConfigBuilder()
            .withMapping(TsaProperties.class)
            .withSources(new PropertiesConfigSource(properties, "test"))
            .build();

        return config.getConfigMapping(TsaProperties.class);
    }

    private Set<ConstraintViolation<TsaProperties>> validate(TsaProperties properties) {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            return factory.getValidator().validate(properties);
        }
    }

}
