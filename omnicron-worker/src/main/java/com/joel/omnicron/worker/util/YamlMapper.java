package com.joel.omnicron.worker.util;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public final class YamlMapper {
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());

    private YamlMapper() {
    }

    public static <T> T readValue(String yaml, Class<T> type) throws IOException {
        return MAPPER.readValue(yaml, type);
    }
}
