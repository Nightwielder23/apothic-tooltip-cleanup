package com.nightwielder.apothictooltipcleanup.handler;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class RarityColorOverride {
    private RarityColorOverride() {}

    public static void apply(ItemStack stack, List<Component> tooltip, String hexColor) {
        if (stack.isEmpty() || tooltip.isEmpty() || hexColor == null) return;
        int rgb;
        try {
            rgb = Integer.decode(hexColor);
        } catch (NumberFormatException ignored) {
            return;
        }
        Component name = tooltip.get(0);
        Style restyled = name.getStyle().withColor(TextColor.fromRgb(rgb));
        tooltip.set(0, name.copy().setStyle(restyled));
    }
}
