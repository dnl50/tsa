package dev.mieser.tsa.signing.config;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.Provider;
import java.security.Security;
import java.util.Optional;
import java.util.Properties;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import dev.mieser.tsa.datetime.api.CurrentDateService;
import dev.mieser.tsa.datetime.api.DateConverter;
import dev.mieser.tsa.signing.api.TimeStampAuthority;
import dev.mieser.tsa.signing.api.TimeStampValidator;
import dev.mieser.tsa.signing.config.TsaProperties.KeystoreProperties;
import dev.mieser.tsa.signing.config.TsaProperties.Pkcs11Properties;
import dev.mieser.tsa.signing.impl.BouncyCastleTimeStampAuthority;
import dev.mieser.tsa.signing.impl.BouncyCastleTimeStampValidator;
import dev.mieser.tsa.signing.impl.TspParser;
import dev.mieser.tsa.signing.impl.cert.CertificateParser;
import dev.mieser.tsa.signing.impl.cert.SigningCertificateExtractor;
import dev.mieser.tsa.signing.impl.cert.keystore.Pkcs11SigningKeystoreLoader;
import dev.mieser.tsa.signing.impl.cert.keystore.Pkcs12SigningKeystoreLoader;
import dev.mieser.tsa.signing.impl.cert.keystore.SigningKeystoreLoader;
import dev.mieser.tsa.signing.impl.mapper.TimeStampResponseMapper;
import dev.mieser.tsa.signing.impl.mapper.TimeStampValidationResultMapper;
import dev.mieser.tsa.signing.impl.serial.RandomSerialNumberGenerator;

@Slf4j
public class TsaConfiguration {

    @Produces
    @ApplicationScoped
    TimeStampAuthority timeStampAuthority(Provider jceProvider,
        TsaProperties tsaProperties,
        SigningKeystoreLoader signingKeystoreLoader,
        CurrentDateService currentDateService,
        DateConverter dateConverter) {
        return new BouncyCastleTimeStampAuthority(jceProvider,
            tsaProperties,
            new TspParser(),
            signingKeystoreLoader, currentDateService,
            new RandomSerialNumberGenerator(),
            new TimeStampResponseMapper(dateConverter),
            new DigestAlgorithmConverter());
    }

    @Produces
    @ApplicationScoped
    TimeStampValidator timeStampValidator(SigningKeystoreLoader signingKeystoreLoader,
        DateConverter dateConverter) {
        return new BouncyCastleTimeStampValidator(new TspParser(),
            signingKeystoreLoader,
            new TimeStampValidationResultMapper(dateConverter),
            new SigningCertificateExtractor(),
            new CertificateParser());
    }

    @Produces
    @ApplicationScoped
    SigningKeystoreLoader signingCertificateLoader(TsaProperties tsaProperties, Provider jceProvider) {
        Optional<KeystoreProperties> keystoreProperties = tsaProperties.keystore();
        if (keystoreProperties.isPresent()) {
            char[] password = keystoreProperties.get().password()
                .map(String::toCharArray)
                .orElse(new char[0]);

            return new Pkcs12SigningKeystoreLoader(
                keystoreProperties.get().path(),
                password,
                keystoreProperties.get().alias().orElse(null));
        } else {
            char[] pin = tsaProperties.pkcs11().orElseThrow().pin()
                .map(String::toCharArray).orElse(new char[0]);

            return new Pkcs11SigningKeystoreLoader(jceProvider, pin);
        }
    }

    @Produces
    @Singleton
    Provider jceProvider(TsaProperties tsaProperties) {
        if (tsaProperties.pkcs11().isPresent()) {
            log.info("Using SunPKCS11 as JCE provider");
            return configureSunPkcs11Provider(tsaProperties.pkcs11().orElseThrow());
        } else {
            log.info("Using Bouncy Castle as JCE provider");
            return new BouncyCastleProvider();
        }
    }

    private Provider configureSunPkcs11Provider(Pkcs11Properties pkcs11Properties) {
        Provider uninitializedPkcs11Provider = Security.getProvider("SunPKCS11");
        if (uninitializedPkcs11Provider == null) {
            throw new IllegalStateException("SunPKCS11 provider is not available!");
        }

        Path tempFile = createTempFile();
        try {
            try (
                Writer writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8, StandardOpenOption.TRUNCATE_EXISTING)) {
                var properties = new Properties();
                properties.putAll(pkcs11Properties.additionalConfiguration());
                properties.put("library", pkcs11Properties.library().toAbsolutePath().toString());
                properties.put("name", "TSA");
                properties.store(writer, null);
            }

            Provider initializedPkcs11Provider = uninitializedPkcs11Provider.configure(tempFile.toAbsolutePath().toString());
            log.info("PKCS#11 provider configured successfully.");

            return initializedPkcs11Provider;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to configure SunPKCS11 provider", e);
        } finally {
            deleteTempFile(tempFile);
        }
    }

    private Path createTempFile() {
        try {
            return Files.createTempFile("sunpkcs11", ".cfg");
        } catch (IOException e) {
            throw new IllegalStateException("Failed to create temporary file for SunPKCS11 configuration", e);
        }
    }

    private void deleteTempFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.debug("Failed to delete temporary file '{}'", path, e);
        }
    }

}
