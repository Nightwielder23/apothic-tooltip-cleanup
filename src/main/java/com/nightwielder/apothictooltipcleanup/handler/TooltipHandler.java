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

// Client tooltip handler. Runs each item tooltip through the cleanup handlers, but only when
// Apotheosis is loaded. Lowest priority so other mods have already run.
@EventBusSubscriber(modid = ApothicTooltipCleanup.MODID, value = Dist.CLIENT)
public class TooltipHandler {

    // Behavior:
    //  - runs each handler over the tooltip in order. AltExpandHandler adds the prompt at the end.
    // Parameters:
    //  - event: the tooltip event, edited in place.
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

        // each handler returns true if it hid something Alt can reveal. OR them together so
        // AltExpandHandler knows whether to add the prompt.
        boolean anyHidden = false;
        anyHidden |= PrefixCleaner.apply(tooltip);
        anyHidden |= SourceLineRemover.apply(tooltip);
        anyHidden |= SummarizationDisabler.apply(tooltip);
        if (ApotheosisDetector.isApothicAttributesLoaded()) {
            anyHidden |= PotionDescriptionToggle.apply(stack, tooltip);
        }
        anyHidden |= AffixMarkerStripper.apply(tooltip);
        anyHidden |= DurabilityHider.apply(tooltip);

        AltExpandHandler.apply(tooltip, anyHidden);

        if (Config.RARITY_COLORS_ENABLED.get()) {
            String hex = rarityHex(stack);
            if (hex != null) {
                RarityColorOverride.apply(stack, tooltip, hex);
            }
        }

        if (Config.HIDE_APOTH_MARKER.get()) {
            MarkerCleaner.apply(tooltip);
        }
    }

    // Hex color for the item's rarity, or null if it has none. Unknown rarities fall back to ancient.
    private static String rarityHex(ItemStack stack) {
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
