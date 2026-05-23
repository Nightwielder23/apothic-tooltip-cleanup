package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AffixCooldownHider {
    // Matches "[⌛ <anything-but-]>]" with optional surrounding whitespace. Apotheosis renders the
    // cooldown marker on affix lines as [⌛ MM:SS]; we strip the bracket group entirely and
    // collapse the whitespace it leaves behind so the surrounding text stays readable.
    private static final Pattern COOLDOWN_PATTERN = Pattern.compile("\\s*\\[\\s*⌛[^\\]]*\\]\\s*");

    private AffixCooldownHider() {}

    public static void apply(List<Component> tooltip) {
        if (!Config.HIDE_AFFIX_COOLDOWNS.get()) return;
        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            if (!TooltipMatcher.isBulletPrefix(line)) continue;
            String inner = extractBulletText(line);
            if (inner == null || inner.isEmpty()) continue;
            if (!COOLDOWN_PATTERN.matcher(inner).find()) continue;
            String cleaned = stripCooldowns(inner);
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

    // The trailing \\s* in the regex can swallow a separating space when the marker is mid-line;
    // we put one back via replacement, then trim the result so leading/trailing matches collapse
    // cleanly. replaceAll handles multiple markers on the same line.
    private static String stripCooldowns(String text) {
        String replaced = COOLDOWN_PATTERN.matcher(text).replaceAll(" ");
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
