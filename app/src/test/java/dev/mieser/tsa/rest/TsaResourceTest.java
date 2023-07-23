package dev.mieser.tsa.rest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

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
import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
@TestTransaction
public class TsaResourceTest {

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
                .contentType("application/timestamp-query")
                .accept("application/timestamp-reply")
                .body(asnEncodedRequest)
                .post("/tsa/sign")
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
                .contentType("application/timestamp-query")
                .accept("application/timestamp-reply")
                .body("I'm a TSP request, trust me!".getBytes(UTF_8))
                .post("/tsa/sign")
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
                .contentType("application/timestamp-query")
                .accept("application/timestamp-reply")
                .body(asnEncodedRequest)
                .post("/tsa/sign")
                .then().assertThat()
                .statusCode(200)
                .and().extract().body().asByteArray();

            // when / then
            TimeStampValidationResult actual = RestAssured.given()
                .contentType("application/timestamp-reply")
                .accept(MediaType.APPLICATION_JSON)
                .body(issuedResponse)
                .put("/tsa/validate")
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
                softly.assertThat(actual.isSignedByThisTsa()).isEqualTo(true);
            });
        }

        @Test
        void returnsBadRequestWhenRequestBodyDoesNotContainValidTspResponse() {
            RestAssured.given()
                .contentType("application/timestamp-reply")
                .accept(MediaType.APPLICATION_JSON)
                .body("nonsense".getBytes(UTF_8))
                .put("/tsa/validate")
                .then().assertThat()
                .statusCode(400);
        }

    }

}
