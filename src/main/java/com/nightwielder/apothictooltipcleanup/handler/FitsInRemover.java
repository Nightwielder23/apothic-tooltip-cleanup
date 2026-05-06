package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class FitsInRemover {
    private FitsInRemover() {}

    public static void apply(List<Component> tooltip) {
        tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, "text.apotheosis.socketable_into")
                || TooltipMatcher.keyStartsWith(c, "text.apotheosis.when_socketed_in"));
    }
}
