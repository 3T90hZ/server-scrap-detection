package com.scrapDetection.util;

import org.springframework.stereotype.Component;

@Component
public class Normalize {
    public String normalizeName(String name) {
        if (name == null) {
            return null;
        }

        return name
                .trim()
                .replaceAll("^[^\\p{L}\\p{N}]+", "")
                .replaceAll("[^\\p{L}\\p{N}]+$", "")
                .replaceAll("\\s+", " ")
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
