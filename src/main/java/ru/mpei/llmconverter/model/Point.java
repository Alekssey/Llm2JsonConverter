package ru.mpei.llmconverter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Point {
    private String id;
    private boolean locked;
    private boolean selected;
    private Coords coords;
}
