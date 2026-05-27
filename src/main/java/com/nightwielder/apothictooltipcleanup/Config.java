package com.nightwielder.apothictooltipcleanup;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.Arrays;
import java.util.List;

public class Config {
    public static final ModConfigSpec SPEC;

    // Affix display
    public static final ModConfigSpec.ConfigValue<String> AFFIX_DISPLAY_MODE;
    public static final ModConfigSpec.IntValue AFFIX_VISIBLE_COUNT;
    public static final ModConfigSpec.ConfigValue<String> AFFIX_SORT_ORDER;
    public static final ModConfigSpec.BooleanValue CLEAN_AFFIX_PREFIXES;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> HIDDEN_AFFIX_IDS;

    // Affix tooltip lines
    public static final ModConfigSpec.BooleanValue HIDE_SOURCE_LINE;
    public static final ModConfigSpec.BooleanValue DISABLE_SUMMARIZATION;
    public static final ModConfigSpec.BooleanValue HIDE_DURABILITY_BONUS;
    public static final ModConfigSpec.BooleanValue DISABLE_POTION_DESCRIPTIONS;
    public static final ModConfigSpec.BooleanValue HIDE_AFFIX_EXTRAS;

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
        AFFIX_DISPLAY_MODE = builder.defineInList("affix_display_mode", "top_n", Arrays.asList("all", "top_n", "alt_only"));

        builder.comment(" Number of affixes shown when mode is top_n.");
        AFFIX_VISIBLE_COUNT = builder.defineInRange("affix_visible_count", 3, 0, 99);

        builder.comment(" Sort order: default, rarity, alphabetical, type.");
        AFFIX_SORT_ORDER = builder.defineInList("affix_sort_order", "default", Arrays.asList("default", "rarity", "alphabetical", "type"));

        builder.comment(" Strips prefixes from affix names.");
        CLEAN_AFFIX_PREFIXES = builder.define("clean_affix_prefixes", false);

        builder.comment(" Translation key prefixes of affixes to hide.");
        HIDDEN_AFFIX_IDS = builder.defineListAllowEmpty("hidden_affix_ids", List.of(), () -> "", o -> o instanceof String);

        builder.comment(" affix tooltip lines",
                " ============================================================",
                " Hides the affix source line.");
        HIDE_SOURCE_LINE = builder.define("hide_source_line", false);

        builder.comment(" Hides the summary block (Cold/Fire/HP%/Spell Resistance lines).");
        DISABLE_SUMMARIZATION = builder.define("disable_summarization", false);

        builder.comment(" Hides the \"ignores X% of durability damage\" line.");
        HIDE_DURABILITY_BONUS = builder.define("hide_durability_bonus", false);

        builder.comment(" Hides potion-style affix descriptions.");
        DISABLE_POTION_DESCRIPTIONS = builder.define("disable_potion_descriptions", false);

        builder.comment(" Hides bracketed markers from affix lines (cooldown and Stacking tag).",
                " Hold Alt to view the full line including markers.");
        HIDE_AFFIX_EXTRAS = builder.define("hide_affix_extras", true);

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
