package dev.mieser.tsa.signing.impl.testutil;

import java.util.Set;

import lombok.Setter;

import dev.mieser.tsa.domain.HashAlgorithm;
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
    public Set<HashAlgorithm> acceptedHashAlgorithms() {
        return configuration.acceptedHashAlgorithms();
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
