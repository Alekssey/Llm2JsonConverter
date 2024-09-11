package ru.mpei.llmconverter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Port {
    private String id;
    private String libId;
    private boolean locked;
    private boolean selected;
    private String parentNode;
    private Coords coords;
    private String alignment;
    private List<String> links;
    private List<Object> points;
}
