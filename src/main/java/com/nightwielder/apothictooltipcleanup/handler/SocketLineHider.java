package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.HideMode;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.List;

// Hides the "Socketed (X)" affix line that Apoth 6.x adds to socketed items. The socket affix has no
// name, only this description, and Apoth wraps it in a dot_prefix bullet, so the affix key sits in
// arg[0] of the bullet rather than at the top level where getKey would see it.
public final class SocketLineHider {
    private static final String SOCKET_DESC_KEY = "affix.apotheosis:socket.desc";

    private SocketLineHider() {}

    // Drops the socket line when the mode hides it and returns true if the hidden line is alt revealable.
    public static boolean apply(List<Component> tooltip) {
        String mode = Config.SOCKETED_LINE_MODE.get();
        boolean altDown = Screen.hasAltDown();
        if (!HideMode.hides(mode, altDown)) {
            return false;
        }
        boolean removed = tooltip.removeIf(SocketLineHider::isSocketLine);
        return removed && HideMode.revealable(mode, altDown);
    }

    static boolean isSocketLine(Component component) {
        if (!TooltipMatcher.isBulletPrefix(component)) {
            return false;
        }
        if (!(component.getContents() instanceof TranslatableContents tc)) {
            return false;
        }
        Object[] args = tc.getArgs();
        if (args.length == 0 || !(args[0] instanceof Component inner)) {
            return false;
        }
        return SOCKET_DESC_KEY.equals(TooltipMatcher.getKey(inner));
    }
}
