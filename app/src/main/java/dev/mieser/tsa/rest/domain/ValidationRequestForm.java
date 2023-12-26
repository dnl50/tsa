package dev.mieser.tsa.rest.domain;

import java.io.InputStream;

import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.FormParam;

import lombok.Data;

@Data
public class ValidationRequestForm {

    @NotNull(message = "response {dev.mieser.tsa.multipart.missing.suffix}")
    @FormParam("response")
    private InputStream response;

    @NotNull(message = "x509Certificate {dev.mieser.tsa.multipart.missing.suffix}")
    @FormParam("x509Certificate")
    private InputStream x509Certificate;

}
