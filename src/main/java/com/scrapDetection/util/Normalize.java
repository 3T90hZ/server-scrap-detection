package com.scrapDetection.util;

import org.springframework.stereotype.Component;

@Component
public class Normalize {
    public String normalizeName(String name) {
        if (name == null) {
            return null;
        }

        return name
                .trim()                          // remove leading/trailing spaces
                .replaceAll("^[^a-zA-Z0-9]+", "") // remove leading special chars
                .replaceAll("[^a-zA-Z0-9]+$", "") // remove trailing special chars
                .toLowerCase();
    }
    public String normalizeEmailAndPhoneNumber(String string){
        if(string != null && !string.trim().isEmpty()){
            return string.trim().toLowerCase();
        }else {
            return null;
        }
    }
}
