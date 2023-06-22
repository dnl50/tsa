package dev.mieser.tsa.rest;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.security.MessageDigest;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.tsp.MessageImprint;
import org.bouncycastle.asn1.tsp.TimeStampReq;
import org.bouncycastle.asn1.tsp.TimeStampResp;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.junit.jupiter.api.Test;

import dev.mieser.tsa.domain.ResponseStatus;
import dev.mieser.tsa.quarkus.TsaTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;

@QuarkusTest
@TestProfile(TsaTestProfile.class)
public class TsaResourceTest {

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
            .body(asnEncodedRequest)
            .post("/tsa/sign")
            .then()
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
            .body("I'm a TSP request, trust me!".getBytes(UTF_8))
            .post("/tsa/sign")
            .then()
            .statusCode(400);
    }

    @Test
    void returnsBadRequestWhenUnknownHashAlgorithmIsUsed() throws Exception {
        // given
        var md5Imprint = new MessageImprint(new AlgorithmIdentifier(PKCSObjectIdentifiers.md5),
            "098f6bcd4621d373cade4e832627b4f6".getBytes(UTF_8));
        var md5Request = new TimeStampReq(md5Imprint, null, null, ASN1Boolean.FALSE, null);

        // when / then
        RestAssured.given()
            .contentType("application/timestamp-query")
            .body(md5Request.getEncoded())
            .post("/tsa/sign")
            .then()
            .statusCode(400);
    }

}
