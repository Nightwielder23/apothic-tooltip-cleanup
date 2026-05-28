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

        String sortOrder = Config.AFFIX_SORT_ORDER.get();
        if (!"default".equals(sortOrder)) {
            AffixSorter.apply(tooltip, sortOrder);
        }

        if (Config.MERGE_EMPTY_SOCKETS.get()) {
            EmptySocketMerger.apply(tooltip);
        }

        // Each hideable feature runs in show / alt / delete mode and reports whether it hid
        // Alt-revealable content this pass. The accumulated flag drives the single "Hold Alt for
        // full details" prompt added by AltExpandHandler. delete-mode and show-mode hides never set
        // it, since Alt cannot bring those back.
        boolean anyAltRevealableHidden = false;
        anyAltRevealableHidden |= PrefixCleaner.apply(tooltip);
        anyAltRevealableHidden |= SourceLineRemover.apply(tooltip);
        anyAltRevealableHidden |= SummarizationDisabler.apply(tooltip);
        if (ApotheosisDetector.isApothicAttributesLoaded()) {
            anyAltRevealableHidden |= PotionDescriptionToggle.apply(stack, tooltip);
        }
        anyAltRevealableHidden |= AffixMarkerStripper.apply(tooltip);
        anyAltRevealableHidden |= DurabilityHider.apply(tooltip);

        AltExpandHandler.apply(tooltip, anyAltRevealableHidden);

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
