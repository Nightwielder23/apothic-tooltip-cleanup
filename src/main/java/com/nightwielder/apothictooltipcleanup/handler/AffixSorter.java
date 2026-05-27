package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AffixSorter {
    private AffixSorter() {}

    // TODO: multi-line affixes are treated as single rows in v1.0. Single-line is the common case.
    public static void apply(List<Component> tooltip, String sortOrder) {
        if (sortOrder == null || "default".equals(sortOrder)) return;

        List<Integer> indices = new ArrayList<>();
        List<Component> affixLines = new ArrayList<>();
        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            if (TooltipMatcher.isAffixLine(line)) {
                indices.add(i);
                affixLines.add(line);
            }
        }
        if (affixLines.size() < 2) return;

        Comparator<Component> comparator = comparatorFor(sortOrder);
        if (comparator == null) return;

        affixLines.sort(comparator);
        for (int i = 0; i < indices.size(); i++) {
            tooltip.set(indices.get(i), affixLines.get(i));
        }
    }

    // Affix rarity and type live inside the dot_prefix translation arguments, which we can't reliably
    // parse, so both fall back to alphabetical for now.
    private static Comparator<Component> comparatorFor(String sortOrder) {
        return switch (sortOrder) {
            case "alphabetical", "rarity", "type" ->
                    Comparator.comparing(c -> c.getString(), String.CASE_INSENSITIVE_ORDER);
            default -> null;
        };
    }
}
