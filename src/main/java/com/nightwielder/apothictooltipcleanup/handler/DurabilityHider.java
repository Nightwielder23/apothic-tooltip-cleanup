package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.HideMode;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

// Hides the "ignores X% of durability damage" line. The text is buried in a translation argument,
// so matching by key misses it. Match the rendered string instead. English only.
public final class DurabilityHider {
    private DurabilityHider() {}

    // Behavior:
    //  - drops the durability line when the mode hides it (see HideMode)
    // Parameters:
    //  - tooltip: the lines being shown, edited in place
    // Returns:
    //  - true if it hid an alt-revealable line
    public static boolean apply(List<Component> tooltip) {
        String mode = Config.DURABILITY_BONUS_MODE.get();
        boolean altDown = Screen.hasAltDown();
        if (!HideMode.hides(mode, altDown)) return false;
        boolean removed = tooltip.removeIf(DurabilityHider::isDurability);
        return removed && HideMode.revealable(mode, altDown);
    }

    static boolean isDurability(Component component) {
        if (!TooltipMatcher.isBulletPrefix(component)) return false;
        String text = component.getString();
        return text.contains("ignores") && text.contains("durability damage");
    }
}
