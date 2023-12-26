package dev.mieser.tsa.testutil;

import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.Date;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveGenParameterSpec;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CertificateGenerator {

    /**
     * @return A X509 Certificate containing a ED25519 public key. The certificate was signed using ECC.
     */
    public static X509Certificate createEd25519Certificate() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, OperatorCreationException, CertificateException {
        Provider jcaProvider = new BouncyCastleProvider();

        KeyPairGenerator ecKeyPairGenerator = KeyPairGenerator.getInstance("EC", jcaProvider);
        ecKeyPairGenerator.initialize(new ECNamedCurveGenParameterSpec("brainpoolP384r1"));
        KeyPair ecKeyPair = ecKeyPairGenerator.generateKeyPair();

        KeyPairGenerator ed25519KeyPairGenerator = KeyPairGenerator.getInstance("Ed25519", jcaProvider);
        KeyPair ed25519KeyPair = ed25519KeyPairGenerator.generateKeyPair();

        var issuer = new X500Name("CN=issuer");
        var subject = new X500Name("CN=subject");
        ZonedDateTime now = ZonedDateTime.now();
        Date notBefore = Date.from(now.minusDays(1L).toInstant());
        Date notAfter = Date.from(now.plusDays(1L).toInstant());

        ContentSigner signer = new JcaContentSignerBuilder("SHA256withECDSA")
            .setProvider(jcaProvider)
            .build(ecKeyPair.getPrivate());

        X509CertificateHolder certificate = new JcaX509v3CertificateBuilder(issuer, BigInteger.TEN, notBefore, notAfter, subject,
            ed25519KeyPair.getPublic()).build(signer);

        return new JcaX509CertificateConverter().getCertificate(certificate);
    }

}
