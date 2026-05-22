package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class DurabilityHider {
    private DurabilityHider() {}

    // Durable bonus renders through a dot_prefix or star_prefix bullet with affix.apotheosis:durable.desc
    // as a translation argument, so a key-tree walk misses it. Match on the rendered string instead.
    // English-only for v1.0; non-English clients won't trigger this match.
    public static void apply(List<Component> tooltip) {
        if (!Config.HIDE_DURABILITY_BONUS.get()) return;
        tooltip.removeIf(DurabilityHider::isDurableLine);
    }

    static boolean isDurableLine(Component component) {
        if (!TooltipMatcher.isBulletPrefix(component)) return false;
        String text = component.getString();
        return text.contains("ignores") && text.contains("durability damage");
    }
}
