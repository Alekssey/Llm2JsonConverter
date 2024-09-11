package ru.mpei.llmconverter.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class Scheme {
    private boolean locked = false;
    private int zoom = 1;
    private int offsetX = 0;
    private int offsetY = 0;
    private int version = 0;
    private int metaSchemeChangeSetId = 0;
    private String metaSchemeVersion = "2.1.4";
    private Map<String, Node> nodes = new HashMap<>();
    private Map<String, Link> links = new HashMap<>();
    private List<String> substations = new ArrayList<>();
    private List<String> transmissionLines = new ArrayList<>();
}
