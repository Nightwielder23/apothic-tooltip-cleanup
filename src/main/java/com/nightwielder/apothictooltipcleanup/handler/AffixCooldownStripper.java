package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.ListIterator;
import java.util.regex.Pattern;

public final class AffixCooldownStripper {
    private static final Pattern COOLDOWN_MARKER = Pattern.compile("\\s*\\[⌛\\s*\\d+:\\d+\\]");

    private AffixCooldownStripper() {}

    // Replacing a translated Component with a literal drops the original style siblings, which can
    // shift color. Acceptable for v1.0; revisit if affected lines render with the wrong color.
    public static void apply(List<Component> tooltip) {
        if (!Config.HIDE_AFFIX_COOLDOWNS.get()) return;
        ListIterator<Component> it = tooltip.listIterator();
        while (it.hasNext()) {
            Component line = it.next();
            if (!TooltipMatcher.keyStartsWith(line, "text.apotheosis.dot_prefix")) continue;
            String text = line.getString();
            if (text.indexOf('⌛') < 0) continue;
            String cleaned = COOLDOWN_MARKER.matcher(text).replaceAll("");
            if (!cleaned.equals(text)) {
                it.set(Component.translatable("text.apotheosis.dot_prefix", Component.literal(cleaned)));
            }
        }
    }
}
