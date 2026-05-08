package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class SourceLineRemover {
    private static final String POTION_CHARM_SOURCE_KEY = "item.apotheosis.potion_charm.desc3";

    private SourceLineRemover() {}

    public static void apply(List<Component> tooltip) {
        if (!Config.HIDE_SOURCE_LINE.get()) return;
        tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, POTION_CHARM_SOURCE_KEY));
    }
}
