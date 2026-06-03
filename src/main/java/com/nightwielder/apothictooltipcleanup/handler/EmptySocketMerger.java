package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

// Text path fallback for merging empty sockets. SocketCompactor handles this at gather time so the
// key rarely reaches the text tooltip. This is a backup in case it leaks through.
public final class EmptySocketMerger {
    private static final String EMPTY_SOCKET_KEY = "socket.apotheosis.empty";

    private EmptySocketMerger() {}

    public static void apply(List<Component> tooltip) {
        if (!Config.MERGE_EMPTY_SOCKETS.get()) {
            return;
        }
        List<Integer> emptyIndices = new ArrayList<>();
        for (int i = 0; i < tooltip.size(); i++) {
            if (TooltipMatcher.keyStartsWith(tooltip.get(i), EMPTY_SOCKET_KEY)) {
                emptyIndices.add(i);
            }
        }
        if (emptyIndices.size() < 2) {
            return;
        }

        int firstIndex = emptyIndices.get(0);
        Component summary = Component.literal("◇ x" + emptyIndices.size() + " empty sockets")
                .withStyle(ChatFormatting.GRAY);

        // Remove the extras back to front so earlier indices stay valid. The first slot becomes the summary.
        for (int i = emptyIndices.size() - 1; i >= 1; i--) {
            tooltip.remove((int) emptyIndices.get(i));
        }
        tooltip.set(firstIndex, summary);
    }
}
