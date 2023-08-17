package dev.mieser.tsa.signing.config;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import dev.mieser.tsa.datetime.api.CurrentDateService;
import dev.mieser.tsa.datetime.api.DateConverter;
import dev.mieser.tsa.signing.api.TimeStampAuthority;
import dev.mieser.tsa.signing.api.TimeStampValidator;
import dev.mieser.tsa.signing.impl.BouncyCastleTimeStampAuthority;
import dev.mieser.tsa.signing.impl.BouncyCastleTimeStampValidator;
import dev.mieser.tsa.signing.impl.TspParser;
import dev.mieser.tsa.signing.impl.cert.CertificateParser;
import dev.mieser.tsa.signing.impl.cert.Pkcs12SigningKeystoreLoader;
import dev.mieser.tsa.signing.impl.cert.SigningCertificateExtractor;
import dev.mieser.tsa.signing.impl.cert.SigningKeystoreLoader;
import dev.mieser.tsa.signing.impl.mapper.TimeStampResponseMapper;
import dev.mieser.tsa.signing.impl.mapper.TimeStampValidationResultMapper;
import dev.mieser.tsa.signing.impl.serial.RandomSerialNumberGenerator;

public class TsaConfiguration {

    @Produces
    @ApplicationScoped
    TimeStampAuthority timeStampAuthority(TsaProperties tsaProperties,
        TspParser tspParser,
        SigningKeystoreLoader signingKeystoreLoader,
        CurrentDateService currentDateService,
        DateConverter dateConverter) {
        return new BouncyCastleTimeStampAuthority(tsaProperties,
            tspParser,
            signingKeystoreLoader,
            currentDateService,
            new RandomSerialNumberGenerator(),
            new TimeStampResponseMapper(dateConverter));
    }

    @Produces
    @ApplicationScoped
    TimeStampValidator timeStampValidator(TspParser tspParser,
        SigningKeystoreLoader signingKeystoreLoader,
        DateConverter dateConverter) {
        return new BouncyCastleTimeStampValidator(tspParser,
            signingKeystoreLoader,
            new TimeStampValidationResultMapper(dateConverter),
            new SigningCertificateExtractor(),
            new CertificateParser());
    }

    @Produces
    @ApplicationScoped
    TspParser tspParser() {
        return new TspParser();
    }

    @Produces
    @ApplicationScoped
    SigningKeystoreLoader signingCertificateLoader(TsaProperties tsaProperties) {
        char[] password = tsaProperties.keystore().password()
            .map(String::toCharArray)
            .orElse(new char[0]);

        return new Pkcs12SigningKeystoreLoader(tsaProperties.keystore().path(), password);
    }

}
