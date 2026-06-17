package com.nightwielder.apothictooltipcleanup;

import com.nightwielder.apothictooltipcleanup.util.HideMode;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

// All the client config, built once into SPEC. Handlers read these values fresh each tooltip pass.
public class Config {
    public static final ModConfigSpec SPEC;

    // Affix display
    public static final ModConfigSpec.ConfigValue<String> AFFIX_DISPLAY_MODE;
    public static final ModConfigSpec.IntValue AFFIX_VISIBLE_COUNT;
    public static final ModConfigSpec.ConfigValue<String> AFFIX_SORT_ORDER;
    public static final ModConfigSpec.ConfigValue<String> AFFIX_PREFIXES_MODE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> HIDDEN_AFFIX_IDS;

    // Affix tooltip lines
    public static final ModConfigSpec.ConfigValue<String> SUMMARIZATION_MODE;
    public static final ModConfigSpec.ConfigValue<String> DURABILITY_BONUS_MODE;
    public static final ModConfigSpec.ConfigValue<String> POTION_DESCRIPTIONS_MODE;
    public static final ModConfigSpec.ConfigValue<String> AFFIX_EXTRAS_MODE;

    // Gem display (raw gems)
    public static final ModConfigSpec.ConfigValue<String> GEM_TOOLTIP_MODE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> HIDDEN_GEM_CATEGORIES;

    // Sockets
    public static final ModConfigSpec.BooleanValue MERGE_EMPTY_SOCKETS;
    public static final ModConfigSpec.BooleanValue HIDE_APOTH_MARKER;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("features");

        builder.comment(" affix display",
                " ============================================================",
                " How many affixes show on a tooltip.",
                " all = show every affix",
                " top_n = show first N, rest under Alt",
                " alt_only = hide every affix unless Alt is held");
        AFFIX_DISPLAY_MODE = builder.defineInList("affix_display_mode", "all", Arrays.asList("all", "top_n", "alt_only"));

        builder.comment(" Affixes shown when mode is top_n.");
        AFFIX_VISIBLE_COUNT = builder.defineInRange("affix_visible_count", 3, 0, 99);

        builder.comment(" Sort order: default or alphabetical.");
        AFFIX_SORT_ORDER = builder.defineInList("affix_sort_order", "default", Arrays.asList("default", "alphabetical"));

        builder.comment(" The affix prefix and suffix Apotheosis adds to the item name (Strengthened, of the Inferno).",
                " show = full affixed name",
                " alt = base item name unless Alt is held",
                " delete = always the base item name");
        AFFIX_PREFIXES_MODE = builder.defineInList("affix_prefixes_mode", HideMode.SHOW, HideMode.OPTIONS);

        builder.comment(" Translation key prefixes of affixes to hide.");
        HIDDEN_AFFIX_IDS = builder.defineListAllowEmpty("hidden_affix_ids", List.of(), () -> "", o -> o instanceof String);

        builder.comment(" affix tooltip lines",
                " ============================================================",
                " The summary block (Cold/Fire/HP%/Spell Resistance lines).",
                " show = always visible",
                " alt = hidden unless Alt is held",
                " delete = always hidden, Alt has no effect");
        SUMMARIZATION_MODE = builder.defineInList("summarization_mode", HideMode.ALT, HideMode.OPTIONS);

        builder.comment(" The \"ignores X% of durability damage\" line.",
                " show = always visible",
                " alt = hidden unless Alt is held",
                " delete = always hidden, Alt has no effect");
        DURABILITY_BONUS_MODE = builder.defineInList("durability_bonus_mode", HideMode.ALT, HideMode.OPTIONS);

        builder.comment(" Potion style affix descriptions.",
                " show = always visible",
                " alt = hidden unless Alt is held",
                " delete = always hidden, Alt has no effect");
        POTION_DESCRIPTIONS_MODE = builder.defineInList("potion_descriptions_mode", HideMode.SHOW, HideMode.OPTIONS);

        builder.comment(" The [MM:SS] cooldown markers and [Stacking] tags on affix lines.",
                " Only the annotations are touched, never the affix text.",
                " show = always visible",
                " alt = hidden unless Alt is held",
                " delete = always hidden, Alt has no effect");
        AFFIX_EXTRAS_MODE = builder.defineInList("affix_extras_mode", HideMode.ALT, HideMode.OPTIONS);

        builder.comment(" gem display (raw gems)",
                " ============================================================",
                " How raw gem tooltips display.",
                " full = original Apotheosis layout",
                " compact = strip headers, keep per bullet categories and bonuses",
                " ultra = one line for categories, one line for bonuses",
                " hidden = remove all gem info");
        GEM_TOOLTIP_MODE = builder.defineInList("gem_tooltip_mode", "compact", Arrays.asList("full", "compact", "ultra", "hidden"));

        builder.comment(" Gem categories to hide from 'Fits In' lists. Case insensitive.",
                " Example: [\"Bows\", \"Crossbows\"] to hide ranged weapons.",
                " No effect when gem_tooltip_mode is hidden, since that drops all gem info anyway.");
        HIDDEN_GEM_CATEGORIES = builder.defineListAllowEmpty("hidden_gem_categories", List.of(), () -> "", o -> o instanceof String);

        builder.comment(" sockets",
                " ============================================================",
                " Merges empty sockets into one summary line.",
                " All empty: one line replaces the empty rows.",
                " Mixed: filled gems still render, empty count added below.");
        MERGE_EMPTY_SOCKETS = builder.define("merge_empty_sockets", true);

        builder.comment(" Hides the APOTH_SOCKET_MARKER literal text.",
                " Only turn on if the marker is leaking through visibly.",
                " Can hide the socket UI on socketed items.");
        HIDE_APOTH_MARKER = builder.define("hide_apoth_marker", false);

        builder.pop();

        SPEC = builder.build();
    }
}
