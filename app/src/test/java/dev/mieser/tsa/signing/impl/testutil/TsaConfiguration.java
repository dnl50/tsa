package dev.mieser.tsa.signing.impl.testutil;

import java.util.Set;

import dev.mieser.tsa.signing.config.HashAlgorithm;
import dev.mieser.tsa.signing.impl.cert.PublicKeyAlgorithm;

/**
 * Encapsulates a TSA Configuration to ease parameterized tests.
 * 
 * @see DelegatingTsaProperties
 * @see ConfigurableSigningKeystoreLoader
 */
public record TsaConfiguration(PublicKeyAlgorithm publicKeyAlgorithm, HashAlgorithm signingDigestAlgorithm,
    HashAlgorithm essCertIdAlgorithm, Set<HashAlgorithm> acceptedHashAlgorithms, String policyOid) {
}
