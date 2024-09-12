package ru.mpei.llmconverter.builders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mpei.llmconverter.builders.model.SwitchgearBuilderOutDto;
import ru.mpei.llmconverter.model.*;
import ru.mpei.llmconverter.utils.NameGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SchemeBuilder {
    @Autowired
    private ElementsBuilder elBuilder;
    @Autowired
    private LinkBuilder linker;
    @Autowired
    private SwitchgearBuilder switchgearBuilder;
    @Autowired
    private NameGenerator nameGenerator;

    public Scheme buildScheme(List<Map<String, String>> voltageSides, List<Map<String, String>> equipments) {
        Scheme scheme =  new Scheme();

        int numberOfTransformers = Integer.parseInt(equipments.get(0).get("quantity"));
        List<Node> transformers = new ArrayList<>();
        List<List<SwitchgearBuilderOutDto>> switchgears = new ArrayList<>();
        double x = -100, y = 0;
        String[] classes = equipments.get(0).get("class").replace("kV", "").replace(" ", "").split("/");

        for (int i = 0; i < numberOfTransformers; i++) {
            Node tr;
            if (classes.length == 2) {
                tr = this.elBuilder.buildTwoWindingTransformer(classes[0], classes[1], x, y);
            } else {
                tr = this.elBuilder.buildThreeWindingTransformer(classes[0], classes[1], classes[2], x, y);
            }
            transformers.add(tr);
            scheme.getNodes().put(tr.getId(), tr);
            x += 100;
        }
        for (Map<String, String> sideDescription: voltageSides) {
            switch (sideDescription.get("circuit")) {
                case "quadrilateral" -> switchgears.add(this.switchgearBuilder.buildQuadrilateral(scheme, sideDescription));
                case "partitionedbusbarsystem" -> switchgears.add(this.switchgearBuilder.buildPartitionedBusBarSystem(scheme, sideDescription));
                case "3/2bussystem" -> switchgears.add(this.switchgearBuilder.build3on2(scheme, sideDescription, numberOfTransformers));
                case "4/3bussystem" -> switchgears.add(this.switchgearBuilder.build4on3(scheme, sideDescription, numberOfTransformers));
                case "hexagon" -> switchgears.add(this.switchgearBuilder.buildHexagonal(scheme, sideDescription));
            }
        }

        int portNumber = 0;
        for (String voltageClass: classes) {
            List<SwitchgearBuilderOutDto> switchgear = new ArrayList<>();
            for (List<SwitchgearBuilderOutDto> sw: switchgears) {
                if (sw.get(0).getNode().getVoltageLevelId().split("_")[1].equals(voltageClass)) {
                    switchgear = sw;
                    break;
                }
            }
            for (int i = 0; i < switchgear.size(); i++) {
                Link link = this.linker.buildLink(transformers.get(i), transformers.get(i).getPorts().get(portNumber), switchgear.get(i).getNode(), switchgear.get(i).getPort());
                scheme.getLinks().put(link.getId(), link);
            }
            portNumber ++;
        }

        this.switchgearBuilder.reset();
        this.nameGenerator.reset();
        return scheme;
    }
}
