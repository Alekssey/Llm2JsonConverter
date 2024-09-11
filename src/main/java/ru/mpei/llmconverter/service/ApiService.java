package ru.mpei.llmconverter.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.mpei.llmconverter.builders.SchemeBuilder;
import ru.mpei.llmconverter.model.Scheme;
import ru.mpei.llmconverter.utils.JsonUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class ApiService {
    @Autowired
    private SchemeBuilder schemeBuilder;

    public void extract_data(String llmResponse) {
        List<Map<String, String>> voltageSides = new ArrayList<>();
        List<Map<String, String>> equipments = new ArrayList<>();

        for(String component: llmResponse.split("\\|")) {
            Map<String, String> parsedComponents = new HashMap<>();
            for(String attr: component.substring(1, component.length() - 1).replace(" ", "").split(";")) {
                List<String> kv = List.of(attr.split(":"));
                parsedComponents.put(kv.get(0), kv.get(1));
            }
            if(parsedComponents.get("accessory").equals("side")) {
                voltageSides.add(parsedComponents);
            } else {
                equipments.add(parsedComponents);
            }
        }

        Scheme scheme = this.schemeBuilder.buildScheme(voltageSides, equipments);
        log.error(JsonUtils.writeAsJson(scheme));
        JsonUtils.writeJsonToFile("C:\\Users\\Aleksey\\Downloads\\generated_scheme.json", scheme);
    }
}
