package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;

import java.util.List;

// Always drops the potion charm source line, which has no config.
public final class SourceLineRemover {
    private static final String POTION_CHARM_SOURCE_KEY = "item.apotheosis.potion_charm.desc3";

    private SourceLineRemover() {}

    // Drops the source line permanently and returns false, so it never triggers the Alt prompt.
    public static boolean apply(List<Component> tooltip) {
        tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, POTION_CHARM_SOURCE_KEY));
        return false;
    }
}
