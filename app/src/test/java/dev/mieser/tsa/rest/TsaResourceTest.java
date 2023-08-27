package dev.mieser.tsa.rest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;

import jakarta.ws.rs.core.MediaType;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.tsp.MessageImprint;
import org.bouncycastle.asn1.tsp.TimeStampReq;
import org.bouncycastle.asn1.tsp.TimeStampResp;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import dev.mieser.tsa.domain.ResponseStatus;
import dev.mieser.tsa.domain.TimeStampValidationResult;
import dev.mieser.tsa.rest.domain.ErrorResponse;
import dev.mieser.tsa.rest.domain.TsaMediaType;
import dev.mieser.tsa.testutil.CertificateGenerator;
import dev.mieser.tsa.testutil.TestKeyLoader;
import io.quarkus.hibernate.validator.runtime.jaxrs.ViolationReport;
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
@TestTransaction
public class TsaResourceTest {

    private static final String FILE_NAME = "file";

    @Nested
    class Sign {

        @Test
        void returnsResponseOnValidRequest() throws Exception {
            // given
            byte[] sha256Digest = MessageDigest.getInstance("SHA-256").digest("test".getBytes(UTF_8));
            var messageImprint = new MessageImprint(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256), sha256Digest);
            byte[] asnEncodedRequest = new TimeStampReq(messageImprint,
                new ASN1ObjectIdentifier("1.3.3.7"),
                new ASN1Integer(BigInteger.TEN),
                ASN1Boolean.TRUE,
                null).getEncoded();

            // when
            byte[] issuedResponse = RestAssured.given()
                .contentType(TsaMediaType.TIMESTAMP_QUERY)
                .accept(TsaMediaType.TIMESTAMP_REPLY)
                .body(asnEncodedRequest)
                .post("/sign")
                .then().assertThat()
                .statusCode(200)
                .and().extract().body().asByteArray();

            // then
            assertThat(TimeStampResp.getInstance(issuedResponse).getStatus().getStatus())
                .isEqualTo(ResponseStatus.GRANTED.getValue());
        }

        @Test
        void returnsBadRequestWhenRequestIsInvalid() {
            RestAssured.given()
                .contentType(TsaMediaType.TIMESTAMP_QUERY)
                .accept(TsaMediaType.TIMESTAMP_REPLY)
                .body("I'm a TSP request, trust me!".getBytes(UTF_8))
                .post("/sign")
                .then().assertThat()
                .statusCode(400);
        }

    }

    @Nested
    class Validate {

        @Test
        void returnsValidatedResponse() throws Exception {
            // given
            byte[] sha256Digest = MessageDigest.getInstance("SHA-256").digest("test".getBytes(UTF_8));
            var messageImprint = new MessageImprint(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256), sha256Digest);
            byte[] asnEncodedRequest = new TimeStampReq(messageImprint,
                new ASN1ObjectIdentifier("1.3.3.7"),
                new ASN1Integer(BigInteger.TEN),
                ASN1Boolean.TRUE,
                null).getEncoded();

            byte[] issuedResponse = RestAssured.given()
                .contentType(TsaMediaType.TIMESTAMP_QUERY)
                .accept(TsaMediaType.TIMESTAMP_REPLY)
                .body(asnEncodedRequest)
                .post("/sign")
                .then().assertThat()
                .statusCode(200)
                .and().extract().body().asByteArray();

            // when / then
            TimeStampValidationResult actual = RestAssured.given()
                .contentType(TsaMediaType.TIMESTAMP_REPLY)
                .accept(MediaType.APPLICATION_JSON)
                .body(issuedResponse)
                .put("/validate")
                .then().assertThat()
                .statusCode(200)
                .and().extract().body().as(TimeStampValidationResult.class);

            assertSoftly(softly -> {
                softly.assertThat(actual.getStatus()).isEqualTo(ResponseStatus.GRANTED);
                softly.assertThat(actual.getStatusString()).isEqualTo("Operation Okay");
                softly.assertThat(actual.getFailureInfo()).isNull();
                softly.assertThat(actual.getGenerationTime()).isNotNull();
                softly.assertThat(actual.getSerialNumber()).isNotNull();
                softly.assertThat(actual.getNonce()).isEqualTo(BigInteger.TEN);
                softly.assertThat(actual.getHashAlgorithmIdentifier()).isEqualTo(NISTObjectIdentifiers.id_sha256.getId());
                softly.assertThat(actual.getHash()).isEqualTo(sha256Digest);
                softly.assertThat(actual.getSigningCertificateIdentifier()).isNotNull();
                softly.assertThat(actual.getSigningCertificateInformation()).isNotNull();
                softly.assertThat(actual.isSignatureValid()).isEqualTo(true);
            });
        }

        @Test
        void returnsBadRequestWhenRequestBodyDoesNotContainValidTspResponse() {
            RestAssured.given()
                .contentType(TsaMediaType.TIMESTAMP_REPLY)
                .accept(MediaType.APPLICATION_JSON)
                .body("nonsense".getBytes(UTF_8))
                .put("/validate")
                .then().assertThat()
                .statusCode(400);
        }

    }

    @Nested
    class ValidateWithCertificate {

        @Test
        void returnsBadRequestWhenPartIsMissing() {
            ViolationReport violationReport = RestAssured.given()
                .accept(MediaType.APPLICATION_JSON)
                .multiPart("unknownPart", "part".getBytes())
                .put("/validate-with-certificate")
                .then().assertThat()
                .statusCode(400)
                .and()
                .extract().as(ViolationReport.class);

            assertThat(violationReport.getViolations())
                .extracting(ViolationReport.Violation::getMessage)
                .containsExactlyInAnyOrder("x509Certificate part is missing.", "response part is missing.");
        }

        @Test
        void returnsBadRequestWhenResponseIsInvalid() throws Exception {
            ErrorResponse errorResponse = RestAssured.given()
                .accept(MediaType.APPLICATION_JSON)
                .multiPart("response", FILE_NAME, "invalid".getBytes())
                .multiPart("x509Certificate", FILE_NAME, TestKeyLoader.loadEcCertificate().getEncoded())
                .put("/validate-with-certificate")
                .then().assertThat()
                .statusCode(400)
                .and()
                .extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo("Could not parse TSP response");
        }

        @Test
        void returnsBadRequestWhenCertificateIsInvalid() throws IOException {
            ErrorResponse errorResponse = RestAssured.given()
                .accept(MediaType.APPLICATION_JSON)
                .multiPart("response", FILE_NAME, readValidTspResponse())
                .multiPart("x509Certificate", FILE_NAME, "invalid".getBytes())
                .put("/validate-with-certificate")
                .then().assertThat()
                .statusCode(400)
                .and()
                .extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).contains("Could not parse certificate");
        }

        @Test
        void returnsBadRequestWhenCertificateUsesUnsupportedPublicKeyAlgorithm() throws Exception {
            ErrorResponse errorResponse = RestAssured.given()
                .accept(MediaType.APPLICATION_JSON)
                .multiPart("response", FILE_NAME, readValidTspResponse())
                .multiPart("x509Certificate", FILE_NAME, CertificateGenerator.createEd25519Certificate().getEncoded())
                .put("/validate-with-certificate")
                .then().assertThat()
                .statusCode(400)
                .and()
                .extract().as(ErrorResponse.class);

            assertThat(errorResponse.message()).isEqualTo("Unsupported public key algorithm 'EdDSA'.");
        }

        @Test
        void validatesSpecifiedRequest() throws Exception {
            TimeStampValidationResult result = RestAssured.given()
                .accept(MediaType.APPLICATION_JSON)
                .multiPart("response", FILE_NAME, readValidTspResponse())
                .multiPart("x509Certificate", FILE_NAME, TestKeyLoader.loadEcCertificate().getEncoded())
                .put("/validate-with-certificate")
                .then().assertThat()
                .statusCode(200)
                .and()
                .extract().as(TimeStampValidationResult.class);

            assertThat(result.isSignatureValid()).isFalse();
        }

        private byte[] readValidTspResponse() throws IOException {
            try (var tspResponse = getClass()
                .getResourceAsStream("/dev/mieser/tsa/signing/impl/digicert-response-2023-08-13.asn1")) {
                return tspResponse.readAllBytes();
            }
        }

    }

}
