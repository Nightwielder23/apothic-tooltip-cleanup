package com.nightwielder.apothictooltipcleanup;

import com.nightwielder.apothictooltipcleanup.util.HideMode;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

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

    // Rarity colors
    public static final ModConfigSpec.BooleanValue RARITY_COLORS_ENABLED;
    public static final ModConfigSpec.ConfigValue<String> COMMON;
    public static final ModConfigSpec.ConfigValue<String> UNCOMMON;
    public static final ModConfigSpec.ConfigValue<String> RARE;
    public static final ModConfigSpec.ConfigValue<String> EPIC;
    public static final ModConfigSpec.ConfigValue<String> MYTHIC;
    public static final ModConfigSpec.ConfigValue<String> ANCIENT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.push("features");

        builder.comment(" affix display",
                " ============================================================",
                " Controls how many affixes show on a tooltip.",
                " all = show every affix",
                " top_n = show first N, rest visible with Alt held",
                " alt_only = hide every affix unless Alt is held");
        AFFIX_DISPLAY_MODE = builder.defineInList("affix_display_mode", "all", Arrays.asList("all", "top_n", "alt_only"));

        builder.comment(" Number of affixes shown when mode is top_n.");
        AFFIX_VISIBLE_COUNT = builder.defineInRange("affix_visible_count", 3, 0, 99);

        builder.comment(" Sort order: default, rarity, alphabetical, type.");
        AFFIX_SORT_ORDER = builder.defineInList("affix_sort_order", "default", Arrays.asList("default", "rarity", "alphabetical", "type"));

        builder.comment(" Affix type prefixes (While held, On hit, On block, Passive).",
                " show = always visible",
                " alt = hidden unless Alt is held",
                " delete = always hidden, Alt has no effect");
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

        builder.comment(" Potion-style affix descriptions.",
                " show = always visible",
                " alt = hidden unless Alt is held",
                " delete = always hidden, Alt has no effect");
        POTION_DESCRIPTIONS_MODE = builder.defineInList("potion_descriptions_mode", HideMode.SHOW, HideMode.OPTIONS);

        builder.comment(" The [⌛ MM:SS] cooldown markers and [Stacking] tags on affix lines.",
                " The affix text itself is never touched, only the annotations.",
                " show = always visible",
                " alt = hidden unless Alt is held",
                " delete = always hidden, Alt has no effect");
        AFFIX_EXTRAS_MODE = builder.defineInList("affix_extras_mode", HideMode.ALT, HideMode.OPTIONS);

        builder.comment(" gem display (raw gems)",
                " ============================================================",
                " Controls how raw gem tooltips display.",
                " full = original Apotheosis layout",
                " compact = strip headers, keep per-bullet categories and bonuses",
                " ultra = one line for categories, one line for bonuses",
                " hidden = remove all gem info");
        GEM_TOOLTIP_MODE = builder.defineInList("gem_tooltip_mode", "compact", Arrays.asList("full", "compact", "ultra", "hidden"));

        builder.comment(" Case-insensitive list of gem categories to hide from Fits In tooltips.",
                " Works in all gem_tooltip_mode values including full.");
        HIDDEN_GEM_CATEGORIES = builder.defineListAllowEmpty("hidden_gem_categories", List.of(), () -> "", o -> o instanceof String);

        builder.comment(" sockets",
                " ============================================================",
                " Merges empty sockets into one summary line.",
                " All-empty: single line replaces the empty rows.",
                " Mixed: filled gems still render, empty count appended below.");
        MERGE_EMPTY_SOCKETS = builder.define("merge_empty_sockets", true);

        builder.comment(" Hides the APOTH_SOCKET_MARKER literal text.",
                " Only enable if the marker is leaking through visibly.",
                " Can hide the socket UI on socketed items if enabled.");
        HIDE_APOTH_MARKER = builder.define("hide_apoth_marker", false);

        builder.pop();

        builder.push("rarity_colors");

        builder.comment(" Enables custom rarity color overrides.",
                " When false, vanilla Apotheosis colors are used.");
        RARITY_COLORS_ENABLED = builder.define("rarity_colors_enabled", false);

        builder.comment(" Hex format: 0xRRGGBB");
        COMMON = builder.define("common", "0xAAAAAA");

        UNCOMMON = builder.define("uncommon", "0x55FF55");
        RARE = builder.define("rare", "0x55FFFF");
        EPIC = builder.define("epic", "0xFF55FF");
        MYTHIC = builder.define("mythic", "0xFFAA00");
        builder.comment(" Color used for unknown rarities. In Apotheosis 8.x ancient is no longer a built-in rarity, but this color still applies to Apotheotic Additions esoteric and any other unrecognized rarity.");
        ANCIENT = builder.define("ancient", "0xFF5555");

        builder.pop();

        SPEC = builder.build();
    }
}
