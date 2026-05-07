package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Iterator;
import java.util.List;

public final class ShiftExpandHandler {
    private static final int VISIBLE_AFFIX_LIMIT = 3;

    private ShiftExpandHandler() {}

    // Config key kept as shift_to_expand for backwards compatibility. Shift triggers vanilla quick-move
    // and EpicFight's innate skill display, and Ctrl conflicts with other tooltip mods, so we gate on Alt.
    public static void apply(List<Component> tooltip) {
        if (Screen.hasAltDown()) return;

        int affixesKept = 0;
        int liveIndex = 0;
        int insertIndex = -1;
        int affixesRemoved = 0;
        Iterator<Component> it = tooltip.iterator();
        while (it.hasNext()) {
            Component line = it.next();
            if (DurabilityHider.isDurableLine(line)) {
                it.remove();
                continue;
            }
            if (TooltipMatcher.isAffixLine(line)) {
                if (affixesKept < VISIBLE_AFFIX_LIMIT) {
                    affixesKept++;
                    liveIndex++;
                } else {
                    if (insertIndex < 0) insertIndex = liveIndex;
                    it.remove();
                    affixesRemoved++;
                }
            } else {
                liveIndex++;
            }
        }

        if (affixesRemoved == 0) return;

        Component prompt = Component.literal("Hold Alt for full details").withStyle(ChatFormatting.DARK_GRAY);
        if (insertIndex >= 0 && insertIndex <= tooltip.size()) {
            tooltip.add(insertIndex, prompt);
        } else {
            tooltip.add(prompt);
        }
    }
}
