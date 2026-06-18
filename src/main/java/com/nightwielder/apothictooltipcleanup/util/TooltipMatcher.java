package com.nightwielder.apothictooltipcleanup.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;

// Helpers for reading Apotheosis tooltip lines and classifying each one.
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

    // Returns the line's translation key, or null if it is not a translatable component.
    public static String getKey(Component component) {
        TranslatableContents tc = translatable(component);
        return tc == null ? null : tc.getKey();
    }

    // Returns the component's translatable contents, or null. Apoth 6.x builds some lines by appending
    // the translatable onto an empty base, so the top level contents is a literal and the key sits on
    // the first sibling. The siblings are checked too, otherwise key matching misses every gem line.
    public static TranslatableContents translatable(Component component) {
        if (component == null) {
            return null;
        }
        if (component.getContents() instanceof TranslatableContents tc) {
            return tc;
        }
        for (Component sibling : component.getSiblings()) {
            if (sibling.getContents() instanceof TranslatableContents tc) {
                return tc;
            }
        }
        return null;
    }

    public static boolean keyStartsWith(Component component, String prefix) {
        String key = getKey(component);
        return key != null && key.startsWith(prefix);
    }

    // Returns true for a bullet line. Apoth wraps affix and gem bullets in dot_prefix, and newer versions
    // also use star_prefix for some affix lines, so both count as bullets or those lines slip every check.
    public static boolean isBulletPrefix(Component component) {
        return keyStartsWith(component, "text.apotheosis.dot_prefix")
                || keyStartsWith(component, "text.apotheosis.star_prefix");
    }

    // Tells affix bullets from gem bonus bullets. Gem bonuses have a ":" before any other punctuation.
    public static boolean isAffixLine(Component component) {
        if (!isBulletPrefix(component)) {
            return false;
        }
        String text = component.getString();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == ':') {
                return false;
            }
            if (c == ',' || c == '.' || c == '!' || c == '?' || c == ';') {
                return true;
            }
        }
        return true;
    }

    // Returns true for a raw gem tooltip. Apoth tags the Fits In header with socketable_into and
    // FitsInRemover rewrites it to a plain "Fits in:" literal in compact mode, so accept either.
    public static boolean isGem(List<Component> tooltip) {
        for (Component line : tooltip) {
            if (keyStartsWith(line, "text.apotheosis.socketable_into") || "Fits in:".equals(line.getString())) {
                return true;
            }
        }
        return false;
    }

    // Returns true if the line or any sibling came from Apotheosis or a supported addon.
    public static boolean isApotheosisLine(Component component) {
        if (component == null) {
            return false;
        }
        String key = getKey(component);
        if (key != null) {
            for (String prefix : APOTH_PREFIXES) {
                if (key.startsWith(prefix)) {
                    return true;
                }
            }
        }
        for (Component sibling : component.getSiblings()) {
            if (isApotheosisLine(sibling)) {
                return true;
            }
        }
        return false;
    }
}
