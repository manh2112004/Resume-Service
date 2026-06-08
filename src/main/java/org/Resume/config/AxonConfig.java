package org.Resume.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.axonframework.serialization.Serializer;
import org.axonframework.serialization.json.JacksonSerializer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AxonConfig {

    @Bean
    @Primary
    public Serializer defaultSerializer() {
        ObjectMapper axonObjectMapper = new ObjectMapper();
        axonObjectMapper.findAndRegisterModules();
        return JacksonSerializer.builder()
                .objectMapper(axonObjectMapper)
                .build();
    }

    @Bean
    @Qualifier("messageSerializer")
    public Serializer messageSerializer() {
        ObjectMapper axonObjectMapper = new ObjectMapper();
        axonObjectMapper.findAndRegisterModules();
        return JacksonSerializer.builder()
                .objectMapper(axonObjectMapper)
                .defaultTyping()
                .build();
    }
}
