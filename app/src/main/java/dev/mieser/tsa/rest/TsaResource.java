package dev.mieser.tsa.rest;

import java.io.InputStream;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import dev.mieser.tsa.domain.TimeStampValidationResult;
import dev.mieser.tsa.integration.api.IssueTimeStampService;
import dev.mieser.tsa.integration.api.ValidateTimeStampResponseService;
import dev.mieser.tsa.rest.domain.ErrorResponse;
import dev.mieser.tsa.rest.domain.HttpStatusCode;
import dev.mieser.tsa.rest.domain.TsaMediaType;
import dev.mieser.tsa.rest.domain.ValidationRequestForm;
import dev.mieser.tsa.signing.api.exception.InvalidCertificateException;
import dev.mieser.tsa.signing.api.exception.InvalidTspRequestException;
import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;
import io.quarkus.hibernate.validator.runtime.jaxrs.ViolationReport;

@Transactional
@Path("/")
@RequiredArgsConstructor
public class TsaResource {

    private final IssueTimeStampService issueTimeStampService;

    private final ValidateTimeStampResponseService validateTimeStampResponseService;

    @POST
    @Path("/sign")
    @Consumes(TsaMediaType.TIMESTAMP_QUERY)
    @Produces(TsaMediaType.TIMESTAMP_REPLY)
    @APIResponses({
        @APIResponse(
                     responseCode = HttpStatusCode.OK,
                     description = "When the time stamp query was parsed successfully and a response was generated."),
        @APIResponse(
                     responseCode = HttpStatusCode.BAD_REQUEST,
                     description = "When the time stamp query uses an unsupported hash algorithm or the request cannot be parsed.")
    })
    public byte[] sign(InputStream timestampQueryStream) throws InvalidTspRequestException {
        return issueTimeStampService.signTimestampRequest(timestampQueryStream).getAsnEncoded();
    }

    @PUT
    @Path("/validate")
    @Consumes(TsaMediaType.TIMESTAMP_REPLY)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses({
        @APIResponse(
                     responseCode = HttpStatusCode.OK,
                     description = "When the time stamp response was parsed successfully."),
        @APIResponse(
                     responseCode = HttpStatusCode.BAD_REQUEST,
                     description = "When the response cannot be parsed.")
    })
    public TimeStampValidationResult validate(InputStream timestampResponse) throws InvalidTspResponseException {
        return validateTimeStampResponseService.validateTimeStampResponse(timestampResponse);
    }

    @PUT
    @Path("/validate-with-certificate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses({
        @APIResponse(
                     responseCode = HttpStatusCode.OK,
                     description = "When the time stamp response was parsed successfully."),
        @APIResponse(
                     responseCode = HttpStatusCode.BAD_REQUEST,
                     description = "When a request part is missing, the response/certificate cannot be parsed or the certificate uses an unsupported public key algorithm.",
                     content = @Content(schema = @Schema(anyOf = { ErrorResponse.class, ViolationReport.class })))
    })
    public TimeStampValidationResult validateWithCertificate(
        @Valid ValidationRequestForm validationRequestForm) throws InvalidTspResponseException, InvalidCertificateException {
        return validateTimeStampResponseService.validateTimeStampResponse(validationRequestForm.getResponse(),
            validationRequestForm.getX509Certificate());
    }

}
