package com.nightwielder.apothictooltipcleanup;

import net.minecraftforge.common.ForgeConfigSpec;

// Phase 2 stub: an empty client config spec so the mod can register a config without crashing.
// Real config entries are added back in a later port phase.
public class Config {
    public static final ForgeConfigSpec SPEC;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        // No entries yet. The spec must still be built so registerConfig has something valid.
        SPEC = builder.build();
    }

    private Config() {}
}
