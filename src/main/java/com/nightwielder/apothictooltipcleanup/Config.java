package com.nightwielder.apothictooltipcleanup;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Collections;
import java.util.List;

public class Config {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue HIDE_FITS_IN;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HIDDEN_AFFIX_IDS;
    public static final ForgeConfigSpec.BooleanValue HIDE_SOURCE_LINE;
    public static final ForgeConfigSpec.BooleanValue DISABLE_SUMMARIZATION;
    public static final ForgeConfigSpec.BooleanValue COMPACT_GEM_DISPLAY;
    public static final ForgeConfigSpec.BooleanValue CLEAN_AFFIX_PREFIXES;
    public static final ForgeConfigSpec.ConfigValue<String> AFFIX_SORT_ORDER;
    public static final ForgeConfigSpec.BooleanValue MERGE_EMPTY_SOCKETS;
    public static final ForgeConfigSpec.BooleanValue DISABLE_POTION_DESCRIPTIONS;
    public static final ForgeConfigSpec.BooleanValue SHIFT_TO_EXPAND;
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

        builder.comment("Removes the Fits In section from gem tooltips.");
        HIDE_FITS_IN = builder.define("hide_fits_in", false);

        builder.comment("Translation key prefixes of affixes to hide.");
        HIDDEN_AFFIX_IDS = builder.defineListAllowEmpty("hidden_affix_ids", Collections.emptyList(), o -> o instanceof String);

        builder.comment("Hides the affix source line.");
        HIDE_SOURCE_LINE = builder.define("hide_source_line", false);

        builder.comment("Hides the Apotheosis summarized affix line.");
        DISABLE_SUMMARIZATION = builder.define("disable_summarization", false);

        builder.comment("Renders gems on a single line.");
        COMPACT_GEM_DISPLAY = builder.define("compact_gem_display", false);

        builder.comment("Strips prefixes from affix names.");
        CLEAN_AFFIX_PREFIXES = builder.define("clean_affix_prefixes", false);

        builder.comment("Sort order for affix lines: default, rarity, alphabetical, type.");
        AFFIX_SORT_ORDER = builder.defineInList("affix_sort_order", "default", List.of("default", "rarity", "alphabetical", "type"));

        builder.comment("Collapses empty sockets into one counted line.");
        MERGE_EMPTY_SOCKETS = builder.define("merge_empty_sockets", false);

        builder.comment("Hides potion-style affix descriptions.");
        DISABLE_POTION_DESCRIPTIONS = builder.define("disable_potion_descriptions", false);

        builder.comment("Hides affix detail unless Alt is held. Config key kept as shift_to_expand for backwards compatibility.");
        SHIFT_TO_EXPAND = builder.define("shift_to_expand", false);

        builder.comment("Hides the bonus durability line.");
        HIDE_DURABILITY_BONUS = builder.define("hide_durability_bonus", false);

        builder.comment("Hides the APOTH_REMOVE_MARKER literal text that leaks through from Apotheosis.");
        HIDE_APOTH_MARKER = builder.define("hide_apoth_marker", true);

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
