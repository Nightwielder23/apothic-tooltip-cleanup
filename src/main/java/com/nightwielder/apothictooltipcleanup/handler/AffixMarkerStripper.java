package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.HideMode;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;

// Strips the cooldown marker and [Stacking] tag off affix and gem bonus bullets, matched by translation
// key. Apoth attaches a marker either as a sibling of the bullet's inner component, or as an arg of a
// bonus description translatable; the second kind can only be dropped by rebuilding that translatable.
public final class AffixMarkerStripper {
    private static final String COOLDOWN_KEY = "affix.apotheosis.cooldown";
    private static final String STACKING_KEY = "affix.apotheosis.stacking";

    private AffixMarkerStripper() {}

    private record Result(boolean stripped, Component component) {}

    // Removes the cooldown and stacking markers when the mode hides them and returns true if alt revealable.
    public static boolean apply(List<Component> tooltip) {
        String mode = Config.AFFIX_EXTRAS_MODE.get();
        boolean altDown = Screen.hasAltDown();
        if (!HideMode.hides(mode, altDown)) {
            return false;
        }

        boolean stripped = false;
        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
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
            Result res = stripMarkers(inner);
            stripped |= res.stripped();
            // inner is arg[0] of the dot_prefix line, so a rebuilt inner has to be threaded back in here.
            if (res.component() != inner) {
                Object[] newArgs = args.clone();
                newArgs[0] = res.component();
                tooltip.set(i, rebuild(tc.getKey(), newArgs, line));
            }
        }
        return stripped && HideMode.revealable(mode, altDown);
    }

    // Walks a bullet's component subtree. Sibling markers are spliced out in place, but an arg marker can
    // only go by rebuilding its translatable without it, so the rebuilt node is returned for the caller to swap in.
    private static Result stripMarkers(Component node) {
        boolean stripped = false;

        // Reverse walk so removals do not shift indices. Also drops the leading space before a marker.
        List<Component> siblings = node.getSiblings();
        for (int i = siblings.size() - 1; i >= 0; i--) {
            Component sibling = siblings.get(i);
            if (isMarker(sibling)) {
                siblings.remove(i);
                stripped = true;
                if (i > 0 && isLiteralSpace(siblings.get(i - 1))) {
                    siblings.remove(i - 1);
                    i--;
                }
            } else {
                Result res = stripMarkers(sibling);
                stripped |= res.stripped();
                if (res.component() != sibling) {
                    siblings.set(i, res.component());
                }
            }
        }

        if (node.getContents() instanceof TranslatableContents tc) {
            Object[] args = tc.getArgs();
            Object[] newArgs = null;
            for (int a = 0; a < args.length; a++) {
                if (!(args[a] instanceof Component argComp)) {
                    continue;
                }
                if (isMarker(argComp)) {
                    if (newArgs == null) {
                        newArgs = args.clone();
                    }
                    newArgs[a] = Component.empty();
                    if (a > 0 && newArgs[a - 1] instanceof Component prev && isLiteralSpace(prev)) {
                        newArgs[a - 1] = Component.empty();
                    }
                    stripped = true;
                } else {
                    Result res = stripMarkers(argComp);
                    stripped |= res.stripped();
                    if (res.component() != argComp) {
                        if (newArgs == null) {
                            newArgs = args.clone();
                        }
                        newArgs[a] = res.component();
                    }
                }
            }
            if (newArgs != null) {
                return new Result(stripped, rebuild(tc.getKey(), newArgs, node));
            }
        }

        return new Result(stripped, node);
    }

    // Keeps the original style and the already stripped siblings, since translatable args are a fixed array
    // and cannot be edited in place.
    private static Component rebuild(String key, Object[] args, Component original) {
        MutableComponent rebuilt = Component.translatable(key, args).setStyle(original.getStyle());
        for (Component sibling : original.getSiblings()) {
            rebuilt.append(sibling);
        }
        return rebuilt;
    }

    private static boolean isMarker(Component component) {
        String key = TooltipMatcher.getKey(component);
        return COOLDOWN_KEY.equals(key) || STACKING_KEY.equals(key);
    }

    private static boolean isLiteralSpace(Component component) {
        return component.getContents() instanceof PlainTextContents pt && " ".equals(pt.text());
    }
}
