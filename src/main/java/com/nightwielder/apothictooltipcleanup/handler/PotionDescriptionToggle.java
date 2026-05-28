package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.HideMode;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;

import java.util.List;

public final class PotionDescriptionToggle {
    private PotionDescriptionToggle() {}

    // Returns true only when descriptions were hidden in Alt-revealable form (alt mode, Alt up), so
    // AltExpandHandler shows the reveal prompt. show and delete modes never set that signal.
    public static boolean apply(ItemStack stack, List<Component> tooltip) {
        String mode = Config.POTION_DESCRIPTIONS_MODE.get();
        boolean altDown = Screen.hasAltDown();
        if (!HideMode.hides(mode, altDown)) return false;
        if (!(stack.getItem() instanceof PotionItem)) return false;
        boolean removed = tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, "apothic_attributes:"));
        return removed && HideMode.revealable(mode, altDown);
    }
}
