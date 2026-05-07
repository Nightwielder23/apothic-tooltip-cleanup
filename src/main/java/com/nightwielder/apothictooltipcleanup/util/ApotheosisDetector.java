package com.nightwielder.apothictooltipcleanup.util;

import net.minecraftforge.fml.ModList;

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
