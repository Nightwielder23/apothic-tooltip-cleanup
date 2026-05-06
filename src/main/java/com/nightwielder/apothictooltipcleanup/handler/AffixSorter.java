package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class AffixSorter {
    private static final String AFFIX_KEY_PREFIX = "affix.apotheosis:";
    private static final String[] TYPE_TOKENS = {"while_held", "on_hit", "on_block", "passive"};

    private AffixSorter() {}

    // TODO: multi-line affixes are treated as single rows in v1.0. Single-line is the common case in 1.20.1.
    public static void apply(List<Component> tooltip, String sortOrder) {
        if (sortOrder == null || "default".equals(sortOrder)) return;

        List<Integer> indices = new ArrayList<>();
        List<Component> affixLines = new ArrayList<>();
        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            if (TooltipMatcher.keyStartsWithRecursive(line, AFFIX_KEY_PREFIX)) {
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

    private static Comparator<Component> comparatorFor(String sortOrder) {
        return switch (sortOrder) {
            case "alphabetical" -> Comparator.comparing(c -> c.getString(), String.CASE_INSENSITIVE_ORDER);
            // Rarity lives in runtime component style, not the key, so we can't sort by it yet. Falls back to alphabetical.
            case "rarity" -> Comparator.comparing(c -> c.getString(), String.CASE_INSENSITIVE_ORDER);
            // Type token is rarely embedded in the affix key itself, so most lines compare equal here
            // and stable sort preserves their original order.
            case "type" -> Comparator.comparing(AffixSorter::extractType);
            default -> null;
        };
    }

    private static String getAffixKey(Component component) {
        String key = TooltipMatcher.getKey(component);
        if (key != null && key.startsWith(AFFIX_KEY_PREFIX)) return key;
        for (Component sibling : component.getSiblings()) {
            String siblingKey = TooltipMatcher.getKey(sibling);
            if (siblingKey != null && siblingKey.startsWith(AFFIX_KEY_PREFIX)) return siblingKey;
        }
        return null;
    }

    private static String extractType(Component component) {
        String key = getAffixKey(component);
        if (key == null) return "";
        for (String token : TYPE_TOKENS) {
            if (key.contains(token)) return token;
        }
        return "";
    }
}
