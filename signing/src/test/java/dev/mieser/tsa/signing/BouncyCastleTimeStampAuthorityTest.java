package dev.mieser.tsa.signing;

import dev.mieser.tsa.datetime.api.CurrentDateTimeService;
import dev.mieser.tsa.domain.HashAlgorithm;
import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.signing.api.exception.TsaInitializationException;
import dev.mieser.tsa.signing.api.exception.TsaNotInitializedException;
import dev.mieser.tsa.signing.api.exception.UnknownHashAlgorithmException;
import dev.mieser.tsa.signing.cert.SigningCertificateLoader;
import dev.mieser.tsa.signing.config.properties.TsaProperties;
import dev.mieser.tsa.signing.mapper.TimestampResponseMapper;
import dev.mieser.tsa.signing.serial.SerialNumberGenerator;
import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.cmp.PKIFailureInfo;
import org.bouncycastle.asn1.tsp.MessageImprint;
import org.bouncycastle.asn1.tsp.TimeStampReq;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.tsp.TimeStampRequest;
import org.bouncycastle.tsp.TimeStampResponse;
import org.bouncycastle.util.io.pem.PemReader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.EnumSet;

import static dev.mieser.tsa.domain.HashAlgorithm.SHA256;
import static dev.mieser.tsa.domain.HashAlgorithm.SHA512;
import static java.math.BigInteger.ONE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BouncyCastleTimeStampAuthorityTest {

    private static final byte[] SHA_256_HASH = "32 character long ASCII sequence".getBytes(UTF_8);

    private final TspRequestParser tspRequestParserMock;

    private final TspRequestValidator tspRequestValidatorMock;

    private final SigningCertificateLoader signingCertificateLoaderMock;

    private final CurrentDateTimeService currentDateTimeServiceMock;

    private final SerialNumberGenerator serialNumberGeneratorMock;

    private final TimestampResponseMapper timestampResponseMapperMock;

    BouncyCastleTimeStampAuthorityTest(@Mock TspRequestParser tspRequestParserMock, @Mock TspRequestValidator tspRequestValidatorMock,
                                       @Mock SigningCertificateLoader signingCertificateLoaderMock, @Mock CurrentDateTimeService currentDateTimeServiceMock,
                                       @Mock SerialNumberGenerator serialNumberGeneratorMock, @Mock TimestampResponseMapper timestampResponseMapper) {
        this.tspRequestParserMock = tspRequestParserMock;
        this.tspRequestValidatorMock = tspRequestValidatorMock;
        this.signingCertificateLoaderMock = signingCertificateLoaderMock;
        this.currentDateTimeServiceMock = currentDateTimeServiceMock;
        this.serialNumberGeneratorMock = serialNumberGeneratorMock;
        this.timestampResponseMapperMock = timestampResponseMapper;
    }

    @Test
    void signRequestThrowsExceptionWhenTsaIsNotInitialized() {
        // given
        var tspRequestInputStream = new ByteArrayInputStream(new byte[0]);

        BouncyCastleTimeStampAuthority testSubject = createTestSubject(new TsaProperties());

        // when / then
        assertThatExceptionOfType(TsaNotInitializedException.class)
                .isThrownBy(() -> testSubject.signRequest(tspRequestInputStream));
    }

    @Test
    void initializeThrowsExceptionWhenInitializationFails() throws IOException {
        // given
        BouncyCastleTimeStampAuthority testSubject = createTestSubject(new TsaProperties());

        IOException cause = new IOException();
        when(signingCertificateLoaderMock.loadCertificate()).thenThrow(cause);

        // when / then
        assertThatExceptionOfType(TsaInitializationException.class)
                .isThrownBy(testSubject::initialize)
                .withMessage("Could not initialize TSA.")
                .withCause(cause);
    }

    @Test
    void signRequestThrowsExceptionWhenTsaDoesNotRecognizeHashAlgorithm(@Mock TimeStampRequest timeStampRequestMock) throws Exception {
        // given
        var tspRequestInputStream = new ByteArrayInputStream(new byte[0]);
        var tsaProperties = new TsaProperties();
        tsaProperties.setEssCertIdAlgorithm(HashAlgorithm.SHA1);
        tsaProperties.setSigningDigestAlgorithm(SHA256);

        X509Certificate certificate = loadRsaCertificate();
        PrivateKey privateKey = loadRsaPrivateKey();

        when(tspRequestParserMock.parseRequest(tspRequestInputStream)).thenReturn(timeStampRequestMock);
        when(tspRequestValidatorMock.isKnownHashAlgorithm(timeStampRequestMock)).thenReturn(false);
        when(timeStampRequestMock.getMessageImprintAlgOID()).thenReturn(new ASN1ObjectIdentifier("1.2.840.113549.2.5"));

        BouncyCastleTimeStampAuthority testSubject = createInitializedTestSubject(tsaProperties, certificate, privateKey);

        // when / then
        assertThatExceptionOfType(UnknownHashAlgorithmException.class)
                .isThrownBy(() -> testSubject.signRequest(tspRequestInputStream))
                .withMessage("Unknown hash algorithm OID '1.2.840.113549.2.5'.");
    }

    @Test
    void signRequestReturnsMappedTspResponse(@Mock TimestampResponseData timestampResponseDataMock) throws Exception {
        // given
        TimeStampRequest tspRequest = createSha256TimestampRequest();
        var tspRequestInputStream = new ByteArrayInputStream(tspRequest.getEncoded());
        var tsaProperties = new TsaProperties();
        tsaProperties.setAcceptedHashAlgorithms(EnumSet.of(SHA512));

        X509Certificate certificate = loadRsaCertificate();
        PrivateKey privateKey = loadRsaPrivateKey();

        when(tspRequestParserMock.parseRequest(tspRequestInputStream)).thenReturn(tspRequest);
        when(tspRequestValidatorMock.isKnownHashAlgorithm(tspRequest)).thenReturn(true);
        when(timestampResponseMapperMock.map(eq(tspRequest), any())).thenReturn(timestampResponseDataMock);
        when(currentDateTimeServiceMock.now()).thenReturn(new Date());
        when(serialNumberGeneratorMock.generateSerialNumber()).thenReturn(BigInteger.TEN);

        BouncyCastleTimeStampAuthority testSubject = createInitializedTestSubject(tsaProperties, certificate, privateKey);

        // when
        TimestampResponseData actualResponseData = testSubject.signRequest(tspRequestInputStream);

        // then
        assertThat(actualResponseData).isEqualTo(timestampResponseDataMock);
    }

    @Test
    void signRequestDoesNotSignRequestWhenHashAlgorithmIsNotAccepted() throws Exception {
        // given
        TimeStampRequest tspRequest = createSha256TimestampRequest();
        var tspRequestInputStream = new ByteArrayInputStream(tspRequest.getEncoded());
        var tsaProperties = new TsaProperties();
        tsaProperties.setAcceptedHashAlgorithms(EnumSet.of(SHA512));

        X509Certificate certificate = loadRsaCertificate();
        PrivateKey privateKey = loadRsaPrivateKey();

        when(tspRequestParserMock.parseRequest(tspRequestInputStream)).thenReturn(tspRequest);
        when(tspRequestValidatorMock.isKnownHashAlgorithm(tspRequest)).thenReturn(true);
        when(currentDateTimeServiceMock.now()).thenReturn(new Date());
        when(serialNumberGeneratorMock.generateSerialNumber()).thenReturn(BigInteger.TEN);

        BouncyCastleTimeStampAuthority testSubject = createInitializedTestSubject(tsaProperties, certificate, privateKey);

        // when
        testSubject.signRequest(tspRequestInputStream);

        // then
        ArgumentCaptor<TimeStampResponse> tspResponseCaptor = ArgumentCaptor.forClass(TimeStampResponse.class);
        then(timestampResponseMapperMock).should().map(eq(tspRequest), tspResponseCaptor.capture());

        TimeStampResponse generatedTspResponse = tspResponseCaptor.getValue();
        assertThat(generatedTspResponse.getFailInfo()).extracting(PKIFailureInfo::intValue).isEqualTo(PKIFailureInfo.badAlg);
    }

    @Test
    void signRequestSignsRequestWhenHashAlgorithmIsAccepted() throws Exception {
        // given
        TimeStampRequest tspRequest = createSha256TimestampRequest();
        var tspRequestInputStream = new ByteArrayInputStream(tspRequest.getEncoded());

        X509Certificate certificate = loadRsaCertificate();
        PrivateKey privateKey = loadRsaPrivateKey();
        Date signingDate = Date.from(ZonedDateTime.parse("2021-11-06T15:57:37+01:00").toInstant());

        when(tspRequestParserMock.parseRequest(tspRequestInputStream)).thenReturn(tspRequest);
        when(tspRequestValidatorMock.isKnownHashAlgorithm(tspRequest)).thenReturn(true);
        when(currentDateTimeServiceMock.now()).thenReturn(signingDate);
        when(serialNumberGeneratorMock.generateSerialNumber()).thenReturn(BigInteger.TEN);

        BouncyCastleTimeStampAuthority testSubject = createInitializedTestSubject(new TsaProperties(), certificate, privateKey);

        // when
        testSubject.signRequest(tspRequestInputStream);

        // then
        ArgumentCaptor<TimeStampResponse> tspResponseCaptor = ArgumentCaptor.forClass(TimeStampResponse.class);
        then(timestampResponseMapperMock).should().map(eq(tspRequest), tspResponseCaptor.capture());

        TimeStampResponse generatedTspResponse = tspResponseCaptor.getValue();
        assertThat(generatedTspResponse.getTimeStampToken().getTimeStampInfo().getGenTime()).isEqualTo(signingDate);
    }

    @Test
    void signRequestUsesConfiguredPolicy() throws Exception {
        // given
        TimeStampRequest tspRequest = createSha256TimestampRequest();
        var tspRequestInputStream = new ByteArrayInputStream(tspRequest.getEncoded());
        var tsaProperties = new TsaProperties();
        tsaProperties.setPolicyOid("1.2.3.4.5");

        X509Certificate certificate = loadRsaCertificate();
        PrivateKey privateKey = loadRsaPrivateKey();
        Date signingDate = Date.from(ZonedDateTime.parse("2021-11-06T15:57:37+01:00").toInstant());

        when(tspRequestParserMock.parseRequest(tspRequestInputStream)).thenReturn(tspRequest);
        when(tspRequestValidatorMock.isKnownHashAlgorithm(tspRequest)).thenReturn(true);
        when(currentDateTimeServiceMock.now()).thenReturn(signingDate);
        when(serialNumberGeneratorMock.generateSerialNumber()).thenReturn(BigInteger.TEN);

        BouncyCastleTimeStampAuthority testSubject = createInitializedTestSubject(tsaProperties, certificate, privateKey);

        // when
        testSubject.signRequest(tspRequestInputStream);

        // then
        ArgumentCaptor<TimeStampResponse> tspResponseCaptor = ArgumentCaptor.forClass(TimeStampResponse.class);
        then(timestampResponseMapperMock).should().map(eq(tspRequest), tspResponseCaptor.capture());

        TimeStampResponse generatedTspResponse = tspResponseCaptor.getValue();
        assertThat(generatedTspResponse.getTimeStampToken().getTimeStampInfo().getPolicy()).isEqualTo(new ASN1ObjectIdentifier("1.2.3.4.5"));
    }

    @Test
    void signRequestUsesGeneratedSerialNumber() throws Exception {
        // given
        TimeStampRequest tspRequest = createSha256TimestampRequest();
        var tspRequestInputStream = new ByteArrayInputStream(tspRequest.getEncoded());
        var tsaProperties = new TsaProperties();
        tsaProperties.setPolicyOid("1.2.3.4.5");

        X509Certificate certificate = loadRsaCertificate();
        PrivateKey privateKey = loadRsaPrivateKey();
        Date signingDate = Date.from(ZonedDateTime.parse("2021-11-06T15:57:37+01:00").toInstant());

        when(tspRequestParserMock.parseRequest(tspRequestInputStream)).thenReturn(tspRequest);
        when(tspRequestValidatorMock.isKnownHashAlgorithm(tspRequest)).thenReturn(true);
        when(currentDateTimeServiceMock.now()).thenReturn(signingDate);
        when(serialNumberGeneratorMock.generateSerialNumber()).thenReturn(BigInteger.TEN);

        BouncyCastleTimeStampAuthority testSubject = createInitializedTestSubject(tsaProperties, certificate, privateKey);

        // when
        testSubject.signRequest(tspRequestInputStream);

        // then
        ArgumentCaptor<TimeStampResponse> tspResponseCaptor = ArgumentCaptor.forClass(TimeStampResponse.class);
        then(timestampResponseMapperMock).should().map(eq(tspRequest), tspResponseCaptor.capture());

        TimeStampResponse generatedTspResponse = tspResponseCaptor.getValue();
        assertThat(generatedTspResponse.getTimeStampToken().getTimeStampInfo().getSerialNumber()).isEqualTo(BigInteger.TEN);
    }

    private BouncyCastleTimeStampAuthority createTestSubject(TsaProperties tsaProperties) {
        return new BouncyCastleTimeStampAuthority(tsaProperties, tspRequestParserMock, tspRequestValidatorMock, signingCertificateLoaderMock,
                currentDateTimeServiceMock, serialNumberGeneratorMock, timestampResponseMapperMock);
    }

    private BouncyCastleTimeStampAuthority createInitializedTestSubject(TsaProperties tsaProperties, X509Certificate x509Certificate, PrivateKey privateKey) throws IOException {
        when(signingCertificateLoaderMock.loadCertificate()).thenReturn(x509Certificate);
        when(signingCertificateLoaderMock.loadPrivateKey()).thenReturn(privateKey);

        BouncyCastleTimeStampAuthority testSubject = createTestSubject(tsaProperties);
        testSubject.initialize();
        return testSubject;
    }

    private X509Certificate loadRsaCertificate() throws Exception {
        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

        try (InputStream certificateInputStream = getClass().getResourceAsStream("tsa-cert/cert-rsa.pem")) {
            return (X509Certificate) certFactory.generateCertificate(certificateInputStream);
        }
    }

    private PrivateKey loadRsaPrivateKey() throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");

        try (Reader privateKeyReader = new InputStreamReader(getClass().getResourceAsStream("tsa-cert/key-rsa.pem"))) {
            PemReader pemReader = new PemReader(privateKeyReader);
            byte[] privateKeyBytes = pemReader.readPemObject().getContent();
            PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            return keyFactory.generatePrivate(privateKeySpec);
        }
    }

    private TimeStampRequest createSha256TimestampRequest() {
        var imprint = new MessageImprint(new AlgorithmIdentifier(new ASN1ObjectIdentifier(SHA256.getObjectIdentifier())), SHA_256_HASH);
        return new TimeStampRequest(new TimeStampReq(imprint, null, new ASN1Integer(ONE), ASN1Boolean.TRUE, null));
    }

}
