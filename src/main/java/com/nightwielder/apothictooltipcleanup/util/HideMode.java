package com.nightwielder.apothictooltipcleanup.util;

import java.util.Arrays;
import java.util.List;

// Shared three-mode toggle for hideable tooltip features:
//   show   - never hide
//   alt    - hide unless Alt is held, so the content stays reachable
//   delete - always hide, Alt cannot bring it back
// The decisions take altDown as a parameter so this class stays free of client-only types and each
// caller reads the key state once per tooltip pass.
public final class HideMode {
    public static final String SHOW = "show";
    public static final String ALT = "alt";
    public static final String DELETE = "delete";
    public static final List<String> OPTIONS = Arrays.asList(SHOW, ALT, DELETE);

    private HideMode() {}

    // Whether the feature's content should be removed on this pass.
    public static boolean hides(String mode, boolean altDown) {
        return DELETE.equals(mode) || (ALT.equals(mode) && !altDown);
    }

    // Whether a removal on this pass can be brought back by holding Alt, so the reveal prompt
    // applies. delete-mode removals are permanent and never qualify.
    public static boolean revealable(String mode, boolean altDown) {
        return ALT.equals(mode) && !altDown;
    }
}
