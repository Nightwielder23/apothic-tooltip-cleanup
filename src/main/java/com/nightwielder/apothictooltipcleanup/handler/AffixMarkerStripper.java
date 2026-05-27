package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;
import java.util.regex.Pattern;

public final class AffixMarkerStripper {
    // Allowlist for the two trailing markers Apoth 7.x emits on affix lines:
    // [⌛ MM:SS] from affix.apotheosis.cooldown and [Stacking] from affix.apotheosis.stacking.
    // A broad [CapitalizedWord] regex would also eat in-prose brackets that affixes like
    // Stoneforming render ([Stone], [Cobblestone]) which are gameplay-critical, so the pattern is
    // pinned to the two known formats.
    private static final Pattern MARKER_PATTERN = Pattern.compile("\\s*\\[(?:⌛\\s*\\d+:\\d+|Stacking)\\]");

    private AffixMarkerStripper() {}

    public static void apply(List<Component> tooltip) {
        if (!Config.HIDE_AFFIX_EXTRAS.get()) return;
        // Holding Alt reveals the unstripped line, matching AltExpandHandler's reveal-on-hold UX.
        if (Screen.hasAltDown()) return;
        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            if (!TooltipMatcher.isBulletPrefix(line)) continue;
            // Cheap codepoint pre-filter: skip lines with neither the hourglass nor "[Stacking]"
            // before paying for extractBulletText + regex on every bullet.
            String text = line.getString();
            if (text.indexOf('⌛') < 0 && !text.contains("[Stacking]")) continue;
            String inner = extractBulletText(line);
            if (inner == null || inner.isEmpty()) continue;
            if (!MARKER_PATTERN.matcher(inner).find()) continue;
            String cleaned = stripMarkers(inner);
            if (cleaned.isEmpty()) continue;
            tooltip.set(i, rebuildBullet(line, cleaned));
        }
    }

    // The bullet's payload is the rendered string of arg[0] on the TranslatableContents, matching
    // how FitsInRemover.extractBulletText reads it.
    private static String extractBulletText(Component bullet) {
        if (!(bullet.getContents() instanceof TranslatableContents tc)) return null;
        Object[] args = tc.getArgs();
        if (args.length == 0) return null;
        Object arg = args[0];
        if (arg instanceof Component c) return c.getString();
        return String.valueOf(arg);
    }

    // The regex's leading \\s* swallows a separating space when the marker is mid-line; we put one
    // back via replacement, then trim and collapse double spaces so leading/trailing matches don't
    // leave gaps. replaceAll handles multiple markers on the same line.
    private static String stripMarkers(String text) {
        String replaced = MARKER_PATTERN.matcher(text).replaceAll(" ");
        return replaced.trim().replaceAll("\\s{2,}", " ");
    }

    // Preserve the bullet's original prefix key (dot_prefix vs star_prefix) and top-level style so
    // the line keeps whatever color/formatting Apotheosis applied. The inner Component is rebuilt
    // as a plain literal carrying the cleaned text; that loses any inner color but matches how the
    // FitsInRemover compact rebuild handles bullet content.
    private static Component rebuildBullet(Component original, String cleanedText) {
        String key = TooltipMatcher.getKey(original);
        if (key == null) key = "text.apotheosis.dot_prefix";
        return Component.translatable(key, Component.literal(cleanedText)).withStyle(original.getStyle());
    }
}
