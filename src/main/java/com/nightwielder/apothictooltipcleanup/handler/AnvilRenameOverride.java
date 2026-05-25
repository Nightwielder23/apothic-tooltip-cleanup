package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.ApothicTooltipCleanup;
import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.ApotheosisDetector;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// Apotheosis 7.4.x mixes into ItemStack.getHoverName() at RETURN (ItemStackMixin.apoth_affixItemName,
// cancellable). If the stack has affix_data.name set, the mixin wraps the returned Component into
// the affix prefix/suffix template, overriding any display.Name we set. setHoverName alone is not
// enough to suppress the wrap.
//
// To suppress wrapping without breaking affixes, rarity, or sockets, we clear just the
// affix_data.name NBT key. Apoth's own mixin self-heals to this same operation when it encounters
// bad data (the catch block on the wrap path calls tag.remove("name")), so this is the canonical
// "stop wrapping" signal on 1.20.1.
//
// Caveat: Apoth regenerates affix_data.name via AffixHelper.setName on reforge and regeneration
// events. After a reforge the wrap returns until the player anvil-renames again.
//
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

        // AnvilUpdateEvent fires on every contents change, including the initial open with
        // event.getName() pre-filled to match the current display name. Naive name-comparison
        // guards are fragile because Apoth's getHoverName mixin runs server-side too, so what
        // counts as "the current name" depends on Apoth's wrap state at the time of comparison.
        //
        // Cost is the reliable signal. Vanilla's anvil sets cost=0 when the operation is a no-op
        // (same name, no material consumed). With the right slot already confirmed empty above,
        // cost=0 means vanilla considers nothing meaningful to be happening, so intervening now
        // would be wasted work and could mis-fire on initial open.
        if (event.getCost() == 0) return;

        // Vanilla / Apoth already computed an output (with cost and material cost). Copy it,
        // restamp the display name, and put it back. If nothing upstream produced an output the
        // operation isn't valid to begin with; bail rather than synthesize, which would otherwise
        // hand the player a free rename (we never call setCost, so the synthesized output would
        // cost 0).
        ItemStack currentOutput = event.getOutput();
        if (currentOutput.isEmpty()) return;
        ItemStack newOutput = currentOutput.copy();
        // Clear the wrap-template NBT so Apoth's getHoverName mixin's gate check
        // (affixData.contains("name", TAG_STRING)) fails and the wrap path is skipped. The vanilla
        // hover name (now display.Name) flows through unwrapped; the post-skip rarity coloring
        // branch still runs so the renamed item keeps its tint.
        CompoundTag outputAffixData = newOutput.getTagElement("affix_data");
        if (outputAffixData != null) {
            outputAffixData.remove("name");
        }
        newOutput.setHoverName(Component.literal(name));
        event.setOutput(newOutput);
    }
}
