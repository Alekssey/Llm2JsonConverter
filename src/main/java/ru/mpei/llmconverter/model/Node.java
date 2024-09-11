package ru.mpei.llmconverter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Node {
    protected String id;
    protected String voltageLevelId;
    protected String libEquipmentId;
    protected int hour;
    protected Coords coords;
    protected Dimensions dimensions;
    protected Map<String, String> fields;
    protected Map<String, Map<String, String>> controls;
    protected List<Port> ports;
    protected String name;
}
