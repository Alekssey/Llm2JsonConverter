package ru.mpei.llmconverter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConnectionNode extends Node{

    private boolean selected;
    private boolean locked;

//    public <V, K, E> ConnectionNode(String s, boolean b, boolean b1, String s1, String circuitBreaker, int i, Coords coords, Dimensions dimensions, HashMap<K,V> kvHashMap, HashMap<K,V> kvHashMap1, ArrayList<E> es, String name) {
//    }


    public ConnectionNode(String id, String voltageLevelId, String libEquipmentId, int hour, Coords coords, Dimensions dimensions, Map<String, String> fields, Map<String, Map<String, String>> controls, List<Port> ports, String name, boolean selected, boolean locked) {
        super(id, voltageLevelId, libEquipmentId, hour, coords, dimensions, fields, controls, ports, name);
        this.selected = selected;
        this.locked = locked;
    }
//
//    public ConnectionNode(boolean selected, boolean locked) {
//        this.selected = selected;
//        this.locked = locked;
//    }
}
