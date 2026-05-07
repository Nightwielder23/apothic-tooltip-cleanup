package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;

import java.util.List;

public final class PotionDescriptionToggle {
    private PotionDescriptionToggle() {}

    public static void apply(ItemStack stack, List<Component> tooltip) {
        if (!(stack.getItem() instanceof PotionItem)) return;
        tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, "apothic_attributes:"));
    }
}
