package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class SummarizationDisabler {
    private SummarizationDisabler() {}

    public static void apply(List<Component> tooltip) {
        // Primary: the Apotheosis affix summary block uses attributeslib.modifier.* keys.
        tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, "attributeslib.modifier"));

        // Fallback for older or alternate setups that route the summary through vanilla attribute keys.
        // May also strip genuine vanilla attribute lines on apoth-affixed items.
        boolean hasApothLine = tooltip.stream().anyMatch(TooltipMatcher::isApotheosisLine);
        if (hasApothLine) {
            tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, "attribute.modifier."));
        }
    }
}
