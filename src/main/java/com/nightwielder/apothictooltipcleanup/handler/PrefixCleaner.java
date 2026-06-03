package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.HideMode;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// Strips the Apotheosis affix prefix and suffix from the item name, leaving the base item name. Apoth
// renames affixed items with a misc.apotheosis.affix_name template, so tooltip line 0 carries that key.
public final class PrefixCleaner {
    private PrefixCleaner() {}

    // Replaces the affixed item name with the plain base name when the mode hides it and returns true
    // if a hidden name is alt revealable.
    public static boolean apply(ItemStack stack, List<Component> tooltip) {
        String mode = Config.AFFIX_PREFIXES_MODE.get();
        boolean altDown = Screen.hasAltDown();
        if (!HideMode.hides(mode, altDown)) {
            return false;
        }
        if (tooltip.isEmpty()) {
            return false;
        }
        Component name = tooltip.get(0);
        if (!TooltipMatcher.keyStartsWith(name, "misc.apotheosis.affix_name")) {
            return false;
        }
        Component base = stack.getItem().getName(stack);
        tooltip.set(0, Component.empty().append(base).withStyle(name.getStyle()));
        return HideMode.revealable(mode, altDown);
    }
}
