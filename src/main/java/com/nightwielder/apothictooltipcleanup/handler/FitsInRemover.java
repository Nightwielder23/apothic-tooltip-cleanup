package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;

public final class FitsInRemover {
    // Apotheosis renders the "Fits In:" header and category bullets in this teal-blue color.
    private static final int FITS_IN_COLOR = 720650;

    private FitsInRemover() {}

    public static void apply(List<Component> tooltip) {
        String mode = Config.GEM_TOOLTIP_MODE.get();
        if ("full".equalsIgnoreCase(mode)) return;

        boolean hidden = "hidden".equalsIgnoreCase(mode);
        boolean tight = "tight".equalsIgnoreCase(mode);
        boolean ultra = "ultra".equalsIgnoreCase(mode);

        stripUnique(tooltip);

        int i = 0;
        while (i < tooltip.size()) {
            String key = TooltipMatcher.getKey(tooltip.get(i));
            boolean isFits = key != null
                    && (key.startsWith("text.apotheosis.socketable_into") || key.startsWith("text.apotheosis.fits_in"));
            boolean isBonusHeader = key != null && key.startsWith("text.apotheosis.when_socketed_in");
            if (!isFits && !isBonusHeader) {
                i++;
                continue;
            }

            int afterBullets = i + 1;
            while (afterBullets < tooltip.size()
                    && TooltipMatcher.keyStartsWith(tooltip.get(afterBullets), "text.apotheosis.dot_prefix")) {
                afterBullets++;
            }

            if (hidden) {
                removeRange(tooltip, i, afterBullets);
                while (i < tooltip.size() && isBlank(tooltip.get(i))) tooltip.remove(i);
                continue;
            }

            if (isFits && (tight || ultra)) {
                String joined = joinBulletTexts(tooltip, i + 1, afterBullets);
                removeRange(tooltip, i, afterBullets);
                if (!joined.isEmpty()) {
                    tooltip.add(i, Component.translatable("text.apotheosis.dot_prefix",
                                    Component.literal("Applicable on: " + joined))
                            .withStyle(Style.EMPTY.withColor(FITS_IN_COLOR)));
                    i++;
                }
                continue;
            }

            if (isBonusHeader && ultra) {
                String joined = joinBulletTexts(tooltip, i + 1, afterBullets);
                removeRange(tooltip, i, afterBullets);
                if (!joined.isEmpty()) {
                    tooltip.add(i, Component.translatable("text.apotheosis.dot_prefix",
                                    Component.literal(joined))
                            .withStyle(ChatFormatting.GOLD));
                    i++;
                }
                continue;
            }

            // compact mode, or tight on the bonus header: drop the header, keep all bullets.
            tooltip.remove(i);
        }

        cleanupOrphanBlanks(tooltip);
    }

    private static void stripUnique(List<Component> tooltip) {
        int j = 0;
        while (j < tooltip.size()) {
            if (TooltipMatcher.keyStartsWith(tooltip.get(j), "text.apotheosis.unique")) {
                tooltip.remove(j);
                if (j < tooltip.size() && isBlank(tooltip.get(j))) tooltip.remove(j);
                continue;
            }
            j++;
        }
    }

    private static String joinBulletTexts(List<Component> tooltip, int from, int toExclusive) {
        StringBuilder sb = new StringBuilder();
        for (int k = from; k < toExclusive; k++) {
            String inner = extractBulletText(tooltip.get(k));
            if (inner == null || inner.isEmpty()) continue;
            if (sb.length() > 0) sb.append(", ");
            sb.append(inner);
        }
        return sb.toString();
    }

    // Each dot_prefix bullet is TranslatableContents("text.apotheosis.dot_prefix", [innerComponent]).
    // The visible payload (categories or bonus text) is the rendered string of arg[0].
    private static String extractBulletText(Component bullet) {
        if (!(bullet.getContents() instanceof TranslatableContents tc)) return null;
        Object[] args = tc.getArgs();
        if (args.length == 0) return null;
        Object arg = args[0];
        if (arg instanceof Component c) return c.getString();
        return String.valueOf(arg);
    }

    private static void removeRange(List<Component> tooltip, int from, int toExclusive) {
        for (int k = toExclusive - 1; k >= from; k--) tooltip.remove(k);
    }

    // After mode-specific transformation, a blank line that used to separate the Fits-In and
    // When-Socketed-In sections may now sit between two dot_prefix bullets. Drop those.
    private static void cleanupOrphanBlanks(List<Component> tooltip) {
        for (int k = tooltip.size() - 2; k >= 1; k--) {
            if (!isBlank(tooltip.get(k))) continue;
            if (TooltipMatcher.keyStartsWith(tooltip.get(k - 1), "text.apotheosis.dot_prefix")
                    && TooltipMatcher.keyStartsWith(tooltip.get(k + 1), "text.apotheosis.dot_prefix")) {
                tooltip.remove(k);
            }
        }
    }

    private static boolean isBlank(Component c) {
        String s = c.getString();
        return s == null || s.trim().isEmpty();
    }
}
