package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.HideMode;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;

import java.util.List;

// Hides the potion-style affix descriptions Apothic Attributes adds to potions.
public final class PotionDescriptionToggle {
    private PotionDescriptionToggle() {}

    // Behavior:
    //  - on potions, drops the attributeslib description lines when the mode hides them
    // Parameters:
    //  - stack: the item the tooltip is for
    //  - tooltip: the lines being shown, edited in place
    // Returns:
    //  - true if it hid an alt-revealable line
    public static boolean apply(ItemStack stack, List<Component> tooltip) {
        String mode = Config.POTION_DESCRIPTIONS_MODE.get();
        boolean altDown = Screen.hasAltDown();
        if (!HideMode.hides(mode, altDown)) return false;
        if (!(stack.getItem() instanceof PotionItem)) return false;
        boolean removed = tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, "attributeslib:"));
        return removed && HideMode.revealable(mode, altDown);
    }
}
