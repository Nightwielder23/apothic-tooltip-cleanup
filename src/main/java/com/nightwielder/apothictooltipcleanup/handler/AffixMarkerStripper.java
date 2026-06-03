package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.HideMode;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;

// Strips the cooldown marker and [Stacking] tag off affix bullets. Apoth attaches them as siblings
// of the bullet inner component where a string regex never sees them, so the siblings are matched by
// translation key.
public final class AffixMarkerStripper {
    private static final String COOLDOWN_KEY = "affix.apotheosis.cooldown";
    private static final String STACKING_KEY = "affix.apotheosis.stacking";

    private AffixMarkerStripper() {}

    // Removes the cooldown and stacking markers when the mode hides them and returns true if alt revealable.
    public static boolean apply(List<Component> tooltip) {
        String mode = Config.AFFIX_EXTRAS_MODE.get();
        boolean altDown = Screen.hasAltDown();
        if (!HideMode.hides(mode, altDown)) {
            return false;
        }

        boolean stripped = false;
        for (Component line : tooltip) {
            if (!TooltipMatcher.isBulletPrefix(line)) {
                continue;
            }
            if (!(line.getContents() instanceof TranslatableContents tc)) {
                continue;
            }
            Object[] args = tc.getArgs();
            if (args.length == 0 || !(args[0] instanceof Component inner)) {
                continue;
            }

            // Reverse walk so removals do not shift indices. Also drops the leading space before a marker.
            List<Component> siblings = inner.getSiblings();
            for (int i = siblings.size() - 1; i >= 0; i--) {
                String key = TooltipMatcher.getKey(siblings.get(i));
                if (!COOLDOWN_KEY.equals(key) && !STACKING_KEY.equals(key)) {
                    continue;
                }
                siblings.remove(i);
                stripped = true;
                if (i > 0 && isLiteralSpace(siblings.get(i - 1))) {
                    siblings.remove(i - 1);
                    i--;
                }
            }
        }
        return stripped && HideMode.revealable(mode, altDown);
    }

    private static boolean isLiteralSpace(Component component) {
        return component.getContents() instanceof LiteralContents lc && " ".equals(lc.text());
    }
}
