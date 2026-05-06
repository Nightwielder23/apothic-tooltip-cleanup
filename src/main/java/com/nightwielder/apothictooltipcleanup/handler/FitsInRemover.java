package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.LiteralContents;

import java.util.List;
import java.util.Set;

public final class FitsInRemover {
    private static final Set<String> CATEGORY_WORDS = Set.of(
            "Anything",
            "Bow", "Bows",
            "Crossbow", "Crossbows",
            "Sword", "Swords",
            "Pickaxe", "Pickaxes",
            "Axe", "Axes",
            "Shovel", "Shovels",
            "Hoe", "Hoes",
            "Trident", "Tridents",
            "Helmet", "Helmets",
            "Chestplate", "Chestplates",
            "Leggings",
            "Boot", "Boots",
            "Shield", "Shields",
            "Heavy Weapon", "Heavy Weapons",
            "Melee Weapon", "Melee Weapons",
            "Core Armor",
            "Mining Tool", "Mining Tools"
    );

    private FitsInRemover() {}

    public static void apply(List<Component> tooltip) {
        tooltip.removeIf(c -> isUniqueLine(c) || isCategoryLine(c));
    }

    private static boolean isUniqueLine(Component component) {
        return TooltipMatcher.keyStartsWith(component, "text.apotheosis.unique");
    }

    // Category lists arrive as raw literals with no translation key. Each line is one or more
    // comma-separated category words; lines with any non-category token are left alone.
    private static boolean isCategoryLine(Component component) {
        if (!(component.getContents() instanceof LiteralContents)) return false;
        String text = component.getString().trim();
        if (text.isEmpty()) return false;
        for (String token : text.split(",")) {
            if (!CATEGORY_WORDS.contains(token.trim())) return false;
        }
        return true;
    }
}
