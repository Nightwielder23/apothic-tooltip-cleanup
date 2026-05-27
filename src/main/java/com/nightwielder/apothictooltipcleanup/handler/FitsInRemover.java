package com.nightwielder.apothictooltipcleanup.handler;

import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public final class FitsInRemover {
    // Apotheosis renders the "Fits In:" header and category bullets in this teal-blue color.
    private static final int FITS_IN_COLOR = 720650;

    // Group buckets used by sortByGroup. Iteration order is the output order; categories within
    // each group keep their input order. Anything unmatched falls into Other.
    private static final LinkedHashMap<String, Set<String>> CATEGORY_GROUPS = new LinkedHashMap<>();
    static {
        CATEGORY_GROUPS.put("Weapons", caseInsensitive(
                "Swords", "Heavy Weapons", "Tridents", "Bows", "Crossbows",
                "Wands", "Staves", "Staffs", "Spell-Casters", "Daggers",
                "Melee Weapons", "Ranged Weapons"));
        CATEGORY_GROUPS.put("Armor", caseInsensitive(
                "Helmets", "Chestplates", "Leggings", "Boots", "Shields",
                "Heavy Armor", "Light Armor", "Core Armor"));
        CATEGORY_GROUPS.put("Mining Tools", caseInsensitive(
                "Pickaxes", "Shovels", "Axes", "Hoes", "Mining Tools"));
    }

    private FitsInRemover() {}

    public static void apply(List<Component> tooltip) {
        String mode = Config.GEM_TOOLTIP_MODE.get();
        if ("full".equalsIgnoreCase(mode)) {
            // Apotheosis renders the Fits-In section normally; we only prune category bullets
            // whose names the user listed in hidden_gem_categories. No structural rewrite.
            filterFullModeCategories(tooltip);
            return;
        }

        boolean hidden = "hidden".equalsIgnoreCase(mode);
        boolean compact = "compact".equalsIgnoreCase(mode);
        boolean ultra = "ultra".equalsIgnoreCase(mode);

        stripUnique(tooltip);

        int i = 0;
        while (i < tooltip.size()) {
            String key = TooltipMatcher.getKey(tooltip.get(i));
            boolean isFits = key != null
                    && (key.startsWith("text.apotheosis.socketable_into") || key.startsWith("text.apotheosis.fits_in"));
            boolean isBonusHeader = key != null && key.startsWith("text.apotheosis.when_socketed_in");
            if (!isFits && !isBonusHeader) {
                i++;
                continue;
            }

            int afterBullets = i + 1;
            while (afterBullets < tooltip.size()
                    && TooltipMatcher.isBulletPrefix(tooltip.get(afterBullets))) {
                afterBullets++;
            }

            if (hidden) {
                removeRange(tooltip, i, afterBullets);
                while (i < tooltip.size() && isBlank(tooltip.get(i))) tooltip.remove(i);
                continue;
            }

            if (isFits) {
                if (ultra) {
                    List<String> names = sortByGroup(filterHidden(extractCategoryNames(tooltip, i + 1, afterBullets)));
                    removeRange(tooltip, i, afterBullets);
                    if (!names.isEmpty()) {
                        tooltip.add(i, joinedCategoryBullet(String.join(", ", names)));
                        i++;
                    }
                    continue;
                }
                // compact: replace the header with a plain teal "Fits in:" line, then drop any
                // hidden categories from the bullets below it.
                tooltip.set(i, fitsInHeaderBullet());
                int headerIndex = i;
                i++;
                int bulletIndex = i;
                while (bulletIndex < afterBullets && TooltipMatcher.isBulletPrefix(tooltip.get(bulletIndex))) {
                    String inner = extractBulletText(tooltip.get(bulletIndex));
                    if (inner == null || inner.isEmpty()) {
                        bulletIndex++;
                        continue;
                    }
                    List<String> names = splitCategories(inner);
                    List<String> kept = filterHidden(names);
                    if (kept.size() == names.size()) {
                        bulletIndex++;
                    } else if (kept.isEmpty()) {
                        tooltip.remove(bulletIndex);
                        afterBullets--;
                    } else {
                        tooltip.set(bulletIndex, compactCategoryBullet(String.join(", ", kept)));
                        bulletIndex++;
                    }
                }
                // Every category bullet filtered out leaves the header with nothing below it; drop it.
                if (headerIndex + 1 >= tooltip.size()
                        || !TooltipMatcher.isBulletPrefix(tooltip.get(headerIndex + 1))) {
                    tooltip.remove(headerIndex);
                    i--;
                }
                continue;
            }

            // isBonusHeader from here on.
            if (compact) {
                for (int k = i + 1; k < afterBullets; k++) {
                    String inner = extractBulletText(tooltip.get(k));
                    if (inner == null) continue;
                    tooltip.set(k, goldBullet(stripExisting(inner)));
                }
                tooltip.remove(i);
                continue;
            }
            if (ultra) {
                StringBuilder sb = new StringBuilder();
                for (int k = i + 1; k < afterBullets; k++) {
                    String inner = extractBulletText(tooltip.get(k));
                    if (inner == null || inner.isEmpty()) continue;
                    String formatted = parenthesize(stripExisting(inner));
                    if (sb.length() > 0) sb.append(", ");
                    sb.append(formatted);
                }
                removeRange(tooltip, i, afterBullets);
                if (sb.length() > 0) {
                    tooltip.add(i, goldBullet(sb.toString()));
                    i++;
                }
                continue;
            }
            // Unknown mode: defensive drop of header.
            tooltip.remove(i);
        }

        cleanupOrphanBlanks(tooltip);
    }

    private static Component joinedCategoryBullet(String text) {
        return Component.translatable("text.apotheosis.dot_prefix",
                        Component.literal("Fits in: " + text))
                .withStyle(Style.EMPTY.withColor(FITS_IN_COLOR));
    }

    // Compact-mode category bullet rebuilt after hidden categories are dropped. No "Fits in:"
    // prefix here; compact mode carries that on its own header line.
    private static Component compactCategoryBullet(String text) {
        return Component.translatable("text.apotheosis.dot_prefix", Component.literal(text))
                .withStyle(Style.EMPTY.withColor(FITS_IN_COLOR));
    }

    // Compact mode renders this as a standalone header line above the existing category bullets,
    // so it is intentionally a plain literal without the dot_prefix bullet wrap.
    private static Component fitsInHeaderBullet() {
        return Component.literal("Fits in:").withStyle(Style.EMPTY.withColor(FITS_IN_COLOR));
    }

    private static Component goldBullet(String text) {
        return Component.translatable("text.apotheosis.dot_prefix", Component.literal(text))
                .withStyle(ChatFormatting.GOLD);
    }

    // "+1 level to existing Sharpness" -> "+1 level to Sharpness".
    private static String stripExisting(String text) {
        return text.replace(" levels to existing ", " levels to ")
                .replace(" level to existing ", " level to ");
    }

    // "Melee Weapons: +1 level to Sharpness" -> "+1 level to Sharpness (Melee Weapons)".
    private static String parenthesize(String text) {
        int sep = text.indexOf(": ");
        if (sep < 0) return text;
        String category = text.substring(0, sep);
        String bonus = text.substring(sep + 2);
        return bonus + " (" + category + ")";
    }

    // Reorders a category list so weapons come first, then armor, then mining tools, then anything
    // that did not match a defined group. Within each group the input order is preserved.
    private static List<String> sortByGroup(List<String> categories) {
        LinkedHashMap<String, List<String>> buckets = new LinkedHashMap<>();
        for (String groupName : CATEGORY_GROUPS.keySet()) buckets.put(groupName, new ArrayList<>());
        buckets.put("Other", new ArrayList<>());
        for (String name : categories) {
            String key = "Other";
            for (Map.Entry<String, Set<String>> e : CATEGORY_GROUPS.entrySet()) {
                if (e.getValue().contains(name)) {
                    key = e.getKey();
                    break;
                }
            }
            buckets.get(key).add(name);
        }
        List<String> sorted = new ArrayList<>(categories.size());
        for (List<String> bucket : buckets.values()) sorted.addAll(bucket);
        return sorted;
    }

    private static List<String> extractCategoryNames(List<Component> tooltip, int from, int toExclusive) {
        List<String> names = new ArrayList<>();
        for (int k = from; k < toExclusive; k++) {
            String inner = extractBulletText(tooltip.get(k));
            if (inner == null || inner.isEmpty()) continue;
            names.addAll(splitCategories(inner));
        }
        return names;
    }

    // Splits a category bullet's inner text ("Swords, Bows, ...") into trimmed, non-empty names.
    private static List<String> splitCategories(String inner) {
        List<String> names = new ArrayList<>();
        for (String part : inner.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) names.add(trimmed);
        }
        return names;
    }

    // Full-mode pass: Apotheosis renders the Fits-In header followed by one category per bullet.
    // We walk each Fits-In block and drop any bullet whose categories all appear in
    // hidden_gem_categories. When every bullet under a header is dropped, the header goes too so
    // we don't leave an orphan label. Bullet styling is left untouched.
    private static void filterFullModeCategories(List<Component> tooltip) {
        List<? extends String> hidden = Config.HIDDEN_GEM_CATEGORIES.get();
        if (hidden == null || hidden.isEmpty()) return;

        int i = 0;
        while (i < tooltip.size()) {
            String key = TooltipMatcher.getKey(tooltip.get(i));
            boolean isFits = key != null
                    && (key.startsWith("text.apotheosis.socketable_into") || key.startsWith("text.apotheosis.fits_in"));
            if (!isFits) {
                i++;
                continue;
            }

            int bulletStart = i + 1;
            int bulletIndex = bulletStart;
            while (bulletIndex < tooltip.size() && TooltipMatcher.isBulletPrefix(tooltip.get(bulletIndex))) {
                String inner = extractBulletText(tooltip.get(bulletIndex));
                if (inner == null || inner.isEmpty()) {
                    bulletIndex++;
                    continue;
                }
                List<String> names = splitCategories(inner);
                List<String> kept = filterHidden(names);
                if (kept.isEmpty()) {
                    tooltip.remove(bulletIndex);
                } else {
                    bulletIndex++;
                }
            }

            if (bulletStart >= tooltip.size() || !TooltipMatcher.isBulletPrefix(tooltip.get(bulletStart))) {
                tooltip.remove(i);
                continue;
            }
            i = bulletIndex;
        }
    }

    private static void stripUnique(List<Component> tooltip) {
        int j = 0;
        while (j < tooltip.size()) {
            if (TooltipMatcher.keyStartsWith(tooltip.get(j), "text.apotheosis.unique")) {
                tooltip.remove(j);
                if (j < tooltip.size() && isBlank(tooltip.get(j))) tooltip.remove(j);
                continue;
            }
            j++;
        }
    }

    // Each bullet (dot_prefix or star_prefix) is a TranslatableContents whose arg[0] is the inner
    // component. The visible payload (categories or bonus text) is the rendered string of arg[0].
    private static String extractBulletText(Component bullet) {
        if (!(bullet.getContents() instanceof TranslatableContents tc)) return null;
        Object[] args = tc.getArgs();
        if (args.length == 0) return null;
        Object arg = args[0];
        if (arg instanceof Component c) return c.getString();
        return String.valueOf(arg);
    }

    private static void removeRange(List<Component> tooltip, int from, int toExclusive) {
        for (int k = toExclusive - 1; k >= from; k--) tooltip.remove(k);
    }

    // After mode-specific transformation, a blank line that used to separate the Fits-In and
    // When-Socketed-In sections may now sit between two dot_prefix bullets. Drop those.
    private static void cleanupOrphanBlanks(List<Component> tooltip) {
        for (int k = tooltip.size() - 2; k >= 1; k--) {
            if (!isBlank(tooltip.get(k))) continue;
            if (TooltipMatcher.isBulletPrefix(tooltip.get(k - 1))
                    && TooltipMatcher.isBulletPrefix(tooltip.get(k + 1))) {
                tooltip.remove(k);
            }
        }
    }

    private static boolean isBlank(Component c) {
        String s = c.getString();
        return s == null || s.trim().isEmpty();
    }

    private static Set<String> caseInsensitive(String... values) {
        Set<String> s = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        Collections.addAll(s, values);
        return s;
    }

    // Drops any category names listed in the hidden_gem_categories config, case-insensitively.
    private static List<String> filterHidden(List<String> categories) {
        List<? extends String> hidden = Config.HIDDEN_GEM_CATEGORIES.get();
        if (hidden == null || hidden.isEmpty()) return categories;
        Set<String> hiddenSet = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        hiddenSet.addAll(hidden);
        List<String> filtered = new ArrayList<>(categories.size());
        for (String name : categories) {
            if (!hiddenSet.contains(name)) filtered.add(name);
        }
        return filtered;
    }
}
