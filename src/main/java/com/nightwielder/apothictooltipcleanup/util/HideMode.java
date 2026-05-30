package com.nightwielder.apothictooltipcleanup.util;

import java.util.Arrays;
import java.util.List;

// The show/alt/delete toggle the hide features share. show keeps the line. alt hides it unless Alt
// is held. delete hides it for good.
//
// A handler drops its line when hides() is true. It returns true only when revealable() is also
// true, which is what adds the "Hold Alt for full details" prompt. delete never qualifies since Alt
// can't undo it.
public final class HideMode {
    public static final String SHOW = "show";
    public static final String ALT = "alt";
    public static final String DELETE = "delete";
    public static final List<String> OPTIONS = Arrays.asList(SHOW, ALT, DELETE);

    private HideMode() {}

    // Behavior:
    //  - drops the line based on the mode. delete always drops it. alt drops it unless Alt is held.
    // Parameters:
    //  - mode: the show/alt/delete string from config
    //  - altDown: whether Alt is held
    // Returns:
    //  - true if the line should be dropped
    public static boolean hides(String mode, boolean altDown) {
        return DELETE.equals(mode) || (ALT.equals(mode) && !altDown);
    }

    // Behavior:
    //  - true for an alt-mode hide while Alt is up. that is the case the prompt can undo.
    // Returns:
    //  - true if Alt can bring the line back
    public static boolean revealable(String mode, boolean altDown) {
        return ALT.equals(mode) && !altDown;
    }
}
