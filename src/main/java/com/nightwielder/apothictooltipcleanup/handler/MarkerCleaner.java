package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import net.minecraft.network.chat.Component;

import java.util.List;

// Removes any line still showing the raw APOTH_REMOVE_MARKER text. The cleanup stays off by default and
// is only worth enabling if Apotheosis lets the marker leak through.
public final class MarkerCleaner {
    private MarkerCleaner() {}

    public static void apply(List<Component> tooltip) {
        if (!Config.HIDE_APOTH_MARKER.get()) {
            return;
        }
        tooltip.removeIf(c -> c.getString().contains("APOTH_REMOVE_MARKER"));
    }
}
