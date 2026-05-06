package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class SourceLineRemover {
    private SourceLineRemover() {}

    public static void apply(List<Component> tooltip) {
        tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, "tooltip.apotheosis.gem_source")
                || TooltipMatcher.keyStartsWith(c, "text.apotheosis.gem_source")
                || TooltipMatcher.keyStartsWith(c, "tooltip.apotheosis.source")
                || TooltipMatcher.keyStartsWith(c, "text.apotheosis.source")
                || isItemSourceLine(c));
    }

    // Defensive fallback: some installs render the source line via the item's own translation key.
    // Limitation: the "Source:" check is locale-dependent and only catches English clients.
    private static boolean isItemSourceLine(Component component) {
        String key = TooltipMatcher.getKey(component);
        if (key == null || !key.startsWith("item.")) return false;
        return component.getString().contains("Source:");
    }
}
