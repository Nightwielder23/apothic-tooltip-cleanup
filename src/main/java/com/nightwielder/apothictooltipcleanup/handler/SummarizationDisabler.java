package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.HideMode;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

// Hides the Apotheosis affix summary block (the Cold/Fire/HP%/Spell Resistance lines).
public final class SummarizationDisabler {
    private SummarizationDisabler() {}

    // Drops the summary lines when the mode hides them and returns true if a hidden line is alt revealable.
    public static boolean apply(List<Component> tooltip) {
        // raw gems emit single attribute socket bonuses with the same attributeslib.modifier key the
        // summary block uses. Skip gems so their "When Socketed" line is not stripped, which would
        // orphan the header. Other summary blocks are unaffected.
        if (TooltipMatcher.isGem(tooltip)) {
            return false;
        }

        String mode = Config.SUMMARIZATION_MODE.get();
        boolean altDown = Screen.hasAltDown();
        if (!HideMode.hides(mode, altDown)) {
            return false;
        }
        // the summary block normally uses attributeslib.modifier.* keys
        boolean removed = tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, "attributeslib.modifier"));

        // some setups route the summary through vanilla attribute keys. Only fall back when an
        // Apotheosis line is present, otherwise genuine vanilla attribute lines would be stripped.
        boolean hasApothLine = tooltip.stream().anyMatch(TooltipMatcher::isApotheosisLine);
        if (hasApothLine) {
            removed |= tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, "attribute.modifier."));
        }
        return removed && HideMode.revealable(mode, altDown);
    }
}
