package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;

public final class AffixMarkerStripper {

    private AffixMarkerStripper() {}

    // Allowlisted markers Apoth 8.5.3 appends to affix descriptions: the cooldown
    // (affix.apotheosis.cooldown, "[⌛ MM:SS]") and the stacking tag (affix.apotheosis.stacking,
    // "[Stacking]"). Stoneforming-style in-prose brackets ([Stone], [Cobblestone], etc.) are
    // intentionally NOT matched - they live elsewhere in the tree.
    // English-only short-circuit: the "[Stacking]" literal is the en_us rendering of
    // affix.apotheosis.stacking; localized clients see translated text and the codepoint check
    // skips the line. The translation-key based sibling match is locale-agnostic, but the
    // short-circuit gates entry.
    public static void apply(List<Component> tooltip) {
        if (!Config.HIDE_AFFIX_EXTRAS.get()) return;
        if (Screen.hasAltDown()) return;

        for (Component line : tooltip) {
            if (!TooltipMatcher.keyStartsWith(line, "text.apotheosis.dot_prefix")) continue;
            String text = line.getString();
            if (text.indexOf('⌛') < 0 && !text.contains("[Stacking]")) continue;

            if (!(line.getContents() instanceof TranslatableContents tc)) continue;
            Object[] args = tc.getArgs();
            if (args.length == 0 || !(args[0] instanceof Component inner)) continue;

            List<Component> siblings = inner.getSiblings();
            for (int i = siblings.size() - 1; i >= 0; i--) {
                String key = TooltipMatcher.getKey(siblings.get(i));
                if (!"affix.apotheosis.cooldown".equals(key) && !"affix.apotheosis.stacking".equals(key)) continue;
                siblings.remove(i);
                if (i - 1 >= 0 && isLiteralSpace(siblings.get(i - 1))) {
                    siblings.remove(i - 1);
                    i--;
                }
            }
        }
    }

    private static boolean isLiteralSpace(Component c) {
        return c.getContents() instanceof PlainTextContents pt && " ".equals(pt.text());
    }
}
