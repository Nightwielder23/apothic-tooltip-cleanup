package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Iterator;
import java.util.List;

// Trims the affix list for top_n and alt_only mode, then adds the "Hold Alt for full details"
// prompt. Alt is the expand key since Shift triggers quick move and Ctrl clashes with other tooltip mods.
public final class AltExpandHandler {
    private AltExpandHandler() {}

    // Drops affixes past the visible count outside "all" mode and adds the prompt if anything hid,
    // skipping gem tooltips so the budget does not eat gem categories.
    public static void apply(List<Component> tooltip, boolean anyHidden) {
        // Alt down means every hide handler no-oped, so everything already shows.
        if (Screen.hasAltDown()) {
            return;
        }

        boolean hidden = anyHidden;
        int insertIndex = -1;

        String mode = Config.AFFIX_DISPLAY_MODE.get();
        if (!"all".equalsIgnoreCase(mode) && !isGem(tooltip)) {
            boolean altOnly = "alt_only".equalsIgnoreCase(mode);
            int visible = altOnly ? 0 : Math.max(0, Config.AFFIX_VISIBLE_COUNT.get());

            int kept = 0;
            int liveIndex = 0;
            Iterator<Component> it = tooltip.iterator();
            while (it.hasNext()) {
                Component line = it.next();
                if (DurabilityHider.isDurability(line)) {
                    // owned by DurabilityHider, do not count it as an affix
                    liveIndex++;
                } else if (TooltipMatcher.isAffixLine(line)) {
                    if (kept < visible) {
                        kept++;
                        liveIndex++;
                    } else {
                        if (insertIndex < 0) {
                            insertIndex = liveIndex;
                        }
                        it.remove();
                        hidden = true;
                    }
                } else {
                    liveIndex++;
                }
            }
        }

        if (!hidden) {
            return;
        }

        // "all" mode does not set insertIndex, so the prompt would default to the end of a long
        // tooltip and fall off screen. Anchor it right after the last affix instead.
        if (insertIndex < 0) {
            insertIndex = affixInsertIndex(tooltip);
        }

        Component prompt = Component.literal("Hold Alt for full details").withStyle(ChatFormatting.DARK_GRAY);
        if (insertIndex >= 0 && insertIndex <= tooltip.size()) {
            tooltip.add(insertIndex, prompt);
        } else {
            tooltip.add(prompt);
        }
    }

    // Returns the index past the last affix line, falling back to the attribute block (item.modifiers.*)
    // then -1 so the caller appends.
    private static int affixInsertIndex(List<Component> tooltip) {
        int lastAffix = -1;
        int attributeBlock = -1;
        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            if (TooltipMatcher.isAffixLine(line)) {
                lastAffix = i;
            } else if (attributeBlock < 0 && TooltipMatcher.keyStartsWith(line, "item.modifiers.")) {
                attributeBlock = i;
            }
        }
        if (lastAffix >= 0) {
            return lastAffix + 1;
        }
        return attributeBlock;
    }

    // Returns true for a raw gem tooltip. full mode keeps the socketable_into/fits_in key and compact
    // rewrites it to a "Fits in:" literal, so match either to skip gems.
    private static boolean isGem(List<Component> tooltip) {
        for (Component line : tooltip) {
            if (TooltipMatcher.keyStartsWith(line, "text.apotheosis.socketable_into")
                    || TooltipMatcher.keyStartsWith(line, "text.apotheosis.fits_in")
                    || "Fits in:".equals(line.getString())) {
                return true;
            }
        }
        return false;
    }
}
