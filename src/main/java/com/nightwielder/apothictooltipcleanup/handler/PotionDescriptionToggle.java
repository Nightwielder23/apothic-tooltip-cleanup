package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.HideMode;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;

import java.util.List;

// Hides the potion style affix descriptions Apothic Attributes adds to potions.
public final class PotionDescriptionToggle {
    private PotionDescriptionToggle() {}

    // On potions, drops the description lines when the mode hides them and returns true if alt revealable.
    public static boolean apply(ItemStack stack, List<Component> tooltip) {
        String mode = Config.POTION_DESCRIPTIONS_MODE.get();
        boolean altDown = Screen.hasAltDown();
        if (!HideMode.hides(mode, altDown)) {
            return false;
        }
        if (!(stack.getItem() instanceof PotionItem)) {
            return false;
        }
        boolean removed = tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, "apothic_attributes:"));
        return removed && HideMode.revealable(mode, altDown);
    }
}
