package dev.mieser.tsa.websocket;

import static io.restassured.RestAssured.given;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigInteger;
import java.net.URI;
import java.security.MessageDigest;

import jakarta.inject.Inject;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Session;
import jakarta.ws.rs.core.Response.Status;

import org.bouncycastle.asn1.ASN1Boolean;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.nist.NISTObjectIdentifiers;
import org.bouncycastle.asn1.tsp.MessageImprint;
import org.bouncycastle.asn1.tsp.TimeStampReq;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.persistence.api.Page;
import dev.mieser.tsa.rest.domain.TsaMediaType;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;

@QuarkusTest
public class TimeStampNotificationSocketTest {

    private final ObjectMapper objectMapper;

    @TestHTTPResource("/history/responses")
    URI websocketUri;

    @Inject
    TimeStampNotificationSocketTest(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Test
    void signedResponsesArePropagatedViaWebsocket() throws Exception {
        var client = new CachingHistoryWebsocketClient(objectMapper);

        try (Session ignored = ContainerProvider.getWebSocketContainer().connectToServer(client, websocketUri)) {
            var expectedResponse = signRequestViaHttpEndpoint();
            assertThat(client.getReceivedMessages()).containsExactly(expectedResponse);
        }
    }

    private TimeStampResponseData signRequestViaHttpEndpoint() throws Exception {
        byte[] sha256Digest = MessageDigest.getInstance("SHA-256").digest("test".getBytes(UTF_8));
        var messageImprint = new MessageImprint(new AlgorithmIdentifier(NISTObjectIdentifiers.id_sha256), sha256Digest);
        byte[] asnEncodedRequest = new TimeStampReq(messageImprint,
            new ASN1ObjectIdentifier("1.3.3.7"),
            new ASN1Integer(BigInteger.TEN),
            ASN1Boolean.FALSE,
            null).getEncoded();

        given()
            .contentType(TsaMediaType.TIMESTAMP_QUERY)
            .accept(TsaMediaType.TIMESTAMP_REPLY)
            .body(asnEncodedRequest)
            .post("/sign")
            .then().assertThat()
            .statusCode(Status.OK.getStatusCode());

        return given()
            .accept(ContentType.JSON)
            .param("size", 1)
            .param("sort", "receptionTime,desc")
            .get("/history/responses")
            .then().assertThat()
            .statusCode(Status.OK.getStatusCode())
            .and()
            .extract()
            .as(new TypeRef<Page<TimeStampResponseData>>() {})
            .content()
            .get(0);
    }

}
