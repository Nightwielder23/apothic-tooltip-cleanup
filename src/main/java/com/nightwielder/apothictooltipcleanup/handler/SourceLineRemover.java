package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;

import java.util.List;

// Always drops the potion charm source line. There's no config for this one.
public final class SourceLineRemover {
    private static final String POTION_CHARM_SOURCE_KEY = "item.apotheosis.potion_charm.desc3";

    private SourceLineRemover() {}

    // Behavior:
    //  - removes the source line. it's permanent and not Alt-revealable.
    // Parameters:
    //  - tooltip: the lines being shown, edited in place
    // Returns:
    //  - always false, so it never triggers the Alt prompt
    public static boolean apply(List<Component> tooltip) {
        tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, POTION_CHARM_SOURCE_KEY));
        return false;
    }
}
