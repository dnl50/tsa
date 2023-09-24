package dev.mieser.tsa.websocket.encoder;

import java.io.Writer;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.EncodeException;
import jakarta.websocket.Encoder;
import jakarta.websocket.server.ServerEndpoint;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.quarkus.arc.Unremovable;

/**
 * Jakarta Websocket {@link Encoder.TextStream text stream encoder} using the configured {@link ObjectMapper} from the
 * CDI context.
 * 
 * @implNote The bean must be annotated with {@link Unremovable} since it is referenced indirectly through the
 * {@link ServerEndpoint#encoders()} list. The bean will otherwise be removed since Quarkus thinks the bean is unused.
 */
@Unremovable
@ApplicationScoped
@RequiredArgsConstructor
public class JsonEncoder implements Encoder.TextStream<Object> {

    private final ObjectMapper objectMapper;

    @Override
    public void encode(Object object, Writer writer) throws EncodeException {
        try {
            objectMapper.writeValue(writer, object);
        } catch (Exception e) {
            throw new EncodeException(object, "Failed to serialize object.", e);
        }
    }

}
