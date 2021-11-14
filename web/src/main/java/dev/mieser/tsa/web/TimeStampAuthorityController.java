package dev.mieser.tsa.web;

import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.integration.api.IssueTimeStampService;
import dev.mieser.tsa.signing.api.exception.InvalidTspRequestException;
import dev.mieser.tsa.signing.api.exception.TspResponseException;
import dev.mieser.tsa.signing.api.exception.UnknownHashAlgorithmException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

@Transactional
@RestController
@RequiredArgsConstructor
public class TimeStampAuthorityController {

    private final IssueTimeStampService issueTimeStampService;

    @PostMapping(
            consumes = "application/timestamp-query",
            produces = "application/timestamp-reply"
    )
    public ResponseEntity<byte[]> sign(InputStream requestInputStream) {
        TimestampResponseData responseData = issueTimeStampService.signTimestampRequest(requestInputStream);
        return ResponseEntity.ok(responseData.getAsnEncoded());
    }

    @ExceptionHandler({InvalidTspRequestException.class, UnknownHashAlgorithmException.class})
    public ResponseEntity<?> handleRequestExceptions() {
        return ResponseEntity.badRequest()
                .build();
    }

    @ExceptionHandler(TspResponseException.class)
    public ResponseEntity<?> handleServerExceptions() {
        return ResponseEntity.internalServerError()
                .build();
    }

}
