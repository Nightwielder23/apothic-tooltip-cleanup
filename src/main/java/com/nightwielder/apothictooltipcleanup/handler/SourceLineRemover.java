package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class SourceLineRemover {
    private static final String POTION_CHARM_SOURCE_KEY = "item.apotheosis.potion_charm.desc3";

    private SourceLineRemover() {}

    // The source line is pure noise with no reason to stay visible, so it is always removed. The
    // removal is permanent and not Alt-revealable, so this returns false and never triggers the prompt.
    public static boolean apply(List<Component> tooltip) {
        tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, POTION_CHARM_SOURCE_KEY));
        return false;
    }
}
