package dev.mieser.tsa.web.config;

import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import dev.mieser.tsa.web.formatter.HexJsonSerializer;

@Configuration
class JacksonObjectMapperConfiguration implements Jackson2ObjectMapperBuilderCustomizer {

    @Override
    public void customize(Jackson2ObjectMapperBuilder jacksonObjectMapperBuilder) {
        jacksonObjectMapperBuilder.modules(new JavaTimeModule());
        jacksonObjectMapperBuilder.serializers(new HexJsonSerializer());
    }

}
