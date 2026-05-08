package com.nightwielder.apothictooltipcleanup;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.List;

public class Config {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue HIDE_FITS_IN;
    public static final ForgeConfigSpec.ConfigValue<String> GEM_TOOLTIP_MODE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HIDDEN_AFFIX_IDS;
    public static final ForgeConfigSpec.BooleanValue HIDE_SOURCE_LINE;
    public static final ForgeConfigSpec.BooleanValue DISABLE_SUMMARIZATION;
    public static final ForgeConfigSpec.BooleanValue COMPACT_GEM_DISPLAY;
    public static final ForgeConfigSpec.BooleanValue CLEAN_AFFIX_PREFIXES;
    public static final ForgeConfigSpec.ConfigValue<String> AFFIX_SORT_ORDER;
    public static final ForgeConfigSpec.BooleanValue MERGE_EMPTY_SOCKETS;
    public static final ForgeConfigSpec.BooleanValue DISABLE_POTION_DESCRIPTIONS;
    public static final ForgeConfigSpec.BooleanValue SHIFT_TO_EXPAND;
    public static final ForgeConfigSpec.ConfigValue<String> AFFIX_DISPLAY_MODE;
    public static final ForgeConfigSpec.IntValue AFFIX_VISIBLE_COUNT;
    public static final ForgeConfigSpec.BooleanValue HIDE_DURABILITY_BONUS;
    public static final ForgeConfigSpec.BooleanValue HIDE_APOTH_MARKER;

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

        builder.comment("Deprecated. Use gem_tooltip_mode instead. When true and gem_tooltip_mode is unset, equivalent to gem_tooltip_mode = hidden.");
        HIDE_FITS_IN = builder.define("hide_fits_in", false);

        builder.comment("Controls how raw gem tooltips display.",
                "full = original Apotheosis layout with Unique tag, headers, and per-bullet category and bonus lines.",
                "compact = strip headers, blank lines, and the Unique tag; keep Apotheosis's natural per-line category bullets and bonus bullets in 'Category: +1 level to X' format. Strips 'level to existing' wording from bonuses.",
                "ultra = one line listing all categories joined ('Applicable on: X, Y, Z'), one line listing all bonuses joined as '+1 level to Sharpness (Melee Weapons), +1 level to Protection (Core Armor)' style.",
                "hidden = remove all gem info from the tooltip.");
        GEM_TOOLTIP_MODE = builder.defineInList("gem_tooltip_mode", "compact", List.of("full", "compact", "ultra", "hidden"));

        builder.comment("Translation key prefixes of affixes to hide.");
        HIDDEN_AFFIX_IDS = builder.defineListAllowEmpty("hidden_affix_ids", Collections.emptyList(), o -> o instanceof String);

        builder.comment("Hides the affix source line.");
        HIDE_SOURCE_LINE = builder.define("hide_source_line", false);

        builder.comment("Hides the Apotheosis summarized affix line.");
        DISABLE_SUMMARIZATION = builder.define("disable_summarization", false);

        builder.comment("Renders gems on a single line. Currently only affects raw gem item hovers. Compact display on socketed items requires graphical-component hooking and is planned for a future version.");
        COMPACT_GEM_DISPLAY = builder.define("compact_gem_display", false);

        builder.comment("Strips prefixes from affix names.");
        CLEAN_AFFIX_PREFIXES = builder.define("clean_affix_prefixes", false);

        builder.comment("Sort order for affix lines: default, rarity, alphabetical, type.");
        AFFIX_SORT_ORDER = builder.defineInList("affix_sort_order", "default", List.of("default", "rarity", "alphabetical", "type"));

        builder.comment("Collapses empty sockets on socketed items. All-empty becomes one summary line; mixed keeps filled gem icons with the empty count appended below.");
        MERGE_EMPTY_SOCKETS = builder.define("merge_empty_sockets", true);

        builder.comment("Hides potion-style affix descriptions.");
        DISABLE_POTION_DESCRIPTIONS = builder.define("disable_potion_descriptions", false);

        builder.comment("Deprecated. Use affix_display_mode instead. When false, disables affix display modification entirely.");
        SHIFT_TO_EXPAND = builder.define("shift_to_expand", true);

        builder.comment("Controls how many affixes show on a tooltip.",
                "all = show every affix.",
                "top_n = show only the first N affixes; rest visible with Alt held.",
                "alt_only = hide every affix unless Alt is held.");
        AFFIX_DISPLAY_MODE = builder.defineInList("affix_display_mode", "top_n", List.of("all", "top_n", "alt_only"));

        builder.comment("Number of affixes shown when affix_display_mode is top_n. Range 0 to 99. Default 3.");
        AFFIX_VISIBLE_COUNT = builder.defineInRange("affix_visible_count", 3, 0, 99);

        builder.comment("Hides the bonus durability line.");
        HIDE_DURABILITY_BONUS = builder.define("hide_durability_bonus", false);

        builder.comment("Hides the APOTH_REMOVE_MARKER literal text. Only enable if the marker is leaking through visibly. Note: Apotheosis uses this marker as a placeholder for its graphical socket display, so enabling this can hide the socket UI on socketed items.");
        HIDE_APOTH_MARKER = builder.define("hide_apoth_marker", false);

        builder.pop();

        builder.push("rarity_colors");

        builder.comment("Enables custom rarity color overrides.");
        RARITY_COLORS_ENABLED = builder.define("rarity_colors_enabled", false);

        builder.comment("Common rarity color (0xRRGGBB).");
        COMMON = builder.define("common", "0xAAAAAA");

        builder.comment("Uncommon rarity color (0xRRGGBB).");
        UNCOMMON = builder.define("uncommon", "0x55FF55");

        builder.comment("Rare rarity color (0xRRGGBB).");
        RARE = builder.define("rare", "0x55FFFF");

        builder.comment("Epic rarity color (0xRRGGBB).");
        EPIC = builder.define("epic", "0xFF55FF");

        builder.comment("Mythic rarity color (0xRRGGBB).");
        MYTHIC = builder.define("mythic", "0xFFAA00");

        builder.comment("Ancient rarity color (0xRRGGBB).");
        ANCIENT = builder.define("ancient", "0xFF5555");

        builder.pop();

        SPEC = builder.build();
    }
}
