package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.HideMode;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class SummarizationDisabler {
    private SummarizationDisabler() {}

    // Returns true only when a summary line was hidden in Alt-revealable form (alt mode, Alt up),
    // so AltExpandHandler shows the reveal prompt. show and delete modes never set that signal.
    public static boolean apply(List<Component> tooltip) {
        String mode = Config.SUMMARIZATION_MODE.get();
        boolean altDown = Screen.hasAltDown();
        if (!HideMode.hides(mode, altDown)) return false;
        // Primary: the Apotheosis affix summary block uses attributeslib.modifier.* keys.
        boolean removed = tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, "attributeslib.modifier"));

        // Fallback for older or alternate setups that route the summary through vanilla attribute keys.
        // May also strip genuine vanilla attribute lines on apoth-affixed items.
        boolean hasApothLine = tooltip.stream().anyMatch(TooltipMatcher::isApotheosisLine);
        if (hasApothLine) {
            removed |= tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, "attribute.modifier."));
        }
        return removed && HideMode.revealable(mode, altDown);
    }
}
