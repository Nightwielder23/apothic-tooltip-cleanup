package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.ApothicTooltipCleanup;
import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.ApotheosisDetector;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// AnvilUpdateEvent fires whenever the player edits the anvil inputs. Apotheosis decorates an
// affixed item's name dynamically from affix_data NBT every render, so the user's custom name
// gets visually buried under the prefix/suffix unless we replace the output's display.Name.
// LOWEST priority lets vanilla and any other anvil mod (Apoth itself, repair mods, etc) settle
// first so we sit on top of whatever output they computed and stamp the custom name onto it
// without touching the cost or material cost they decided.
@Mod.EventBusSubscriber(modid = ApothicTooltipCleanup.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class AnvilRenameOverride {
    private AnvilRenameOverride() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onAnvilUpdate(AnvilUpdateEvent event) {
        if (!Config.OVERRIDE_AFFIX_NAME_ON_RENAME.get()) return;
        if (!ApotheosisDetector.isApotheosisLoaded()) return;

        // Combined operations (rename + enchant book, rename + repair material) fall through to
        // vanilla / Apoth untouched. Our override only fires on pure rename operations so we
        // never accidentally drop a player's book or repair material.
        if (!event.getRight().isEmpty()) return;

        ItemStack left = event.getLeft();
        if (left.isEmpty()) return;

        String name = event.getName();
        if (name == null || name.isEmpty()) return;

        // Only intervene on affixed items. Anything else falls through to vanilla / Apoth.
        if (!left.hasTag() || !left.getTag().contains("affix_data")) return;

        // AnvilUpdateEvent also fires on initial open with event.getName() equal to the item's
        // current display name. Comparing the typed name against left.getHoverName() doesn't work
        // server-side: Apoth decorates the name via ItemTooltipEvent at render time, so the
        // server-visible hover name is the plain vanilla name and never matches the decorated
        // string the user actually sees and re-types.
        // Cost is the reliable signal. Vanilla's anvil sets cost=0 when the operation is a no-op
        // (same name as current display, no material consumed). With the right slot already
        // confirmed empty above, cost=0 means vanilla considers nothing meaningful to be
        // happening, so writing display.Name now would freeze Apoth's dynamic decoration as a
        // literal string and break future name refreshes (reforging, augmenting).
        if (event.getCost() == 0) return;

        // Vanilla / Apoth already computed an output (with cost and material cost). Copy it,
        // restamp the display name, and put it back. If nothing upstream produced an output the
        // operation isn't valid to begin with; bail rather than synthesize, which would otherwise
        // hand the player a free rename (we never call setCost, so the synthesized output would
        // cost 0).
        ItemStack currentOutput = event.getOutput();
        if (currentOutput.isEmpty()) return;
        ItemStack newOutput = currentOutput.copy();
        newOutput.setHoverName(Component.literal(name));
        event.setOutput(newOutput);
    }
}
