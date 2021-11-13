package dev.mieser.tsa.signing.config;

import dev.mieser.tsa.datetime.api.CurrentDateTimeService;
import dev.mieser.tsa.datetime.api.DateConverter;
import dev.mieser.tsa.datetime.config.DateTimeConfiguration;
import dev.mieser.tsa.signing.BouncyCastleTimeStampAuthority;
import dev.mieser.tsa.signing.TspRequestParser;
import dev.mieser.tsa.signing.TspRequestValidator;
import dev.mieser.tsa.signing.api.TimeStampAuthority;
import dev.mieser.tsa.signing.cert.ClasspathCertificateLoader;
import dev.mieser.tsa.signing.cert.FileSystemCertificateLoader;
import dev.mieser.tsa.signing.cert.SigningCertificateLoader;
import dev.mieser.tsa.signing.config.properties.TsaProperties;
import dev.mieser.tsa.signing.mapper.TimestampResponseMapper;
import dev.mieser.tsa.signing.serial.RandomSerialNumberGenerator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(DateTimeConfiguration.class)
@EnableConfigurationProperties(TsaProperties.class)
public class TsaConfiguration {

    @Bean
    TimeStampAuthority timeStampAuthority(TsaProperties tsaProperties, SigningCertificateLoader signingCertificateLoader,
                                          CurrentDateTimeService currentDateTimeService, DateConverter dateConverter) {
        return new BouncyCastleTimeStampAuthority(tsaProperties, new TspRequestParser(), new TspRequestValidator(),
                signingCertificateLoader, currentDateTimeService, new RandomSerialNumberGenerator(), new TimestampResponseMapper(dateConverter));
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
