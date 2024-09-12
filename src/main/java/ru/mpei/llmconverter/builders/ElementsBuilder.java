package ru.mpei.llmconverter.builders;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.mpei.llmconverter.model.*;
import ru.mpei.llmconverter.utils.IdGenerator;
import ru.mpei.llmconverter.utils.NameGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ElementsBuilder {
    @Autowired
    private IdGenerator idGenerator;
    @Autowired
    private NameGenerator nameGenerator;

    public ConnectionNode buildBreaker(String voltageLevel, double x, double y) {
        return this.buildBreaker(voltageLevel, x, y, 0);
    }

    public ConnectionNode buildBreaker(String voltageLevel, double x, double y, int hour) {
        String name = nameGenerator.generateName("breaker");
        String id = this.idGenerator.generateId();
        voltageLevel = voltageLevel.replace("kV", "");
        ConnectionNode breaker = new ConnectionNode(
                id,
                "KILOVOLTS_" + voltageLevel,
                "CIRCUIT_BREAKER",
                hour,
                new Coords(x, y),
                new Dimensions(60, 40),
                Map.of("SUBSTATION","noId", "NAME", name,"POSITION", "on", "RATED_CURRENT", "100"),
                Map.of("POSITION", Map.of("value", "enabled", "min", "NaN", "max", "NaN")),
                List.of(
                        this.buildPort(id, "FIRST", "TOP", x + 54.5, y + 8.5),
                        this.buildPort(id, "SECOND", "BOTTOM", x + 54.5, y + 68.5)),
                name,
                false,
                false
        );

        return breaker;
    }

    public ConnectionNode buildBus(String voltageLevel, double x, double y) {
        return this.buildBus(voltageLevel, x, y, 370);
    }

    public ConnectionNode buildBus(String voltageLevel, double x, double y, double width) {
        String name = this.nameGenerator.generateName("bus");
        voltageLevel = voltageLevel.replace("kV", "");

        ConnectionNode bus = new ConnectionNode(
                this.idGenerator.generateId(),
                "KILOVOLTS_" + voltageLevel,
                "BUS",
                0,
                new Coords(x, y),
                new Dimensions(7, width),
                Map.of("SUBSTATION", "noId", "NAME", name, "SHOULD_FREQUENCY_BE_MEASURED", "no"),
                new HashMap<>(),
                new ArrayList<>(),
                name,
                false,
                false
        );
        return bus;
    }

    public ConnectionNode buildDisconnector(String voltageLevel, double x, double y) {
        return this.buildDisconnector(voltageLevel, x, y, 0);
    }

    public ConnectionNode buildDisconnector(String voltageLevel, double x, double y, int hour) {
        String name = this.nameGenerator.generateName("disconnector");
        String id = this.idGenerator.generateId();
        voltageLevel = voltageLevel.replace("kV", "");
        ConnectionNode disconnector = new ConnectionNode(
                id,
                "KILOVOLTS_" + voltageLevel,
                "DISCONNECTOR",
                hour,
                new Coords(x, y),
                new Dimensions(55, 13),
                Map.of("SUBSTATION", "noId", "NAME", name, "POSITION", "on", "RATED_CURRENT", "100"),
                Map.of("POSITION", Map.of("value", "enabled", "min", "NaN", "max", "NaN")),
                List.of(this.buildPort(id, "FIRST", "TOP", x + 17.5, y + 11), this.buildPort(id, "SECOND", "BOTTOM", x + 17.5, y + 66)),
                name,
                false,
                false
        );
        return disconnector;
    }

    public Node buildConnectivityNode(String voltageLevel, double x, double y) {
        String id = this.idGenerator.generateId();
        Node node = new Node(
                id,
                null,
                "CONNECTIVITY",
                0,
                new Coords(x, y),
                new Dimensions(0, 0),
                new HashMap<>(),
                new HashMap<>(),
                List.of(this.buildPort(id, "FIRST", "TOP", x, y)),
                ""
        );
        return node;
    }

    public Node buildTwoWindingTransformer(String highVoltageLevel, String lowVoltageLevel, double x, double y) {
        String name = this.nameGenerator.generateName("2W transformer");
        String id = this.idGenerator.generateId();
        highVoltageLevel = highVoltageLevel.replace("kV", "");
        lowVoltageLevel = lowVoltageLevel.replace("kV", "");
        Node transformer = new Node(
                this.idGenerator.generateId(),
                null,
                "TWO_WINDING_POWER_TRANSFORMER",
                0,
                new Coords(x, y),
                new Dimensions(136, 34),
                new HashMap<>(),
                new HashMap<>(),
                List.of(this.buildPort(id, "FIRST", "TOP", x + 50, y + 8.5), this.buildPort(id, "SECOND", "BOTTOM", x + 50, y + 144.5)),
                name
        );
        transformer.getFields().putAll(Map.of("SUBSTATION", "noId", "NAME", name, "FREQUENCY", "50", "RATED_APPARENT_POWER", "16"));
        transformer.getFields().putAll(Map.of("FIRST_WINDING_RATED_VOLTAGE", highVoltageLevel, "SECOND_WINDING_RATED_VOLTAGE", lowVoltageLevel, "FIRST_SECOND_WINDING_SHORT_CIRCUIT_VOLTAGE", "10.5", "FIRST_THIRD_WINDING_SHORT_CIRCUIT_VOLTAGE", "17", "SECOND_THIRD_WINDING_SHORT_CIRCUIT_VOLTAGE", "6", "SHORT_CIRCUIT_ACTIVE_POWER", "100", "IDLING_ACTIVE_POWER", "23", "IDLING_CURRENT", "1"));
        transformer.getFields().putAll(Map.of("FIRST_WINDING_TYPE", "yg", "SECOND_WINDING_TYPE", "yg", "THIRD_WINDING_TYPE", "d11", "SATURATION_EXIST", "no", "MAGNETIZATION_VOLTAGE", "1.17", "AIR_CORE_RESISTANCE", "0.2", "SATURATION_COEFFICIENT", "1.25", "TAP_CHANGER_EXISTENCE", "disabled", "TAP_CHANGER_INSTALLATION_WINDING", "onFirstWinding"));
        transformer.getFields().putAll(Map.of("TAP_CHANGER_DEFAULT_POSITION", "0", "TAP_CHANGER_VOLTAGE_CHANGE", "1.78", "TAP_CHANGER_MAX_POSITION", "9", "TAP_CHANGER_MIN_POSITION", "-9"));
        return transformer;
    }

    public Node buildThreeWindingTransformer(String highVoltageLevel, String midVoltageLevel, String lowVoltageLevel, double x, double y) {
        String name = this.nameGenerator.generateName("2W transformer");
        String id = this.idGenerator.generateId();

        highVoltageLevel = highVoltageLevel.replace("kV", "");
        lowVoltageLevel = lowVoltageLevel.replace("kV", "");
        Node transformer = new Node(
                this.idGenerator.generateId(),
                null,
                "THREE_WINDING_POWER_TRANSFORMER",
                0,
                new Coords(x, y),
                new Dimensions(136, 106),
                new HashMap<>(),
                new HashMap<>(),
                List.of(this.buildPort(id, "FIRST", "TOP", 12.35, -2.5), this.buildPort(id, "SECOND", "RIGHT", 101, 61), this.buildPort(id, "THIRD", "BOTTOM", 12.35, 133.5)),
                name
        );
        transformer.getFields().putAll(Map.of("SUBSTATION", "noId", "NAME", name, "FREQUENCY", "50", "RATED_APPARENT_POWER", "16"));
        transformer.getFields().putAll(Map.of("FIRST_WINDING_RATED_VOLTAGE", highVoltageLevel, "SECOND_WINDING_RATED_VOLTAGE", midVoltageLevel, "THIRD_WINDING_RATED_VOLTAGE", lowVoltageLevel, "SHORT_CIRCUIT_VOLTAGE", "10.5", "SHORT_CIRCUIT_ACTIVE_POWER", "85", "IDLING_ACTIVE_POWER", "19", "IDLING_CURRENT", "0.7", "FIRST_WINDING_TYPE", "yg", "SECOND_WINDING_TYPE", "d11"));
        transformer.getFields().putAll(Map.of("SATURATION_EXIST", "no", "MAGNETIZATION_VOLTAGE", "1.17", "AIR_CORE_RESISTANCE", "0.2", "SATURATION_COEFFICIENT", "1.25", "TAP_CHANGER_EXISTENCE", "disabled", "TAP_CHANGER_INSTALLATION_WINDING", "onFirstWinding", "TAP_CHANGER_DEFAULT_POSITION", "0", "TAP_CHANGER_VOLTAGE_CHANGE", "1.78", "TAP_CHANGER_MAX_POSITION", "9", "TAP_CHANGER_MIN_POSITION", "-9"));
        return transformer;
    }

    public Port buildPort(String parentNOdeId, String libId, String alignment, double x, double y) {
        return new Port(
                idGenerator.generateId(),
                libId,
                false,
                false,
                parentNOdeId,
                new Coords(x,y),
                alignment,
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    public Point buildPoint(double x, double y) {
        Point point = new Point(
                this.idGenerator.generateId(),
                false,
                false,
                new Coords(x, y)
        );
        return point;
    }
}
