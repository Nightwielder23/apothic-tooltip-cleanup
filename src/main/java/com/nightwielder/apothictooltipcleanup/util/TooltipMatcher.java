package com.nightwielder.apothictooltipcleanup.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;

// Helpers for reading Apotheosis tooltip lines and figuring out what kind of line each one is.
public final class TooltipMatcher {
    private static final String[] APOTH_PREFIXES = {
            "apotheosis:",
            "affix.apotheosis",
            "text.apotheosis",
            "tooltip.apotheosis",
            "bonus.apotheosis",
            "misc.apotheosis",
            "attributeslib.modifier",
            "apotheotic_additions"
    };

    private TooltipMatcher() {}

    // The line's translation key, or null if it isn't a translatable component.
    public static String getKey(Component component) {
        TranslatableContents tc = translatable(component);
        return tc == null ? null : tc.getKey();
    }

    // The component's translatable contents, or null. Apoth 6.x builds some tooltip lines (gem
    // headers and bullets among them) by appending the actual translatable onto an empty base
    // component, so the top-level contents is a literal and the key sits on the first sibling.
    // Check the siblings too, otherwise key matching misses every gem line.
    public static TranslatableContents translatable(Component component) {
        if (component == null) return null;
        if (component.getContents() instanceof TranslatableContents tc) return tc;
        for (Component sibling : component.getSiblings()) {
            if (sibling.getContents() instanceof TranslatableContents tc) return tc;
        }
        return null;
    }

    public static boolean keyStartsWith(Component component, String prefix) {
        String key = getKey(component);
        return key != null && key.startsWith(prefix);
    }

    // A bullet line. Apoth 6.x wraps these in dot_prefix.
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

    // True for a raw gem tooltip. Apoth tags the Fits In header with socketable_into; FitsInRemover's
    // compact mode rewrites it to a plain "Fits in:" literal, so accept either.
    public static boolean isGem(List<Component> tooltip) {
        for (Component line : tooltip) {
            if (keyStartsWith(line, "text.apotheosis.socketable_into") || "Fits in:".equals(line.getString())) {
                return true;
            }
        }
        return false;
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
