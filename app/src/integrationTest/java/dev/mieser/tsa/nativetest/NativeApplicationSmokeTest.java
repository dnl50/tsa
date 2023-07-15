package dev.mieser.tsa.nativetest;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.math.BigInteger;
import java.security.MessageDigest;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.tsp.MessageImprint;
import org.bouncycastle.asn1.tsp.TimeStampReq;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.junit.jupiter.api.Test;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusIntegrationTest;
import io.restassured.RestAssured;

@QuarkusIntegrationTest
@QuarkusTestResource(KeystoreResourceLifecycleManager.class)
public class NativeApplicationSmokeTest {

    @Test
    void signRequest() throws Exception {
        // given
        byte[] sha256Digest = MessageDigest.getInstance("SHA-256").digest("test".getBytes(UTF_8));
        var messageImprint = new MessageImprint(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256), sha256Digest);
        byte[] asnEncodedRequest = new TimeStampReq(messageImprint,
            new ASN1ObjectIdentifier("1.3.3.7"),
            new ASN1Integer(BigInteger.TEN),
            ASN1Boolean.TRUE,
            null).getEncoded();

        // when / then
        RestAssured.given()
            .contentType("application/timestamp-query")
            .accept("application/timestamp-reply")
            .body(asnEncodedRequest)
            .post("/tsa/sign")
            .then()
            .statusCode(200);
    }

}
