package dev.mieser.tsa.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.RestAssured;

@QuarkusTest
public class ExportOpenApiDocumentationTest {

    private static final String TARGET_FILE_PATH_PROPERTY = "openapi.specification.target-file";

    @Test
    void saveOpenApiSpecification() throws Exception {
        byte[] openApiSpecification = RestAssured.given()
            .when()
            .param("format", "json")
            .get("/q/openapi")
            .then()
            .statusCode(200)
            .and().extract().body().asByteArray();

        writeSpecificationFile(openApiSpecification);
    }

    private void writeSpecificationFile(byte[] content) throws IOException {
        String targetFilePath = System.getProperty(TARGET_FILE_PATH_PROPERTY);
        if (StringUtils.isBlank(targetFilePath)) {
            return;
        }

        Files.write(Paths.get(targetFilePath), content);
    }

}
