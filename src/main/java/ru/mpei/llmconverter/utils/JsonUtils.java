package ru.mpei.llmconverter.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class JsonUtils {
    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static <T> T parserJSON(String json, Class<T> clazz){
        T object = null;
        try {
            object = mapper.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return object;
    }

    public static <T> T parseFromFile(String path, Class<T> clazz){
        String json = null;
        T object = null;
        try {
            json = new String(Files.readAllBytes(Paths.get(path)));
            object = mapper.readValue(json, clazz);
        } catch (IOException e) {
            log.error("Configuration with path {} not correction", path);
            throw new RuntimeException(e);
        }
        return object;
    }

    public static String writeAsJson(Object obj) {
        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String readJsonFromFile(String filePath) {
        try {
            String json = new String(Files.readAllBytes(Paths.get(filePath)));
            return json;
        } catch (IOException e) {
            log.error("Can not read file by path: {}", filePath);
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public static <T> void writeJsonToFile(String filepath, T objectToWrite) {
        mapper.writeValue(new File(filepath), objectToWrite);
    }
}
