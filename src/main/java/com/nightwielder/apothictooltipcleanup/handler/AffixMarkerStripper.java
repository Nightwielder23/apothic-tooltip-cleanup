package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.HideMode;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;

// Strips the [⌛ MM:SS] cooldown marker and the [Stacking] tag off affix bullets. Apoth attaches them
// as siblings of the bullet's inner component, so a regex over the string never sees them. We match
// the siblings by translation key. The ⌛/[Stacking] check is a quick English-only pre-filter.
public final class AffixMarkerStripper {
    private static final String COOLDOWN_KEY = "affix.apotheosis.cooldown";
    private static final String STACKING_KEY = "affix.apotheosis.stacking";

    private AffixMarkerStripper() {}

    // Behavior:
    //  - removes the cooldown/stacking markers when the mode hides them (see HideMode)
    // Parameters:
    //  - tooltip: the lines being shown, edited in place
    // Returns:
    //  - true if it hid an alt-revealable marker
    public static boolean apply(List<Component> tooltip) {
        String mode = Config.AFFIX_EXTRAS_MODE.get();
        boolean altDown = Screen.hasAltDown();
        if (!HideMode.hides(mode, altDown)) return false;

        boolean stripped = false;
        for (Component line : tooltip) {
            if (!TooltipMatcher.isBulletPrefix(line)) continue;
            String text = line.getString();
            if (text.indexOf('⌛') < 0 && !text.contains("[Stacking]")) continue;
            if (!(line.getContents() instanceof TranslatableContents tc)) continue;
            Object[] args = tc.getArgs();
            if (args.length == 0 || !(args[0] instanceof Component inner)) continue;

            // Reverse walk so removals don't shift indices. Also drops the leading space before a
            // stripped marker.
            List<Component> siblings = inner.getSiblings();
            for (int i = siblings.size() - 1; i >= 0; i--) {
                String key = TooltipMatcher.getKey(siblings.get(i));
                if (!COOLDOWN_KEY.equals(key) && !STACKING_KEY.equals(key)) continue;
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
        return component.getContents() instanceof PlainTextContents pt && " ".equals(pt.text());
    }
}
