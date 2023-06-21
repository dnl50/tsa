package dev.mieser.tsa.rest;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;

import dev.mieser.tsa.quarkus.TsaTestProfile;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.restassured.RestAssured;

@QuarkusTest
@TestProfile(TsaTestProfile.class)
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
            throw new IllegalStateException(String.format("System Property '%s' not set.", TARGET_FILE_PATH_PROPERTY));
        }

        FileUtils.writeByteArrayToFile(new File(targetFilePath), content);
    }

}
