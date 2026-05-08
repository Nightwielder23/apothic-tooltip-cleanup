package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class HiddenAffixHandler {
    private HiddenAffixHandler() {}

    public static void apply(List<Component> tooltip, List<? extends String> hiddenIds) {
        if (hiddenIds == null || hiddenIds.isEmpty()) return;
        tooltip.removeIf(c -> {
            String key = TooltipMatcher.getKey(c);
            if (key == null || !key.startsWith("affix.apotheosis:")) return false;
            for (String id : hiddenIds) {
                if (key.equals("affix.apotheosis:" + id + ".desc")) return true;
            }
            return false;
        });
    }
}
