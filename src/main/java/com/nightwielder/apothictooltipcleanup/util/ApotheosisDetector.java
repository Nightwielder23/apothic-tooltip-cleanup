package com.nightwielder.apothictooltipcleanup.util;

import net.minecraftforge.fml.ModList;

// Checks whether the optional integration mods are installed. Cached since the mod list never
// changes after load.
public final class ApotheosisDetector {
    private static Boolean apotheosisLoaded;
    private static Boolean apothicAttributesLoaded;

    private ApotheosisDetector() {}

    public static boolean isApotheosisLoaded() {
        if (apotheosisLoaded == null) {
            apotheosisLoaded = ModList.get().isLoaded("apotheosis");
        }
        return apotheosisLoaded;
    }

    public static boolean isApothicAttributesLoaded() {
        if (apothicAttributesLoaded == null) {
            apothicAttributesLoaded = ModList.get().isLoaded("apothic_attributes");
        }
        return apothicAttributesLoaded;
    }
}
