package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class FitsInRemover {
    private FitsInRemover() {}

    public static void apply(List<Component> tooltip) {
        // Phase 1: handle text.apotheosis.unique line plus the blank that follows it
        int j = 0;
        while (j < tooltip.size()) {
            if (TooltipMatcher.keyStartsWith(tooltip.get(j), "text.apotheosis.unique")) {
                tooltip.remove(j);
                if (j < tooltip.size() && isBlank(tooltip.get(j))) {
                    tooltip.remove(j);
                }
                continue;
            }
            j++;
        }

        // Phase 2: state machine for header blocks. Headers seen on loose gems and socketed items:
        //   text.apotheosis.socketable_into       -> "Fits In:"
        //   text.apotheosis.when_socketed_in      -> "When Socketed in:"
        //   text.apotheosis.when_socketed         -> "When Socketed:"
        //   text.apotheosis.when_socketed_typed   -> "When Socketed in <category>:"
        // After a header we strip dot_prefix bullets and blank lines until we hit something else.
        int i = 0;
        while (i < tooltip.size()) {
            String key = TooltipMatcher.getKey(tooltip.get(i));
            boolean isHeader = key != null && (
                key.startsWith("text.apotheosis.socketable_into") ||
                key.startsWith("text.apotheosis.when_socketed_in") ||
                key.startsWith("text.apotheosis.when_socketed_typed") ||
                key.startsWith("text.apotheosis.when_socketed") ||
                key.startsWith("text.apotheosis.fits_in")
            );
            if (!isHeader) {
                i++;
                continue;
            }
            tooltip.remove(i);
            while (i < tooltip.size()) {
                Component next = tooltip.get(i);
                String nextKey = TooltipMatcher.getKey(next);
                boolean isBullet = nextKey != null && nextKey.startsWith("text.apotheosis.dot_prefix");
                if (isBullet || isBlank(next)) {
                    tooltip.remove(i);
                    continue;
                }
                break;
            }
        }
    }

    private static boolean isBlank(Component c) {
        String s = c.getString();
        return s == null || s.trim().isEmpty();
    }
}
