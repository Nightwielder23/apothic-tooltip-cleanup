package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.ApothicTooltipCleanup;
import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.ApotheosisDetector;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

// Client tooltip handler that runs each item tooltip through the cleanup handlers only when
// Apotheosis is loaded. Lowest priority so other mods have already run.
@Mod.EventBusSubscriber(modid = ApothicTooltipCleanup.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TooltipHandler {

    // Runs each handler over the tooltip in order. AltExpandHandler adds the prompt at the end.
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.isEmpty()) {
            return;
        }
        if (!ApotheosisDetector.isApotheosisLoaded()) {
            return;
        }

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

        if (Config.HIDE_APOTH_MARKER.get()) {
            MarkerCleaner.apply(tooltip);
        }
    }
}
