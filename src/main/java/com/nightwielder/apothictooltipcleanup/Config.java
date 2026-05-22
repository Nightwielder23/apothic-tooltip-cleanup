package com.nightwielder.apothictooltipcleanup;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.List;

public class Config {
    public static final ForgeConfigSpec SPEC;

    // Affix display
    public static final ForgeConfigSpec.ConfigValue<String> AFFIX_DISPLAY_MODE;
    public static final ForgeConfigSpec.IntValue AFFIX_VISIBLE_COUNT;
    public static final ForgeConfigSpec.BooleanValue SHIFT_TO_EXPAND;
    public static final ForgeConfigSpec.ConfigValue<String> AFFIX_SORT_ORDER;
    public static final ForgeConfigSpec.BooleanValue CLEAN_AFFIX_PREFIXES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HIDDEN_AFFIX_IDS;

    // Affix tooltip lines
    public static final ForgeConfigSpec.BooleanValue HIDE_SOURCE_LINE;
    public static final ForgeConfigSpec.BooleanValue DISABLE_SUMMARIZATION;
    public static final ForgeConfigSpec.BooleanValue HIDE_DURABILITY_BONUS;
    public static final ForgeConfigSpec.BooleanValue DISABLE_POTION_DESCRIPTIONS;

    // Gem display (raw gems)
    public static final ForgeConfigSpec.ConfigValue<String> GEM_TOOLTIP_MODE;
    public static final ForgeConfigSpec.BooleanValue COMPACT_GEM_DISPLAY;
    public static final ForgeConfigSpec.BooleanValue HIDE_FITS_IN;

    // Sockets
    public static final ForgeConfigSpec.BooleanValue MERGE_EMPTY_SOCKETS;
    public static final ForgeConfigSpec.BooleanValue HIDE_APOTH_MARKER;

    // Rarity colors
    public static final ForgeConfigSpec.BooleanValue RARITY_COLORS_ENABLED;
    public static final ForgeConfigSpec.ConfigValue<String> COMMON;
    public static final ForgeConfigSpec.ConfigValue<String> UNCOMMON;
    public static final ForgeConfigSpec.ConfigValue<String> RARE;
    public static final ForgeConfigSpec.ConfigValue<String> EPIC;
    public static final ForgeConfigSpec.ConfigValue<String> MYTHIC;
    public static final ForgeConfigSpec.ConfigValue<String> ANCIENT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("features");

        builder.comment(" affix display",
                " ============================================================",
                " Controls how many affixes show on a tooltip.",
                " all = show every affix",
                " top_n = show first N, rest visible with Alt held",
                " alt_only = hide every affix unless Alt is held");
        AFFIX_DISPLAY_MODE = builder.defineInList("affix_display_mode", "top_n", List.of("all", "top_n", "alt_only"));

        builder.comment(" Number of affixes shown when mode is top_n.");
        AFFIX_VISIBLE_COUNT = builder.defineInRange("affix_visible_count", 3, 0, 99);

        builder.comment(" Sort order: default, rarity, alphabetical, type.");
        AFFIX_SORT_ORDER = builder.defineInList("affix_sort_order", "default", List.of("default", "rarity", "alphabetical", "type"));

        builder.comment(" Strips prefixes from affix names.");
        CLEAN_AFFIX_PREFIXES = builder.define("clean_affix_prefixes", false);

        builder.comment(" Translation key prefixes of affixes to hide.");
        HIDDEN_AFFIX_IDS = builder.defineListAllowEmpty("hidden_affix_ids", Collections.emptyList(), o -> o instanceof String);

        builder.comment(" Deprecated, use affix_display_mode instead.");
        SHIFT_TO_EXPAND = builder.define("shift_to_expand", true);

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

        builder.comment(" gem display (raw gems)",
                " ============================================================",
                " Controls how raw gem tooltips display.",
                " full = original Apotheosis layout",
                " compact = strip headers, keep per-bullet categories and bonuses",
                " ultra = one line for categories, one line for bonuses",
                " hidden = remove all gem info");
        GEM_TOOLTIP_MODE = builder.defineInList("gem_tooltip_mode", "compact", List.of("full", "compact", "ultra", "hidden"));

        builder.comment(" Legacy, superseded by gem_tooltip_mode.");
        COMPACT_GEM_DISPLAY = builder.define("compact_gem_display", false);

        builder.comment(" Deprecated, use gem_tooltip_mode instead.");
        HIDE_FITS_IN = builder.define("hide_fits_in", false);

        builder.comment(" sockets",
                " ============================================================",
                " Merges empty sockets into one summary line.",
                " All-empty: single line replaces the empty rows.",
                " Mixed: filled gems still render, empty count appended below.");
        MERGE_EMPTY_SOCKETS = builder.define("merge_empty_sockets", true);

        builder.comment(" Hides the APOTH_REMOVE_MARKER literal text.",
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
        ANCIENT = builder.define("ancient", "0xFF5555");

        builder.pop();

        SPEC = builder.build();
    }
}
