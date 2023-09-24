package dev.mieser.tsa.rest;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.ZonedDateTime;
import java.util.Comparator;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response.Status;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import dev.mieser.tsa.domain.ResponseStatus;
import dev.mieser.tsa.domain.TimeStampRequestData;
import dev.mieser.tsa.domain.TimeStampResponseData;
import dev.mieser.tsa.persistence.api.Page;
import dev.mieser.tsa.persistence.api.TspResponseDataRepository;
import dev.mieser.tsa.signing.config.HashAlgorithm;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;

@QuarkusTest
class ResponseHistoryResourceTest {

    private final TspResponseDataRepository tspResponseDataRepository;

    @Inject
    ResponseHistoryResourceTest(TspResponseDataRepository tspResponseDataRepository) {
        this.tspResponseDataRepository = tspResponseDataRepository;
    }

    @AfterEach
    void tearDown() {
        QuarkusTransaction.requiringNew().run(tspResponseDataRepository::deleteAll);
    }

    @Test
    void queryByIdReturnsNotFoundWhenResponseDoesNotExist() {
        given().accept(ContentType.JSON)
            .get("/history/responses/-1")
            .then().assertThat()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void queryById() {
        // given
        TimeStampResponseData savedResponse = saveGeneratedResponseInNewTransaction(ZonedDateTime.now());

        // when / then
        TimeStampResponseData returnedResponse = given()
            .accept(ContentType.JSON)
            .get("/history/responses/{id}", savedResponse.getId())
            .then().assertThat()
            .statusCode(Status.OK.getStatusCode())
            .extract()
            .as(TimeStampResponseData.class);

        assertThat(returnedResponse).usingComparatorForType(Comparator.comparing(ZonedDateTime::toInstant), ZonedDateTime.class)
            .usingRecursiveComparison()
            .isEqualTo(savedResponse);
    }

    @Test
    void queryAllReturnsPageWithExpectedSort() {
        // given
        var now = ZonedDateTime.now();
        var latestResponse = saveGeneratedResponseInNewTransaction(now.plusMinutes(15L));
        var secondLatestResponse = saveGeneratedResponseInNewTransaction(now.plusMinutes(10L));
        var thirdLatestResponse = saveGeneratedResponseInNewTransaction(now.plusMinutes(5L));

        // when / then
        Page<TimeStampResponseData> returnedPage = given()
            .accept(ContentType.JSON)
            .param("page", 1)
            .param("size", 2)
            .param("sort", "receptionTime,desc")
            .get("/history/responses")
            .then().assertThat()
            .statusCode(Status.OK.getStatusCode())
            .and()
            .extract().as(new TypeRef<>() {});

        assertSoftly(softly -> {
            softly.assertThat(returnedPage.pageNumber()).isEqualTo(1);
            softly.assertThat(returnedPage.size()).isEqualTo(2);

            softly.assertThat(returnedPage.content())
                .usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration.builder()
                    .withComparatorForType(Comparator.comparing(ZonedDateTime::toInstant), ZonedDateTime.class)
                    .build())
                .containsSubsequence(latestResponse, secondLatestResponse)
                .doesNotContain(thirdLatestResponse);
        });
    }

    @Test
    void deleteByIdReturnsNotFoundWhenResponseDoesNotExist() {
        given().accept(ContentType.JSON)
            .delete("/history/responses/-1")
            .then().assertThat()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void deleteByIdDeletesResponse() {
        // given
        TimeStampResponseData responseToDelete = saveGeneratedResponseInNewTransaction(ZonedDateTime.now());

        // when
        given().accept(ContentType.JSON)
            .delete("/history/responses/{id}", responseToDelete.getId())
            .then().assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // then
        given().accept(ContentType.JSON)
            .get("/history/responses/{id}", responseToDelete.getId())
            .then().assertThat()
            .statusCode(Status.NOT_FOUND.getStatusCode());
    }

    @Test
    void deleteAll() {
        // given
        saveGeneratedResponseInNewTransaction(ZonedDateTime.now());

        // when
        given().accept(ContentType.JSON)
            .delete("/history/responses")
            .then().assertThat()
            .statusCode(Status.NO_CONTENT.getStatusCode());

        // then
        Page<TimeStampResponseData> returnedPage = given().accept(ContentType.JSON)
            .get("/history/responses")
            .then().assertThat()
            .statusCode(Status.OK.getStatusCode())
            .and()
            .extract()
            .as(new TypeRef<>() {});

        assertThat(returnedPage.totalElements()).isZero();
    }

    private TimeStampResponseData saveGeneratedResponseInNewTransaction(ZonedDateTime receptionTime) {
        var minimalRequest = TimeStampRequestData
            .builder(HashAlgorithm.SHA256.getObjectIdentifier(), "sha-256".getBytes(), "asn-encoded".getBytes())
            .build();
        var minimalResponse = TimeStampResponseData
            .builder(ResponseStatus.REJECTION, receptionTime, minimalRequest, "asn-encoded".getBytes()).build();

        return QuarkusTransaction.requiringNew().call(() -> tspResponseDataRepository.save(minimalResponse));
    }

}
