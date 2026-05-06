package com.nightwielder.apothictooltipcleanup.handler;

import net.minecraft.network.chat.Component;

import java.util.List;

public final class MarkerCleaner {
    private MarkerCleaner() {}

    public static void apply(List<Component> tooltip) {
        tooltip.removeIf(c -> c.getString().contains("APOTH_REMOVE_MARKER"));
    }
}
