package dev.mieser.tsa.web.dto;

import javax.validation.constraints.NotEmpty;

import lombok.*;
import dev.mieser.tsa.web.validator.Base64Encoded;

/**
 * DTO encapsulating information about TSP responses.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TimeStampResponseDto {

    /**
     * The Base64 encoding of an ASN.1 DER encoded TSP response.
     */
    @NotEmpty(message = "{dev.mieser.tsa.web.dto.TimeStampResponseDto.notEmpty}")
    @Base64Encoded
    private String base64EncodedResponse;

}
