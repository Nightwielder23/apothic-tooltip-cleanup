package com.nightwielder.apothictooltipcleanup.util;

import java.util.Arrays;
import java.util.List;

// The show/alt/delete toggle shared by the hide features. show keeps the line, alt hides it unless
// Alt is held, and delete hides it permanently.
public final class HideMode {
    public static final String SHOW = "show";
    public static final String ALT = "alt";
    public static final String DELETE = "delete";
    public static final List<String> OPTIONS = Arrays.asList(SHOW, ALT, DELETE);

    private HideMode() {}

    // Returns true when the line should be dropped for this mode and Alt state.
    public static boolean hides(String mode, boolean altDown) {
        return DELETE.equals(mode) || (ALT.equals(mode) && !altDown);
    }

    // Returns true for an alt mode hide while Alt is up, the case the prompt can undo.
    public static boolean revealable(String mode, boolean altDown) {
        return ALT.equals(mode) && !altDown;
    }
}
