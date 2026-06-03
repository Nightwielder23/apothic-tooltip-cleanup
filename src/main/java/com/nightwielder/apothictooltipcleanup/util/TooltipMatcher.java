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
            "apothic_attributes:",
            "attributeslib.modifier",
            "apotheotic_additions"
    };

    private TooltipMatcher() {}

    // Returns the line's translation key, or null if it is not a translatable component.
    public static String getKey(Component component) {
        TranslatableContents tc = translatable(component);
        return tc == null ? null : tc.getKey();
    }

    // Returns the component's translatable contents, or null. Some Apotheosis lines append the
    // translatable onto an empty base, so the top level contents is a literal and the key sits on the
    // first sibling. The siblings are checked too.
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

    // Returns true for a bullet line, which Apoth wraps in dot_prefix.
    public static boolean isBulletPrefix(Component component) {
        return keyStartsWith(component, "text.apotheosis.dot_prefix");
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

    // Returns true for a raw gem tooltip. full mode keeps the socketable_into/fits_in key and compact
    // rewrites it to a "Fits in:" literal, so match either to skip gems.
    public static boolean isGem(List<Component> tooltip) {
        for (Component line : tooltip) {
            if (keyStartsWith(line, "text.apotheosis.socketable_into")
                    || keyStartsWith(line, "text.apotheosis.fits_in")
                    || "Fits in:".equals(line.getString())) {
                return true;
            }
        }
        return false;
    }

    // Returns true if the line or any sibling came from Apotheosis or a supported add-on.
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
