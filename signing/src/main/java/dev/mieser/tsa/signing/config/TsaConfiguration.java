package dev.mieser.tsa.signing.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import dev.mieser.tsa.datetime.api.CurrentDateTimeService;
import dev.mieser.tsa.datetime.api.DateConverter;
import dev.mieser.tsa.datetime.config.DateTimeConfiguration;
import dev.mieser.tsa.signing.BouncyCastleTimeStampAuthority;
import dev.mieser.tsa.signing.BouncyCastleTimeStampValidator;
import dev.mieser.tsa.signing.TspParser;
import dev.mieser.tsa.signing.TspValidator;
import dev.mieser.tsa.signing.api.TimeStampAuthority;
import dev.mieser.tsa.signing.api.TimeStampValidator;
import dev.mieser.tsa.signing.cert.*;
import dev.mieser.tsa.signing.mapper.TimestampResponseMapper;
import dev.mieser.tsa.signing.mapper.TimestampVerificationResultMapper;
import dev.mieser.tsa.signing.serial.RandomSerialNumberGenerator;

@Configuration
@Import(DateTimeConfiguration.class)
@EnableConfigurationProperties(TsaProperties.class)
public class TsaConfiguration {

    @Bean
    TimeStampAuthority timeStampAuthority(TsaProperties tsaProperties, SigningCertificateLoader signingCertificateLoader,
        CurrentDateTimeService currentDateTimeService, DateConverter dateConverter) {
        return new BouncyCastleTimeStampAuthority(tsaProperties, tspParser(), tspValidator(),
            signingCertificateLoader, currentDateTimeService, new RandomSerialNumberGenerator(),
            new TimestampResponseMapper(dateConverter),
            publicKeyAnalyzer());
    }

    @Bean
    TimeStampValidator timeStampValidator(SigningCertificateLoader signingCertificateLoader, DateConverter dateConverter) {
        return new BouncyCastleTimeStampValidator(tspParser(), signingCertificateLoader, publicKeyAnalyzer(),
            new TimestampVerificationResultMapper(dateConverter), tspValidator(), signingCertificateExtractor());
    }

    @Bean
    PublicKeyAnalyzer publicKeyAnalyzer() {
        return new PublicKeyAnalyzer();
    }

    @Bean
    TspParser tspParser() {
        return new TspParser();
    }

    @Bean
    TspValidator tspValidator() {
        return new TspValidator();
    }

    @Bean
    SigningCertificateExtractor signingCertificateExtractor() {
        return new SigningCertificateExtractor();
    }

    @Bean
    @ConditionalOnExpression("#{!('${tsa.certificate.path}' matches '^classpath:.*$')}")
    SigningCertificateLoader fileSystemCertificateLoader(TsaProperties tsaProperties) {
        char[] password = toCharArray(tsaProperties.getCertificate().getPassword());

        return new FileSystemCertificateLoader(tsaProperties.getCertificate().getPath(), password);
    }

    @Bean
    @ConditionalOnExpression("#{'${tsa.certificate.path}' matches '^classpath:.*$'}")
    SigningCertificateLoader classPathSystemCertificateLoader(TsaProperties tsaProperties) {
        char[] password = toCharArray(tsaProperties.getCertificate().getPassword());
        String path = tsaProperties.getCertificate().getPath().replace("classpath:", "");

        return new ClasspathCertificateLoader(path, password);
    }

    private char[] toCharArray(String password) {
        return password != null ? password.toCharArray() : new char[0];
    }

}
