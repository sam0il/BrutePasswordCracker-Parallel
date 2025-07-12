package org.example;

import java.util.*;

public class MaskConfig {
    public final String mask;
    public final Set<Integer> maskIndices;

    public MaskConfig(String mask, Set<Integer> maskIndices) {
        this.mask = mask;
        this.maskIndices = maskIndices;
    }

    public Map<Integer, Character> getMaskMap(Set<Integer> currentMaskIndices) {
        Map<Integer, Character> map = new HashMap<>();
        List<Integer> sortedIndices = new ArrayList<>(currentMaskIndices);
        Collections.sort(sortedIndices);

        for (int i = 0; i < sortedIndices.size(); i++) {
            int position = sortedIndices.get(i);
            char maskChar = (i < mask.length()) ? mask.charAt(i) : '*';
            map.put(position, maskChar);
        }
        return map;
    }

    public boolean hasMask() {
        return !maskIndices.isEmpty() && !mask.isEmpty();
    }
}