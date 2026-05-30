package com.nightwielder.apothictooltipcleanup.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

// Helpers for reading Apotheosis tooltip lines and figuring out what kind of line each one is.
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

    // The line's translation key, or null if it isn't a translatable component.
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

    // A bullet line. Apoth 8.x wraps these in dot_prefix.
    public static boolean isBulletPrefix(Component component) {
        return keyStartsWith(component, "text.apotheosis.dot_prefix");
    }

    // Behavior:
    //  - tells affix bullets from gem bonus bullets. gem bonuses have a ":" before any other punctuation.
    // Parameters:
    //  - component: a tooltip line
    // Returns:
    //  - true if it looks like an affix line
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

    // True if the line or any of its siblings came from Apotheosis or an add-on.
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
