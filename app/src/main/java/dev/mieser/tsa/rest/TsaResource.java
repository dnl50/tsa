package dev.mieser.tsa.rest;

import java.io.InputStream;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import lombok.RequiredArgsConstructor;

import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponses;

import dev.mieser.tsa.domain.TimeStampValidationResult;
import dev.mieser.tsa.integration.api.IssueTimeStampService;
import dev.mieser.tsa.integration.api.ValidateTimeStampResponseService;
import dev.mieser.tsa.signing.api.exception.InvalidTspRequestException;
import dev.mieser.tsa.signing.api.exception.InvalidTspResponseException;
import dev.mieser.tsa.signing.api.exception.UnknownHashAlgorithmException;

@Transactional
@Path("/tsa")
@RequiredArgsConstructor
public class TsaResource {

    private final IssueTimeStampService issueTimeStampService;

    private final ValidateTimeStampResponseService validateTimeStampResponseService;

    @POST
    @Path("/sign")
    @Consumes("application/timestamp-query")
    @Produces("application/timestamp-reply")
    @APIResponses({
        @APIResponse(
                     responseCode = HttpStatusCode.OK,
                     description = "When the time stamp query was parsed successfully and a response was generated."),
        @APIResponse(
                     responseCode = HttpStatusCode.BAD_REQUEST,
                     description = "When the time stamp query uses an unsupported hash algorithm or the request cannot be parsed.")
    })
    public byte[] sign(InputStream timestampQueryStream) throws InvalidTspRequestException, UnknownHashAlgorithmException {
        return issueTimeStampService.signTimestampRequest(timestampQueryStream).getAsnEncoded();
    }

    @PUT
    @Path("/validate")
    @Consumes("application/timestamp-reply")
    @Produces(MediaType.APPLICATION_JSON)
    @APIResponses({
        @APIResponse(
                     responseCode = HttpStatusCode.OK,
                     description = "When the time stamp response was parsed successfully."),
        @APIResponse(
                     responseCode = HttpStatusCode.BAD_REQUEST,
                     description = "When the time stamp query uses an unsupported hash algorithm or the response cannot be parsed.")
    })
    public TimeStampValidationResult validate(
        InputStream timestampResponse) throws InvalidTspResponseException, UnknownHashAlgorithmException {
        return validateTimeStampResponseService.validateTimeStampResponse(timestampResponse);
    }

}
