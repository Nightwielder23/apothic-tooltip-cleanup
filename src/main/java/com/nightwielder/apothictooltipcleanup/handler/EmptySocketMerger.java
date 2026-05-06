package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public final class EmptySocketMerger {
    // Defensive: exact key has not been verified at runtime, so match all plausible variants.
    private static final String[] EMPTY_SOCKET_KEYS = {
            "tooltip.apotheosis.socket.empty",
            "text.apotheosis.socket.empty",
            "tooltip.apotheosis.empty_socket",
            "text.apotheosis.empty_socket"
    };

    private EmptySocketMerger() {}

    public static void apply(List<Component> tooltip) {
        List<Integer> emptyIndices = new ArrayList<>();
        for (int i = 0; i < tooltip.size(); i++) {
            if (isEmptySocketLine(tooltip.get(i))) {
                emptyIndices.add(i);
            }
        }
        if (emptyIndices.size() < 2) return;

        int firstIndex = emptyIndices.get(0);
        int emptyCount = emptyIndices.size();
        Component summary = Component.literal("◇ x" + emptyCount + " empty sockets")
                .withStyle(ChatFormatting.GRAY);

        for (int i = emptyIndices.size() - 1; i >= 1; i--) {
            tooltip.remove((int) emptyIndices.get(i));
        }
        tooltip.set(firstIndex, summary);
    }

    private static boolean isEmptySocketLine(Component component) {
        for (String key : EMPTY_SOCKET_KEYS) {
            if (TooltipMatcher.keyStartsWith(component, key)) return true;
        }
        return false;
    }
}
