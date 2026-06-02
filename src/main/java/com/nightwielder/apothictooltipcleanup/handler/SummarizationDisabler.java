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

    // Behavior:
    //  - drops the summary lines when the mode hides them (see HideMode)
    // Parameters:
    //  - tooltip: the lines being shown, edited in place
    // Returns:
    //  - true if it hid an alt-revealable line
    public static boolean apply(List<Component> tooltip) {
        // raw gems emit single-attribute socket bonuses with the same attributeslib.modifier key the
        // summary block uses; skip gems so their "When Socketed" line is not stripped (which would
        // orphan the header). non-gem summary blocks are unaffected.
        if (TooltipMatcher.isGem(tooltip)) return false;

        String mode = Config.SUMMARIZATION_MODE.get();
        boolean altDown = Screen.hasAltDown();
        if (!HideMode.hides(mode, altDown)) return false;
        // the summary block normally uses attributeslib.modifier.* keys
        boolean removed = tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, "attributeslib.modifier"));

        // some setups route the summary through vanilla attribute keys. only fall back when an
        // Apotheosis line is present. otherwise it would strip genuine vanilla attribute lines.
        boolean hasApothLine = tooltip.stream().anyMatch(TooltipMatcher::isApotheosisLine);
        if (hasApothLine) {
            removed |= tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, "attribute.modifier."));
        }
        return removed && HideMode.revealable(mode, altDown);
    }
}
