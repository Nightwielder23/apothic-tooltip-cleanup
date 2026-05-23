package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.ApothicTooltipCleanup;
import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.ApotheosisDetector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(modid = ApothicTooltipCleanup.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipHandler {

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) return;
        if (!ApotheosisDetector.isApotheosisLoaded()) return;

        List<Component> tooltip = event.getToolTip();

        FitsInRemover.apply(tooltip);

        List<? extends String> hiddenIds = Config.HIDDEN_AFFIX_IDS.get();
        if (!hiddenIds.isEmpty()) {
            HiddenAffixHandler.apply(tooltip, hiddenIds);
        }

        if (Config.HIDE_SOURCE_LINE.get()) {
            SourceLineRemover.apply(tooltip);
        }

        if (Config.DISABLE_SUMMARIZATION.get()) {
            SummarizationDisabler.apply(tooltip);
        }

        if (Config.CLEAN_AFFIX_PREFIXES.get()) {
            PrefixCleaner.apply(tooltip);
        }

        String sortOrder = Config.AFFIX_SORT_ORDER.get();
        if (!"default".equals(sortOrder)) {
            AffixSorter.apply(tooltip, sortOrder);
        }

        if (Config.MERGE_EMPTY_SOCKETS.get()) {
            EmptySocketMerger.apply(tooltip);
        }

        if (Config.DISABLE_POTION_DESCRIPTIONS.get() && ApotheosisDetector.isApothicAttributesLoaded()) {
            PotionDescriptionToggle.apply(stack, tooltip);
        }

        AltExpandHandler.apply(tooltip);

        if (Config.HIDE_DURABILITY_BONUS.get()) {
            DurabilityHider.apply(tooltip);
        }

        if (Config.RARITY_COLORS_ENABLED.get()) {
            String hex = resolveRarityHex(stack);
            if (hex != null) {
                RarityColorOverride.apply(stack, tooltip, hex);
            }
        }

        if (Config.HIDE_AFFIX_COOLDOWNS.get()) {
            AffixCooldownHider.apply(tooltip);
        }

        if (Config.HIDE_APOTH_MARKER.get()) {
            MarkerCleaner.apply(tooltip);
        }
    }

    private static String resolveRarityHex(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains("affix_data")) return null;
        String rarity = tag.getCompound("affix_data").getString("rarity");
        if (rarity == null || rarity.isEmpty()) return null;
        // Strip whatever namespace produced the rarity (apotheosis:, apotheotic_additions:, etc).
        int colon = rarity.indexOf(':');
        if (colon >= 0) {
            rarity = rarity.substring(colon + 1);
        }
        // Unknown rarities (e.g. apotheotic_additions:esoteric) fall through to ancient as the highest tier.
        return switch (rarity) {
            case "common" -> Config.COMMON.get();
            case "uncommon" -> Config.UNCOMMON.get();
            case "rare" -> Config.RARE.get();
            case "epic" -> Config.EPIC.get();
            case "mythic" -> Config.MYTHIC.get();
            default -> Config.ANCIENT.get();
        };
    }
}
