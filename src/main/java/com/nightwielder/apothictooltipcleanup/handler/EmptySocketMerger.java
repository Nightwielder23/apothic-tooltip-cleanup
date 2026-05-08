package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

// Text-path fallback for the merge-empty-sockets feature. socket.apotheosis.empty doesn't reach
// ItemTooltipEvent.getToolTip() on the standard render path; that case is handled by
// SocketCompactor at RenderTooltipEvent.GatherComponents. This handler still runs in case some
// modpack setup leaks the key into the text tooltip.
public final class EmptySocketMerger {
    private static final String EMPTY_SOCKET_KEY = "socket.apotheosis.empty";

    private EmptySocketMerger() {}

    public static void apply(List<Component> tooltip) {
        if (!Config.MERGE_EMPTY_SOCKETS.get()) return;
        List<Integer> emptyIndices = new ArrayList<>();
        for (int i = 0; i < tooltip.size(); i++) {
            if (TooltipMatcher.keyStartsWith(tooltip.get(i), EMPTY_SOCKET_KEY)) {
                emptyIndices.add(i);
            }
        }
        if (emptyIndices.size() < 2) return;

        int firstIndex = emptyIndices.get(0);
        Component summary = Component.literal("◇ x" + emptyIndices.size() + " empty sockets")
                .withStyle(ChatFormatting.GRAY);

        for (int i = emptyIndices.size() - 1; i >= 1; i--) {
            tooltip.remove((int) emptyIndices.get(i));
        }
        tooltip.set(firstIndex, summary);
    }
}
