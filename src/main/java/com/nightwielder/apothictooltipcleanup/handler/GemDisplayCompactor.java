package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TridentItem;

import java.util.List;

public final class GemDisplayCompactor {
    private GemDisplayCompactor() {}

    public static void apply(ItemStack stack, List<Component> tooltip) {
        String category = resolveCategory(stack);
        if (category == null) return;
        String matchSuffix = "." + category + ".desc";
        tooltip.removeIf(c -> {
            String key = TooltipMatcher.getKey(c);
            if (key == null) return false;
            if (!key.startsWith("bonus.apotheosis:")) return false;
            return !key.endsWith(matchSuffix);
        });
    }

    private static String resolveCategory(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof SwordItem) return "sword";
        if (item instanceof PickaxeItem) return "pickaxe";
        if (item instanceof ShovelItem) return "shovel";
        if (item instanceof AxeItem) return "axe";
        if (item instanceof BowItem) return "bow";
        if (item instanceof CrossbowItem) return "crossbow";
        if (item instanceof TridentItem) return "trident";
        if (item instanceof ShieldItem) return "shield";
        if (item instanceof ArmorItem armor) {
            EquipmentSlot slot = armor.getEquipmentSlot();
            return switch (slot) {
                case HEAD -> "helmet";
                case CHEST -> "chestplate";
                case LEGS -> "leggings";
                case FEET -> "boots";
                default -> null;
            };
        }
        return null;
    }
}
