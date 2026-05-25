package com.nightwielder.apothictooltipcleanup;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Config {
    // Client spec covers everything the tooltip renderer cares about. Common spec carries the
    // anvil rename override and any future server-authoritative toggles, so a dedicated server
    // operator can flip them on without touching the client-side TOML.
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec COMMON_SPEC;

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
    public static final ForgeConfigSpec.BooleanValue HIDE_AFFIX_COOLDOWNS;

    // Gem display (raw gems)
    public static final ForgeConfigSpec.ConfigValue<String> GEM_TOOLTIP_MODE;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> HIDDEN_GEM_CATEGORIES;

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

    // Server-side (COMMON spec). Lives in apothic_tooltip_cleanup-common.toml so dedicated server
    // operators can control it without a client install.
    public static final ForgeConfigSpec.BooleanValue OVERRIDE_AFFIX_NAME_ON_RENAME;

    static {
        ForgeConfigSpec.Builder clientBuilder = new ForgeConfigSpec.Builder();

        clientBuilder.push("features");

        clientBuilder.comment(" affix display",
                " ============================================================",
                " Controls how many affixes show on a tooltip.",
                " all = show every affix",
                " top_n = show first N, rest visible with Alt held",
                " alt_only = hide every affix unless Alt is held");
        AFFIX_DISPLAY_MODE = clientBuilder.defineInList("affix_display_mode", "top_n", Arrays.asList("all", "top_n", "alt_only"));

        clientBuilder.comment(" Number of affixes shown when mode is top_n.");
        AFFIX_VISIBLE_COUNT = clientBuilder.defineInRange("affix_visible_count", 3, 0, 99);

        clientBuilder.comment(" Sort order: default, rarity, alphabetical, type.");
        AFFIX_SORT_ORDER = clientBuilder.defineInList("affix_sort_order", "default", Arrays.asList("default", "rarity", "alphabetical", "type"));

        clientBuilder.comment(" Strips prefixes from affix names.");
        CLEAN_AFFIX_PREFIXES = clientBuilder.define("clean_affix_prefixes", false);

        clientBuilder.comment(" Translation key prefixes of affixes to hide.");
        HIDDEN_AFFIX_IDS = clientBuilder.defineListAllowEmpty("hidden_affix_ids", Collections.emptyList(), o -> o instanceof String);

        clientBuilder.comment(" Deprecated, no longer has any effect. Use affix_display_mode instead.");
        SHIFT_TO_EXPAND = clientBuilder.define("shift_to_expand", true);

        clientBuilder.comment(" affix tooltip lines",
                " ============================================================",
                " Hides the affix source line.");
        HIDE_SOURCE_LINE = clientBuilder.define("hide_source_line", false);

        clientBuilder.comment(" Hides the summary block (Cold/Fire/HP%/Spell Resistance lines).");
        DISABLE_SUMMARIZATION = clientBuilder.define("disable_summarization", false);

        clientBuilder.comment(" Hides the \"ignores X% of durability damage\" line.");
        HIDE_DURABILITY_BONUS = clientBuilder.define("hide_durability_bonus", false);

        clientBuilder.comment(" Hides potion-style affix descriptions.");
        DISABLE_POTION_DESCRIPTIONS = clientBuilder.define("disable_potion_descriptions", false);

        clientBuilder.comment(" Hides the [⌛ MM:SS] cooldown markers on affix lines.",
                " Strips just the cooldown annotation, the affix text remains unchanged.");
        HIDE_AFFIX_COOLDOWNS = clientBuilder.define("hide_affix_cooldowns", false);

        clientBuilder.comment(" gem display (raw gems)",
                " ============================================================",
                " Controls how raw gem tooltips display.",
                " full = original Apotheosis layout",
                " compact = strip headers, keep per-bullet categories and bonuses",
                " ultra = one line for categories, one line for bonuses",
                " hidden = remove all gem info");
        GEM_TOOLTIP_MODE = clientBuilder.defineInList("gem_tooltip_mode", "compact", Arrays.asList("full", "compact", "ultra", "hidden"));

        clientBuilder.comment(" Category names to hide from gem 'Fits In' lists.",
                " Example: [\"Bows\", \"Crossbows\"] to hide ranged weapons.",
                " Case-insensitive. Has no effect when gem_tooltip_mode is full or hidden.");
        HIDDEN_GEM_CATEGORIES = clientBuilder.defineListAllowEmpty("hidden_gem_categories", Collections.emptyList(), o -> o instanceof String);

        clientBuilder.comment(" sockets",
                " ============================================================",
                " Merges empty sockets into one summary line.",
                " All-empty: single line replaces the empty rows.",
                " Mixed: filled gems still render, empty count appended below.");
        MERGE_EMPTY_SOCKETS = clientBuilder.define("merge_empty_sockets", true);

        clientBuilder.comment(" Hides the APOTH_REMOVE_MARKER literal text.",
                " Only enable if the marker is leaking through visibly.",
                " Can hide the socket UI on socketed items if enabled.");
        HIDE_APOTH_MARKER = clientBuilder.define("hide_apoth_marker", false);

        clientBuilder.pop();

        clientBuilder.push("rarity_colors");

        clientBuilder.comment(" Enables custom rarity color overrides.",
                " When false, vanilla Apotheosis colors are used.");
        RARITY_COLORS_ENABLED = clientBuilder.define("rarity_colors_enabled", false);

        clientBuilder.comment(" Hex format: 0xRRGGBB");
        COMMON = clientBuilder.define("common", "0xAAAAAA");

        UNCOMMON = clientBuilder.define("uncommon", "0x55FF55");
        RARE = clientBuilder.define("rare", "0x55FFFF");
        EPIC = clientBuilder.define("epic", "0xFF55FF");
        MYTHIC = clientBuilder.define("mythic", "0xFFAA00");
        ANCIENT = clientBuilder.define("ancient", "0xFF5555");

        clientBuilder.pop();

        CLIENT_SPEC = clientBuilder.build();

        ForgeConfigSpec.Builder commonBuilder = new ForgeConfigSpec.Builder();

        commonBuilder.push("rename_override");
        commonBuilder.comment(" Replaces the affix-decorated item name with the user's custom name",
                " when renaming an affixed item in an anvil. Requires the mod to be installed",
                " on the server. Default off for backward compatibility with client-only installs.");
        OVERRIDE_AFFIX_NAME_ON_RENAME = commonBuilder.define("override_affix_name_on_rename", false);
        commonBuilder.pop();

        COMMON_SPEC = commonBuilder.build();
    }
}
