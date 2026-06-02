package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;

import java.util.List;

// Strips the blank lines Apotheosis 6.x emits between gem tooltip sections. Gem-only, so separator
// blanks on other tooltips (attribute blocks and the like) are left alone.
public final class GemBlankLineCleaner {
    private GemBlankLineCleaner() {}

    public static void apply(List<Component> tooltip) {
        if (!TooltipMatcher.isGem(tooltip)) return;

        // drop every blank line; keep the gem name at index 0 even if it somehow renders blank
        for (int i = tooltip.size() - 1; i >= 1; i--) {
            if (isBlank(tooltip.get(i))) {
                tooltip.remove(i);
            }
        }
    }

    private static boolean isBlank(Component c) {
        String s = c.getString();
        return s == null || s.trim().isEmpty();
    }
}
