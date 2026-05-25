package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.ApothicTooltipCleanup;
import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.ApotheosisDetector;
import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AnvilUpdateEvent;

@EventBusSubscriber(modid = ApothicTooltipCleanup.MODID)
public final class AnvilRenameOverride {

    private AnvilRenameOverride() {}

    @SubscribeEvent
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        if (!Config.STRIP_AFFIX_NAME_ON_RENAME.get()) return;
        if (!ApotheosisDetector.isApotheosisLoaded()) return;

        ItemStack left = event.getLeft();
        if (left.isEmpty()) return;
        if (!left.has(Apoth.Components.AFFIX_NAME)) return;

        String name = event.getName();
        if (name == null || name.isEmpty()) return;

        ItemStack output = left.copy();
        output.set(DataComponents.CUSTOM_NAME, Component.literal(name));

        Style wrapperStyle = Style.EMPTY.withItalic(false);
        DynamicHolder<LootRarity> rarity = AffixHelper.getRarity(output);
        if (rarity.isBound()) {
            wrapperStyle = wrapperStyle.withColor(rarity.get().color());
        }
        AffixHelper.setName(output, Component.translatable("%2$s", "", "").withStyle(wrapperStyle));

        event.setCost(1);
        event.setMaterialCost(0);
        event.setOutput(output);
    }
}
