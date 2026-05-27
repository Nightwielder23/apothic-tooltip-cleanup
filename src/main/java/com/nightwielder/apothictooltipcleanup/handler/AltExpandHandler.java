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
    public static void apply(List<Component> tooltip) {
        String mode = Config.AFFIX_DISPLAY_MODE.get();
        if ("all".equalsIgnoreCase(mode)) return;
        if (Screen.hasAltDown()) return;

        // Gem "Fits In" category bullets share text.apotheosis.dot_prefix with affix lines, so
        // without this guard a gem with 4+ categories gets bullets truncated as if they were
        // affixes. Apoth's full-mode header carries text.apotheosis.socketable_into (locale-
        // agnostic); FitsInRemover's compact-mode header is a literal "Fits in:" (English).
        for (Component line : tooltip) {
            if (TooltipMatcher.keyStartsWith(line, "text.apotheosis.socketable_into")
                    || TooltipMatcher.keyStartsWith(line, "text.apotheosis.fits_in")
                    || "Fits in:".equals(line.getString())) {
                return;
            }
        }

        boolean altOnly = "alt_only".equalsIgnoreCase(mode);
        int visibleCount = altOnly ? 0 : Math.max(0, Config.AFFIX_VISIBLE_COUNT.get());

        int affixesKept = 0;
        int liveIndex = 0;
        int insertIndex = -1;
        // True if this handler removed any line. The prompt must appear whenever Alt would reveal
        // something, including the case where only the durability bonus was stripped and no
        // affixes were truncated.
        boolean anythingHidden = false;

        Iterator<Component> it = tooltip.iterator();
        while (it.hasNext()) {
            Component line = it.next();
            if (DurabilityHider.isDurableLine(line)) {
                if (insertIndex < 0) insertIndex = liveIndex;
                it.remove();
                anythingHidden = true;
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

        if (!anythingHidden) return;

        Component prompt = Component.literal("Hold Alt for full details").withStyle(ChatFormatting.DARK_GRAY);
        if (insertIndex >= 0 && insertIndex <= tooltip.size()) {
            tooltip.add(insertIndex, prompt);
        } else {
            tooltip.add(prompt);
        }
    }
}
