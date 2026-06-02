package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// Recolors the item name line to the configured rarity color.
public final class RarityColorOverride {
    private RarityColorOverride() {}

    // Behavior:
    //  - parses the hex and applies it to the first line (the item name).
    // Parameters:
    //  - stack: the item the tooltip is for
    //  - tooltip: the lines being shown, edited in place
    //  - hexColor: the 0xRRGGBB color to apply
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

    // the affixed name has colored siblings, so a top-level color gets masked.
    // walk the whole tree and force the color on every piece.
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
