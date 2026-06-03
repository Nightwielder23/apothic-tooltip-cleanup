package com.nightwielder.apothictooltipcleanup;

import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

// Mod entry point for 1.19.2 Forge that registers the client config. The tooltip handlers self
// register on the Forge bus via @Mod.EventBusSubscriber.
@Mod(ApothicTooltipCleanup.MODID)
public class ApothicTooltipCleanup {
    public static final String MODID = "apothic_tooltip_cleanup";

    public ApothicTooltipCleanup() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
    }
}
