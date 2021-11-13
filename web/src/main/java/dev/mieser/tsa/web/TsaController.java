package dev.mieser.tsa.web;

import dev.mieser.tsa.domain.TimestampResponseData;
import dev.mieser.tsa.integration.api.PersistentTsa;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

@Transactional
@RestController
@RequiredArgsConstructor
public class TsaController {

    private final PersistentTsa persistentTsa;

    @PostMapping(
            consumes = "application/timestamp-query",
            produces = "application/timestamp-reply"
    )
    public ResponseEntity<byte[]> sign(InputStream requestInputStream) {
        TimestampResponseData responseData = persistentTsa.signTimestampRequest(requestInputStream);
        return ResponseEntity.ok(responseData.getAsnEncoded());
    }

}
