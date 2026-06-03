package com.nightwielder.apothictooltipcleanup;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

// Mod entry point that registers the client config. The tooltip work is in the handlers.
@Mod(ApothicTooltipCleanup.MODID)
public class ApothicTooltipCleanup {
    public static final String MODID = "apothic_tooltip_cleanup";

    public ApothicTooltipCleanup(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }
}
