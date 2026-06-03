package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.HideMode;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

// Hides the "ignores X% of durability damage" line. The text is buried in a translation argument so
// matching by key misses it. This matches the rendered string instead and is English only.
public final class DurabilityHider {
    private DurabilityHider() {}

    // Drops the durability line when the mode hides it and returns true if the hidden line is alt revealable.
    public static boolean apply(List<Component> tooltip) {
        String mode = Config.DURABILITY_BONUS_MODE.get();
        boolean altDown = Screen.hasAltDown();
        if (!HideMode.hides(mode, altDown)) {
            return false;
        }
        boolean removed = tooltip.removeIf(DurabilityHider::isDurability);
        return removed && HideMode.revealable(mode, altDown);
    }

    static boolean isDurability(Component component) {
        if (!TooltipMatcher.isBulletPrefix(component)) {
            return false;
        }
        String text = component.getString();
        return text.contains("ignores") && text.contains("durability damage");
    }
}
