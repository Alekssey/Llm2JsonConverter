package ru.mpei.llmconverter.utils;

import org.springframework.stereotype.Component;

@Component
public class IdGenerator {
    private String prevId = "00000000-0000-0000-0000-000000000000";

    public synchronized String generateId() {
        String id = tryToGen();
        while (id.equals(prevId)){
            id = tryToGen();
        }
        prevId = id;
        return id;
    }

    private String tryToGen() {
        String mask = "00000000000000000000000000000000";
        String time = String.valueOf(System.currentTimeMillis());
        mask = mask.substring(0, mask.length() - time.length()) + time;
        return mask.substring(0, 8) + "-" + mask.substring(8, 12) + "-" + mask.substring(12, 16) + "-" + mask.substring(16, 20) + "-" + mask.substring(20, 32);
    }

}
