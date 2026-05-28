package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.HideMode;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class DurabilityHider {
    private DurabilityHider() {}

    // Durable bonus renders through a dot_prefix or star_prefix bullet with affix.apotheosis:durable.desc
    // as a translation argument, so a key-tree walk misses it. Match on the rendered string instead.
    // English-only for v1.0; non-English clients won't trigger this match.
    //
    // Returns true only when the line was hidden in Alt-revealable form (alt mode, Alt up), so
    // AltExpandHandler shows the reveal prompt. show and delete modes never set that signal.
    public static boolean apply(List<Component> tooltip) {
        String mode = Config.DURABILITY_BONUS_MODE.get();
        boolean altDown = Screen.hasAltDown();
        if (!HideMode.hides(mode, altDown)) return false;
        boolean removed = tooltip.removeIf(DurabilityHider::isDurableLine);
        return removed && HideMode.revealable(mode, altDown);
    }

    static boolean isDurableLine(Component component) {
        if (!TooltipMatcher.isBulletPrefix(component)) return false;
        String text = component.getString();
        return text.contains("ignores") && text.contains("durability damage");
    }
}
