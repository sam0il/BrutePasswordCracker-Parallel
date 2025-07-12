package org.example;

import java.util.*;

public class MaskConfig {
    public final String mask;
    public final Set<Integer> maskIndices;

    public MaskConfig(String mask, Set<Integer> maskIndices) {
        this.mask = mask;
        this.maskIndices = maskIndices;
    }

    // Builds a map of index -> fixed character from mask
    public Map<Integer, Character> getMaskMap(Set<Integer> currentMaskIndices) {
        Map<Integer, Character> map = new HashMap<>();
        List<Integer> sortedIndices = new ArrayList<>(currentMaskIndices);

        for (int i = 0; i < sortedIndices.size(); i++) {
            int position = sortedIndices.get(i);
            char maskChar = (i < mask.length()) ? mask.charAt(i) : '*'; // '*' as fallback
            map.put(position, maskChar);
        }
        return map;
    }

}
