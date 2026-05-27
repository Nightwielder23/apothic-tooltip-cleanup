package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;

// Apoth 7.x attaches the [⌛ MM:SS] cooldown marker (affix.apotheosis.cooldown) and the
// [Stacking] tag (affix.apotheosis.stacking) as siblings of the bullet's arg[0] component, not
// as inline text inside arg[0]. A regex over arg[0].getString() never sees them and silently
// no-ops. We match siblings by translation key and remove them directly. Translation-key
// matching is locale-agnostic, and the parenthesized in-prose duration like "(00:10)" survives
// automatically: it lives inside arg[0]'s arg[0] (the affix description) rather than as a
// sibling, so the sibling pass cannot touch it. The codepoint pre-filter on rendered text is
// the only English-only piece; it only gates extra work, not correctness.
public final class AffixMarkerStripper {
    private static final String COOLDOWN_KEY = "affix.apotheosis.cooldown";
    private static final String STACKING_KEY = "affix.apotheosis.stacking";

    private AffixMarkerStripper() {}

    public static void apply(List<Component> tooltip) {
        if (!Config.HIDE_AFFIX_EXTRAS.get()) return;
        // Holding Alt reveals the unstripped line, matching AltExpandHandler's reveal-on-hold UX.
        if (Screen.hasAltDown()) return;
        for (Component line : tooltip) {
            if (!TooltipMatcher.isBulletPrefix(line)) continue;
            String text = line.getString();
            if (text.indexOf('⌛') < 0 && !text.contains("[Stacking]")) continue;
            if (!(line.getContents() instanceof TranslatableContents tc)) continue;
            Object[] args = tc.getArgs();
            if (args.length == 0 || !(args[0] instanceof Component inner)) continue;
            stripMarkerSiblings(inner.getSiblings());
        }
    }

    // Reverse walk so removals don't invalidate later indices. When a marker sibling is removed
    // we also drop the preceding " " separator so the bullet doesn't render with a trailing space.
    private static void stripMarkerSiblings(List<Component> siblings) {
        for (int i = siblings.size() - 1; i >= 0; i--) {
            String key = TooltipMatcher.getKey(siblings.get(i));
            if (!COOLDOWN_KEY.equals(key) && !STACKING_KEY.equals(key)) continue;
            siblings.remove(i);
            if (i > 0 && isLiteralSpace(siblings.get(i - 1))) {
                siblings.remove(i - 1);
                i--;
            }
        }
    }

    private static boolean isLiteralSpace(Component component) {
        return component.getContents() instanceof LiteralContents lc && " ".equals(lc.text());
    }
}
