package com.nightwielder.apothictooltipcleanup;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import org.slf4j.Logger;

// Mod entry point (1.19.2 Forge). Phase 2 stub: registers the empty client config only.
// Tooltip handlers are added back in a later port phase.
@Mod(ApothicTooltipCleanup.MODID)
public class ApothicTooltipCleanup {
    public static final String MODID = "apothic_tooltip_cleanup";
    public static final Logger LOGGER = LogUtils.getLogger();

    public ApothicTooltipCleanup() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, Config.SPEC);
        LOGGER.info("Apothic Tooltip Cleanup loaded (1.19.2 port scaffold).");
    }
}
