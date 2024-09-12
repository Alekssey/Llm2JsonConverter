package ru.mpei.llmconverter;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import ru.mpei.llmconverter.utils.JsonUtils;

import java.awt.image.DataBuffer;
import java.util.*;

public class SimpleTests {

    @Test
    void test1() {
        int count = 10;
        System.out.println(count % 3);
    }

    @Test
    void findAllCircuitTypes() {
        String json = JsonUtils.readJsonFromFile("src/test/resources/dataset_en_refactored.json");
        DatasetHolder dataset = JsonUtils.parserJSON(json, DatasetHolder.class);
        List<String> schemeTypes = new ArrayList<>();

        for (DataPair pair: dataset.dataset()) {
            for(String component: pair.json_output().split("\\|")) {
                Map<String, String> parsedComponents = new HashMap<>();
                for(String attr: component.substring(1, component.length() - 1).replace(" ", "").split(";")) {
                    List<String> kv = List.of(attr.split(":"));
                    parsedComponents.put(kv.get(0), kv.get(1));
                }
                if(parsedComponents.get("accessory").equals("side")) {
                    if (!schemeTypes.contains(parsedComponents.get("circuit"))) {
                        schemeTypes.add(parsedComponents.get("circuit"));
                    }
                }
            }
        }
        System.err.println(schemeTypes);
    }

    @SneakyThrows
    @Test
    void consoleInput() {
        for (int i = 0; i < 5; i++) {
            System.in.read();
        }
    }
    private record DataPair(String input_text, String json_output) {}
    private record DatasetHolder(List<DataPair> dataset) {}
}
