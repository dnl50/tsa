package dev.mieser.tsa.signing.impl.testutil;

import java.util.Set;
import java.util.stream.Collectors;

import lombok.Setter;

import dev.mieser.tsa.signing.config.HashAlgorithm;
import dev.mieser.tsa.signing.config.TsaProperties;

/**
 * {@link TsaProperties} Implementation for easier testing. Reads the configuration from a {@link TsaConfiguration}.
 */
@Setter
public class DelegatingTsaProperties implements TsaProperties {

    private TsaConfiguration configuration;

    @Override
    public HashAlgorithm essCertIdAlgorithm() {
        return configuration.essCertIdAlgorithm();
    }

    @Override
    public HashAlgorithm signingDigestAlgorithm() {
        return configuration.signingDigestAlgorithm();
    }

    @Override
    public Set<String> acceptedHashAlgorithms() {
        return configuration.acceptedHashAlgorithms().stream()
            .map(HashAlgorithm::getObjectIdentifier)
            .collect(Collectors.toUnmodifiableSet());
    }

    @Override
    public String policyOid() {
        return configuration.policyOid();
    }

    @Override
    public KeystoreLoaderProperties keystore() {
        throw new UnsupportedOperationException("Not required for unit testing.");
    }

}
