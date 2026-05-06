package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class SummarizationDisabler {
    private SummarizationDisabler() {}

    // May also strip genuine vanilla attribute lines on apoth-affixed items.
    public static void apply(List<Component> tooltip) {
        boolean hasApothLine = tooltip.stream().anyMatch(TooltipMatcher::isApotheosisLine);
        if (!hasApothLine) return;
        tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, "attribute.modifier."));
    }
}
