package dev.mieser.tsa.signing.config;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.DefaultDigestAlgorithmIdentifierFinder;
import org.bouncycastle.operator.DigestAlgorithmIdentifierFinder;

@Slf4j
public class DigestAlgorithmConverter {

    private final DigestAlgorithmIdentifierFinder digestAlgorithmFinder = new DefaultDigestAlgorithmIdentifierFinder();

    /**
     * 
     * @param value
     *     Either the name or the OID of a digest algorithm.
     * @return The converted OID or {@code null} when the value cannot be converted to a OID of a digest algorithm supported
     * by Bouncy Castle.
     * @see DigestAlgorithmIdentifierFinder#find(String)
     */
    public ASN1ObjectIdentifier convert(String value) {
        AlgorithmIdentifier algorithmIdentifier = digestAlgorithmFinder.find(value);
        return algorithmIdentifier != null ? algorithmIdentifier.getAlgorithm() : null;
    }

    /**
     * @see #convert(String)
     */
    public Set<ASN1ObjectIdentifier> convert(Collection<String> values) {
        return values.stream()
            .map(this::convert)
            .collect(Collectors.toSet());
    }

}
