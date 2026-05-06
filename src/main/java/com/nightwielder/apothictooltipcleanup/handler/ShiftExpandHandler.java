package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Iterator;
import java.util.List;

public final class ShiftExpandHandler {
    private static final String AFFIX_KEY_PREFIX = "affix.apotheosis:";
    private static final String DURABLE_KEY = "affix.apotheosis:durable.desc";
    private static final int VISIBLE_AFFIX_LIMIT = 3;

    private ShiftExpandHandler() {}

    // Config name SHIFT_TO_EXPAND is historical: shift-click in inventory triggers vanilla quick-move, so we gate on Ctrl.
    public static void apply(List<Component> tooltip) {
        if (Screen.hasControlDown()) return;

        int affixesKept = 0;
        int liveIndex = 0;
        int insertIndex = -1;
        boolean removed = false;
        Iterator<Component> it = tooltip.iterator();
        while (it.hasNext()) {
            Component line = it.next();
            if (TooltipMatcher.keyEqualsRecursive(line, DURABLE_KEY)) {
                it.remove();
                removed = true;
                continue;
            }
            if (TooltipMatcher.keyStartsWithRecursive(line, AFFIX_KEY_PREFIX)) {
                if (affixesKept < VISIBLE_AFFIX_LIMIT) {
                    affixesKept++;
                    liveIndex++;
                } else {
                    if (insertIndex < 0) insertIndex = liveIndex;
                    it.remove();
                    removed = true;
                }
            } else {
                liveIndex++;
            }
        }

        if (!removed) return;

        Component prompt = Component.literal("Hold Ctrl for full details").withStyle(ChatFormatting.DARK_GRAY);
        if (insertIndex >= 0 && insertIndex <= tooltip.size()) {
            tooltip.add(insertIndex, prompt);
        } else {
            tooltip.add(prompt);
        }
    }
}
