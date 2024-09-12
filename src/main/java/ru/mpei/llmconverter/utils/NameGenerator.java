package ru.mpei.llmconverter.utils;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class NameGenerator {
    private List<String> generatedNames = new ArrayList<>();

    public void clearNamesList() {
        this.generatedNames.clear();
    }

    public String generateName(String nodeType) {
        int i = 0;
        String name = nodeType + "_" + i;
        while(this.generatedNames.contains(name)) {
            name = nodeType + "_" + ++i;
        }
        this.generatedNames.add(name);
        return name;
    }

    public void reset() {
        this.generatedNames.clear();
    }
}
