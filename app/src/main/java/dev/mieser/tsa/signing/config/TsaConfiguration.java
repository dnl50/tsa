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
import dev.mieser.tsa.signing.impl.TspValidator;
import dev.mieser.tsa.signing.impl.cert.Pkcs12SigningCertificateLoader;
import dev.mieser.tsa.signing.impl.cert.SigningCertificateExtractor;
import dev.mieser.tsa.signing.impl.cert.SigningCertificateLoader;
import dev.mieser.tsa.signing.impl.mapper.TimeStampResponseMapper;
import dev.mieser.tsa.signing.impl.mapper.TimeStampValidationResultMapper;
import dev.mieser.tsa.signing.impl.serial.RandomSerialNumberGenerator;

public class TsaConfiguration {

    @Produces
    @ApplicationScoped
    TimeStampAuthority timeStampAuthority(TsaProperties tsaProperties,
        TspParser tspParser,
        TspValidator tspValidator,
        SigningCertificateLoader signingCertificateLoader,
        CurrentDateService currentDateService,
        DateConverter dateConverter) {
        return new BouncyCastleTimeStampAuthority(tsaProperties,
            tspParser,
            tspValidator,
            signingCertificateLoader,
            currentDateService,
            new RandomSerialNumberGenerator(),
            new TimeStampResponseMapper(dateConverter));
    }

    @Produces
    @ApplicationScoped
    TimeStampValidator timeStampValidator(TspParser tspParser,
        SigningCertificateLoader signingCertificateLoader,
        DateConverter dateConverter,
        TspValidator tspValidator,
        SigningCertificateExtractor signingCertificateExtractor) {
        return new BouncyCastleTimeStampValidator(tspParser,
            signingCertificateLoader,
            new TimeStampValidationResultMapper(dateConverter),
            tspValidator,
            signingCertificateExtractor);
    }

    @Produces
    @ApplicationScoped
    TspParser tspParser() {
        return new TspParser();
    }

    @ApplicationScoped
    TspValidator tspValidator() {
        return new TspValidator();
    }

    @ApplicationScoped
    SigningCertificateExtractor signingCertificateExtractor() {
        return new SigningCertificateExtractor();
    }

    @Produces
    @ApplicationScoped
    SigningCertificateLoader signingCertificateLoader(TsaProperties tsaProperties) {
        char[] password = tsaProperties.certificate().password()
            .map(String::toCharArray)
            .orElse(new char[0]);

        return new Pkcs12SigningCertificateLoader(tsaProperties.certificate().path(), password);
    }

}
