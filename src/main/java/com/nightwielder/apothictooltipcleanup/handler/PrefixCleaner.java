package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.HideMode;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

// Hides the affix type prefix lines (While held, On hit, On block, Passive).
public final class PrefixCleaner {
    private PrefixCleaner() {}

    // Behavior:
    //  - drops the prefix lines when the mode hides them (see HideMode)
    // Parameters:
    //  - tooltip: the lines being shown, edited in place
    // Returns:
    //  - true if it hid an alt-revealable line
    public static boolean apply(List<Component> tooltip) {
        String mode = Config.AFFIX_PREFIXES_MODE.get();
        boolean altDown = Screen.hasAltDown();
        if (!HideMode.hides(mode, altDown)) return false;
        boolean removed = tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, "text.apotheosis.affix_type.while_held")
                || TooltipMatcher.keyStartsWith(c, "text.apotheosis.affix_type.on_hit")
                || TooltipMatcher.keyStartsWith(c, "text.apotheosis.affix_type.on_block")
                || TooltipMatcher.keyStartsWith(c, "text.apotheosis.affix_type.passive"));
        return removed && HideMode.revealable(mode, altDown);
    }
}
