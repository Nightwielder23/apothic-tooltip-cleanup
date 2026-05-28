package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.HideMode;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class PrefixCleaner {
    private PrefixCleaner() {}

    // Returns true only when prefixes were hidden in Alt-revealable form (alt mode, Alt up), so
    // AltExpandHandler shows the reveal prompt. show and delete modes never set that signal.
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
