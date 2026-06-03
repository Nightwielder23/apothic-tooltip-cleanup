package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// Reshapes the item name Apotheosis composes from affixes for affix_prefixes_mode. Apoth keys that
// name misc.apotheosis.affix_name on tooltip line 0 and colors that component with the rarity color.
public final class PrefixCleaner {
    private PrefixCleaner() {}

    // Rewrites line 0 for the affix_prefixes_mode. Always returns false since this feature has no alt reveal.
    public static boolean apply(ItemStack stack, List<Component> tooltip) {
        String mode = Config.AFFIX_PREFIXES_MODE.get();
        boolean prefix = "prefix".equalsIgnoreCase(mode);
        boolean vanilla = "vanilla".equalsIgnoreCase(mode);
        if (!prefix && !vanilla) {
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
        if (vanilla) {
            tooltip.set(0, base);
            return false;
        }

        // prefix: base item name in the affix rarity color, dropping the prefixes, suffixes and italics.
        // Apoth colors the affix_name component itself, not line 0's root, so dig the color out of the tree.
        TextColor color = rarityColor(name);
        if (color == null) {
            tooltip.set(0, base);
        } else {
            tooltip.set(0, base.copy().withStyle(Style.EMPTY.withColor(color)));
        }
        return false;
    }

    // Returns the first text color set on the component or any sibling, or null if none carries one.
    private static TextColor rarityColor(Component component) {
        TextColor color = component.getStyle().getColor();
        if (color != null) {
            return color;
        }
        for (Component sibling : component.getSiblings()) {
            TextColor found = rarityColor(sibling);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
