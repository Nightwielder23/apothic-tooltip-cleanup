package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class PrefixCleaner {
    private PrefixCleaner() {}

    public static void apply(List<Component> tooltip) {
        if (!Config.CLEAN_AFFIX_PREFIXES.get()) return;
        tooltip.removeIf(c -> TooltipMatcher.keyStartsWith(c, "text.apotheosis.affix_type.while_held")
                || TooltipMatcher.keyStartsWith(c, "text.apotheosis.affix_type.on_hit")
                || TooltipMatcher.keyStartsWith(c, "text.apotheosis.affix_type.on_block")
                || TooltipMatcher.keyStartsWith(c, "text.apotheosis.affix_type.passive"));
    }
}
