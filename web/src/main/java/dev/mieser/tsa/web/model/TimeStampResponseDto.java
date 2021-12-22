package dev.mieser.tsa.web.model;

import dev.mieser.tsa.web.validator.Base64Encoded;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;

/**
 * DTO encapsulating information about TSP responses.
 */
@Getter
@Setter
public class TimeStampResponseDto {

    /**
     * The Base64 encoding of an ASN.1 DER encoded TSP response.
     */
    @NotEmpty
    @Base64Encoded
    private String base64EncodedResponse;

}
