package ru.mpei.llmconverter.service;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.mpei.llmconverter.builders.SchemeBuilder;
import ru.mpei.llmconverter.model.Scheme;
import ru.mpei.llmconverter.utils.JsonUtils;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.Files.*;

@Service
@Slf4j
public class ApiService {
    @Autowired
    private RequestToCimConverterService cimConverterService;
    @Autowired
    private SchemeBuilder schemeBuilder;
    @Value("${folder.path.json}")
    private String jsonFolderPath;
    @Value("${folder.path.cim}")
    private String cimFolderPath;
    @Value("${folder.path.downloads}")
    private String downloadsFolderPath;
//    private String baseJsonOutFilepath = "src/main/resources/Json/";
    private int fileNumber = 0;

    @PostConstruct
    private void scanFiles() {
        try {
            List<Path> files = walk(Paths.get(this.jsonFolderPath))
                    .filter(Files::isRegularFile)
                    .toList();
            this.fileNumber = files.size() + 1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

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
        JsonUtils.writeJsonToFile(this.downloadsFolderPath + "generated_scheme.json", scheme);
        JsonUtils.writeJsonToFile(this.jsonFolderPath + "scheme_" + this.fileNumber + ".json", scheme);
        String cim = this.cimConverterService.sendSchemeForConvertingToCim(scheme);

        this.writeCimToFile(this.downloadsFolderPath, "generated_scheme_cim.xml", cim);
        this.writeCimToFile(this.cimFolderPath, "scheme_" + this.fileNumber + ".xml", cim);

        log.info("scheme generated");

        this.fileNumber ++;

    }

    private void writeCimToFile(String path, String fileName, String content) {
        try {
            FileWriter writer = new FileWriter(path + fileName, false);
            writer.write(content);
            writer.close();

        } catch (IOException e) {
            System.out.println("Возникла ошибка во время записи, проверьте данные.");
        }
    }
}
