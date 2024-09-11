package ru.mpei.llmconverter.builders.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.mpei.llmconverter.model.Node;
import ru.mpei.llmconverter.model.Port;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SwitchgearBuilderOutDto {
    private Node node;
    private Port port;
}
