package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Iterator;
import java.util.List;

public final class AltExpandHandler {

    private AltExpandHandler() {}

    // Alt is the expand modifier: Shift triggers vanilla quick-move and EpicFight's innate skill
    // display, and Ctrl conflicts with other tooltip mods.
    //
    // Runs last in the tooltip pass: it truncates affixes for top_n / alt_only mode and adds the
    // single "Hold Alt for full details" prompt. anyAltRevealableHidden carries whether any feature
    // hid content this pass that Alt can bring back; delete-mode hides are excluded, so the prompt
    // never promises to reveal something it cannot. The prompt shows for those reveals even when
    // affix truncation is off (mode = all).
    public static void apply(List<Component> tooltip, boolean anyAltRevealableHidden) {
        // Alt held: every hide handler no-ops this pass, so the user already sees everything and no
        // prompt is needed.
        if (Screen.hasAltDown()) return;

        boolean anythingHidden = anyAltRevealableHidden;
        int insertIndex = -1;

        // Truncation only applies outside "all" mode, and never on gem tooltips: gem "Fits In"
        // category bullets share text.apotheosis.dot_prefix with affix lines, so the budget would
        // otherwise eat gem categories on multi-category gems.
        String mode = Config.AFFIX_DISPLAY_MODE.get();
        if (!"all".equalsIgnoreCase(mode) && !isGemTooltip(tooltip)) {
            boolean altOnly = "alt_only".equalsIgnoreCase(mode);
            int visibleCount = altOnly ? 0 : Math.max(0, Config.AFFIX_VISIBLE_COUNT.get());

            int affixesKept = 0;
            int liveIndex = 0;
            Iterator<Component> it = tooltip.iterator();
            while (it.hasNext()) {
                Component line = it.next();
                // Durability is owned by DurabilityHider; leave it in place rather than counting it
                // against the affix budget or truncating it as an affix.
                if (DurabilityHider.isDurableLine(line)) {
                    liveIndex++;
                } else if (TooltipMatcher.isAffixLine(line)) {
                    if (affixesKept < visibleCount) {
                        affixesKept++;
                        liveIndex++;
                    } else {
                        if (insertIndex < 0) insertIndex = liveIndex;
                        it.remove();
                        anythingHidden = true;
                    }
                } else {
                    liveIndex++;
                }
            }
        }

        if (!anythingHidden) return;

        // With no truncation (mode = all) nothing set insertIndex. Appending at the end drops the
        // prompt below the attribute block, where a tall tooltip clips it off the bottom of the
        // screen, so anchor it right after the last affix line instead.
        if (insertIndex < 0) {
            insertIndex = lastAffixInsertIndex(tooltip);
        }

        Component prompt = Component.literal("Hold Alt for full details").withStyle(ChatFormatting.DARK_GRAY);
        if (insertIndex >= 0 && insertIndex <= tooltip.size()) {
            tooltip.add(insertIndex, prompt);
        } else {
            tooltip.add(prompt);
        }
    }

    // Index just after the last affix line. Falls back to the start of the vanilla attribute block
    // ("When in Main Hand:" et al, keyed item.modifiers.*), then to -1 so the caller appends.
    private static int lastAffixInsertIndex(List<Component> tooltip) {
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
        if (lastAffix >= 0) return lastAffix + 1;
        return attributeBlock;
    }

    // Apoth full-mode header carries text.apotheosis.socketable_into (locale-agnostic);
    // FitsInRemover's compact-mode header is the literal "Fits in:" (English).
    private static boolean isGemTooltip(List<Component> tooltip) {
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
