package com.nightwielder.apothictooltipcleanup.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

public final class TooltipMatcher {
    private static final String[] APOTH_PREFIXES = {
            "apotheosis:",
            "affix.apotheosis",
            "text.apotheosis",
            "tooltip.apotheosis",
            "bonus.apotheosis",
            "misc.apotheosis",
            "apothic_attributes:",
            "attributeslib.modifier",
            "apotheotic_additions"
    };

    private TooltipMatcher() {}

    public static String getKey(Component component) {
        if (component == null) return null;
        if (component.getContents() instanceof TranslatableContents tc) {
            return tc.getKey();
        }
        return null;
    }

    public static boolean keyStartsWith(Component component, String prefix) {
        String key = getKey(component);
        return key != null && key.startsWith(prefix);
    }

    // Apotheosis 7.x emphasizes some affix and bonus lines with star_prefix instead of dot_prefix;
    // both are bullet wrappers, so input-side checks should accept either.
    public static boolean isBulletPrefix(Component component) {
        return keyStartsWith(component, "text.apotheosis.dot_prefix")
                || keyStartsWith(component, "text.apotheosis.star_prefix");
    }

    // Affix lines are prose ("On hit, ...", "When held, ..."), while gem bonus lines on
    // sockets follow "<Category>: <effect>". Both arrive bullet-wrapped, so the check is
    // whether ":" is the first punctuation character in the rendered text.
    public static boolean isAffixLine(Component component) {
        if (!isBulletPrefix(component)) return false;
        String text = component.getString();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ':') return false;
            if (c == ',' || c == '.' || c == '!' || c == '?' || c == ';') return true;
        }
        return true;
    }

    public static boolean isApotheosisLine(Component component) {
        if (component == null) return false;
        String key = getKey(component);
        if (key != null) {
            for (String prefix : APOTH_PREFIXES) {
                if (key.startsWith(prefix)) return true;
            }
        }
        for (Component sibling : component.getSiblings()) {
            if (isApotheosisLine(sibling)) return true;
        }
        return false;
    }
}
