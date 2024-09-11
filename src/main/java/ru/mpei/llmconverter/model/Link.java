package ru.mpei.llmconverter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Link {
    private String id;
    private String alignmentType;
    private boolean locked;
    private boolean selected;
    private String source;
    private String target;
    private String sourcePort;
    private String targetPort;
    private List<Point> points;
}
