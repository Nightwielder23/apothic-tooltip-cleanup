package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.ApothicTooltipCleanup;
import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.ApotheosisDetector;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

@EventBusSubscriber(modid = ApothicTooltipCleanup.MODID, value = Dist.CLIENT)
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

        if (Config.COMPACT_GEM_DISPLAY.get()) {
            GemDisplayCompactor.apply(stack, tooltip);
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

        if (Config.HIDE_AFFIX_COOLDOWNS.get()) {
            AffixCooldownStripper.apply(tooltip);
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

        if (Config.HIDE_APOTH_MARKER.get()) {
            MarkerCleaner.apply(tooltip);
        }
    }

    private static String resolveRarityHex(ItemStack stack) {
        DynamicHolder<LootRarity> holder = AffixHelper.getRarity(stack);
        if (!holder.isBound()) return null;
        String rarity = holder.getId().getPath();
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
