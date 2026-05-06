package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class DurabilityHider {
    private static final String DURABLE_KEY = "affix.apotheosis:durable.desc";

    private DurabilityHider() {}

    public static void apply(List<Component> tooltip) {
        tooltip.removeIf(c -> TooltipMatcher.keyEqualsRecursive(c, DURABLE_KEY));
    }
}
