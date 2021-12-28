package dev.mieser.tsa.signing.cert;

import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.DigestCalculatorProvider;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.util.Selector;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import static java.util.Arrays.copyOf;

/**
 * {@link Selector} which selects a {@link X509CertificateHolder X.509 Certificate} with a specific hash value.
 */
@RequiredArgsConstructor
public class HashBasedCertificateSelector implements Selector<X509CertificateHolder> {

    /**
     * The algorithm which was used to calculate the hash with, not {@code null}.
     */
    private final AlgorithmIdentifier algorithmIdentifier;

    /**
     * The hash of the X.509 certificate to select, not {@code null}.
     */
    private final byte[] hash;

    /**
     * Provider for digest calculators.
     */
    private final DigestCalculatorProvider digestCalculatorProvider;

    public HashBasedCertificateSelector(AlgorithmIdentifier algorithmIdentifier, byte[] hash) {
        this(algorithmIdentifier, hash, new BcDigestCalculatorProvider());
    }

    @Override
    public boolean match(X509CertificateHolder certificate) {
        try {
            DigestCalculator digestCalculator = digestCalculatorProvider.get(algorithmIdentifier);
            try (OutputStream digestOutputStream = digestCalculator.getOutputStream()) {
                new ByteArrayInputStream(certificate.getEncoded()).transferTo(digestOutputStream);
            }

            return Arrays.equals(digestCalculator.getDigest(), hash);
        } catch (OperatorCreationException | IOException e) {
            throw new IllegalStateException("Failed to calculate hash.", e);
        }
    }

    @Override
    public HashBasedCertificateSelector clone() {
        return new HashBasedCertificateSelector(algorithmIdentifier, copyOf(hash, hash.length));
    }

}
