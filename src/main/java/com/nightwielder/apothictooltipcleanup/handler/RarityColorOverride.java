package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class RarityColorOverride {
    private RarityColorOverride() {}

    public static void apply(ItemStack stack, List<Component> tooltip, String hexColor) {
        if (!Config.RARITY_COLORS_ENABLED.get()) return;
        if (stack.isEmpty() || tooltip.isEmpty() || hexColor == null) return;
        int rgb;
        try {
            rgb = Integer.decode(hexColor);
        } catch (NumberFormatException ignored) {
            return;
        }
        tooltip.set(0, recolor(tooltip.get(0), TextColor.fromRgb(rgb)));
    }

    // The affixed name renders through misc.apotheosis.affix_name.three with colored siblings, so a
    // top-level setStyle is masked by their own colors. Walk siblings and force the color through.
    private static MutableComponent recolor(Component source, TextColor color) {
        MutableComponent result = source.copy();
        result.setStyle(result.getStyle().withColor(color));
        List<Component> siblings = result.getSiblings();
        for (int i = 0; i < siblings.size(); i++) {
            siblings.set(i, recolor(siblings.get(i), color));
        }
        return result;
    }
}
