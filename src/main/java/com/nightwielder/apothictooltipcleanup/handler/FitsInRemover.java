package com.nightwielder.apothictooltipcleanup.handler;

import com.mojang.logging.LogUtils;
import com.nightwielder.apothictooltipcleanup.Config;
import com.nightwielder.apothictooltipcleanup.util.TooltipMatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

// Reshapes the "Fits In" category list on raw gem tooltips. gem_tooltip_mode controls how aggressive
// the cleanup is (full, compact, ultra, hidden), and it also drops any "When Socketed in:" bonus
// bullet whose category is in hidden_gem_categories. The 7.x bonus block compaction is not ported
// because Apoth 6.x labels that block with item.modifiers.socket* keys.
public final class FitsInRemover {
    private static final Logger LOG = LogUtils.getLogger();
    private static boolean loggedError;

    // teal blue Apotheosis uses for the Fits In header and category bullets
    private static final int FITS_IN_COLOR = 720650;

    // A category in a Fits In bullet: its rendered name, its Apoth id from the translation key (null
    // when the key shape is unexpected), and the original component so a filtered bullet stays translatable.
    private record Category(String name, String id, Component component) {}

    // Output order for ultra mode's category line: weapons, armor, mining tools, then Other. Within a
    // group, input order is kept.
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

    // Rewrites the gem tooltip for the current gem_tooltip_mode. List surgery on an unexpected shape
    // should never blank the whole feature, so failures are caught and logged once instead of propagating.
    public static void apply(List<Component> tooltip) {
        try {
            applyInternal(tooltip);
        } catch (Throwable t) {
            if (!loggedError) {
                loggedError = true;
                LOG.warn("Failed to process a gem tooltip, leaving it unchanged.", t);
            }
        }
    }

    private static void applyInternal(List<Component> tooltip) {
        // the "When Socketed in:" bonus block is independent of gem_tooltip_mode, so filter it first.
        filterSocketInByCategory(tooltip);

        String mode = Config.GEM_TOOLTIP_MODE.get();
        if ("full".equalsIgnoreCase(mode)) {
            filterFullCategories(tooltip);
            return;
        }

        boolean hidden = "hidden".equalsIgnoreCase(mode);
        boolean ultra = "ultra".equalsIgnoreCase(mode);

        stripUnique(tooltip);

        int i = 0;
        while (i < tooltip.size()) {
            if (!TooltipMatcher.keyStartsWith(tooltip.get(i), "text.apotheosis.socketable_into")) {
                i++;
                continue;
            }

            int afterBullets = i + 1;
            while (afterBullets < tooltip.size() && TooltipMatcher.isBulletPrefix(tooltip.get(afterBullets))) {
                afterBullets++;
            }

            if (hidden) {
                removeRange(tooltip, i, afterBullets);
                while (i < tooltip.size() && isBlank(tooltip.get(i))) {
                    tooltip.remove(i);
                }
                continue;
            }

            if (ultra) {
                List<String> names = sortByGroup(namesOf(filterHidden(extractCategoriesRange(tooltip, i + 1, afterBullets))));
                removeRange(tooltip, i, afterBullets);
                if (!names.isEmpty()) {
                    tooltip.add(i, joinedCategoryBullet(String.join(", ", names)));
                    i++;
                }
                continue;
            }

            // compact: replace the header with a plain "Fits in:" line and drop hidden categories below it.
            tooltip.set(i, fitsInHeaderBullet());
            int headerIndex = i;
            i++;
            int bulletIndex = i;
            while (bulletIndex < afterBullets && TooltipMatcher.isBulletPrefix(tooltip.get(bulletIndex))) {
                List<Category> cats = extractCategories(tooltip.get(bulletIndex));
                if (cats.isEmpty()) {
                    bulletIndex++;
                    continue;
                }
                List<Category> kept = filterHidden(cats);
                if (kept.size() == cats.size()) {
                    bulletIndex++;
                } else if (kept.isEmpty()) {
                    tooltip.remove(bulletIndex);
                    afterBullets--;
                } else {
                    tooltip.set(bulletIndex, rebuildCategoryBullet(kept));
                    bulletIndex++;
                }
            }
            // header has nothing under it now, so drop it.
            if (headerIndex + 1 >= tooltip.size() || !TooltipMatcher.isBulletPrefix(tooltip.get(headerIndex + 1))) {
                tooltip.remove(headerIndex);
                i--;
            }
        }

        cleanupOrphanBlanks(tooltip);
    }

    // full mode keeps Apoth's layout, so walk each Fits In block and drop hidden categories. A bullet
    // with nothing left is removed, and so is a header with no bullets under it.
    private static void filterFullCategories(List<Component> tooltip) {
        if (hiddenSet().isEmpty()) {
            return;
        }

        int i = 0;
        while (i < tooltip.size()) {
            if (!TooltipMatcher.keyStartsWith(tooltip.get(i), "text.apotheosis.socketable_into")) {
                i++;
                continue;
            }

            int headerIndex = i;
            int bulletIndex = i + 1;
            int afterBullets = bulletIndex;
            while (afterBullets < tooltip.size() && TooltipMatcher.isBulletPrefix(tooltip.get(afterBullets))) {
                afterBullets++;
            }

            while (bulletIndex < afterBullets) {
                List<Category> cats = extractCategories(tooltip.get(bulletIndex));
                if (cats.isEmpty()) {
                    bulletIndex++;
                    continue;
                }
                List<Category> kept = filterHidden(cats);
                if (kept.size() == cats.size()) {
                    bulletIndex++;
                } else if (kept.isEmpty()) {
                    tooltip.remove(bulletIndex);
                    afterBullets--;
                } else {
                    tooltip.set(bulletIndex, rebuildCategoryBullet(kept));
                    bulletIndex++;
                }
            }

            if (headerIndex + 1 >= tooltip.size() || !TooltipMatcher.isBulletPrefix(tooltip.get(headerIndex + 1))) {
                tooltip.remove(headerIndex);
                i = headerIndex;
            } else {
                i = afterBullets;
            }
        }
    }

    // Drops "When Socketed in:" bonus bullets whose gem_class category is in hidden_gem_categories.
    // Pattern B is an item.modifiers.socket_in header followed by dot_prefix bullets, each wrapping a
    // "%s: %s" join whose first arg is a gem_class.<id> component. Runs in every mode.
    private static void filterSocketInByCategory(List<Component> tooltip) {
        Set<String> hidden = hiddenSet();
        if (hidden.isEmpty()) {
            return;
        }

        int i = 0;
        while (i < tooltip.size()) {
            if (!TooltipMatcher.keyStartsWith(tooltip.get(i), "item.modifiers.socket_in")) {
                i++;
                continue;
            }

            int headerIndex = i;
            int bulletIndex = i + 1;
            int kept = 0;
            while (bulletIndex < tooltip.size() && TooltipMatcher.isBulletPrefix(tooltip.get(bulletIndex))) {
                String id = socketBonusCategoryId(tooltip.get(bulletIndex));
                if (id != null && hidden.contains(id)) {
                    tooltip.remove(bulletIndex);
                } else {
                    kept++;
                    bulletIndex++;
                }
            }
            // header with nothing left under it would be orphaned, so drop it too.
            if (kept == 0) {
                tooltip.remove(headerIndex);
                i = headerIndex;
            } else {
                i = bulletIndex;
            }
        }
    }

    // Returns the gem_class id from a Pattern B socket bonus bullet (dot_prefix to "%s: %s" to arg[0]
    // keyed gem_class.<id>), or null if the bullet is not that shape.
    private static String socketBonusCategoryId(Component bullet) {
        TranslatableContents outer = TooltipMatcher.translatable(bullet);
        if (outer == null || outer.getArgs().length == 0) {
            return null;
        }
        if (!(outer.getArgs()[0] instanceof Component join)) {
            return null;
        }

        TranslatableContents joinTc = TooltipMatcher.translatable(join);
        if (joinTc == null || joinTc.getArgs().length == 0) {
            return null;
        }
        if (!(joinTc.getArgs()[0] instanceof Component gemClass)) {
            return null;
        }

        TranslatableContents gcTc = TooltipMatcher.translatable(gemClass);
        if (gcTc == null) {
            return null;
        }
        String key = gcTc.getKey();
        int at = key.lastIndexOf("gem_class.");
        if (at < 0) {
            return null;
        }
        return key.substring(at + "gem_class.".length());
    }

    // ultra mode, one bullet with every category and the "Fits in:" label inline.
    private static Component joinedCategoryBullet(String text) {
        return Component.translatable("text.apotheosis.dot_prefix",
                        Component.literal("Fits in: " + text))
                .withStyle(Style.EMPTY.withColor(FITS_IN_COLOR));
    }

    // compact mode's standalone "Fits in:" header, a plain literal with no bullet wrap.
    private static Component fitsInHeaderBullet() {
        return Component.literal("Fits in:").withStyle(Style.EMPTY.withColor(FITS_IN_COLOR));
    }

    private static List<String> sortByGroup(List<String> categories) {
        LinkedHashMap<String, List<String>> buckets = new LinkedHashMap<>();
        for (String groupName : CATEGORY_GROUPS.keySet()) {
            buckets.put(groupName, new ArrayList<>());
        }
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
        for (List<String> bucket : buckets.values()) {
            sorted.addAll(bucket);
        }
        return sorted;
    }

    // Pulls each category out of a Fits In dot_prefix bullet as a name and Apoth id. Apoth holds the
    // categories as the args of an inner translatable, one component per category keyed
    // text.apotheosis.category.<id>.plural, falling back to splitting the rendered text when the shape
    // is unexpected such as the "Anything" line or a plain literal.
    private static List<Category> extractCategories(Component bullet) {
        TranslatableContents outer = TooltipMatcher.translatable(bullet);
        if (outer == null || outer.getArgs().length == 0) {
            return Collections.emptyList();
        }
        if (!(outer.getArgs()[0] instanceof Component inner)) {
            return Collections.emptyList();
        }

        TranslatableContents innerTc = TooltipMatcher.translatable(inner);
        if (innerTc != null && innerTc.getArgs().length > 0) {
            List<Category> out = new ArrayList<>();
            for (Object arg : innerTc.getArgs()) {
                if (arg instanceof Component cat) {
                    out.add(new Category(cat.getString(), categoryId(cat), cat));
                } else {
                    String name = String.valueOf(arg);
                    out.add(new Category(name, null, Component.literal(name)));
                }
            }
            return out;
        }

        List<Category> out = new ArrayList<>();
        for (String name : splitCategories(inner.getString())) {
            out.add(new Category(name, null, Component.literal(name)));
        }
        return out;
    }

    private static List<Category> extractCategoriesRange(List<Component> tooltip, int from, int toExclusive) {
        List<Category> out = new ArrayList<>();
        for (int k = from; k < toExclusive; k++) {
            out.addAll(extractCategories(tooltip.get(k)));
        }
        return out;
    }

    // Returns the Apoth category id from a category name component's key (text.apotheosis.category.<id>.plural),
    // so config values like "chestplate" match regardless of the rendered "Chestplates" text or locale.
    private static String categoryId(Component cat) {
        TranslatableContents tc = TooltipMatcher.translatable(cat);
        if (tc == null) {
            return null;
        }
        String key = tc.getKey();
        int at = key.lastIndexOf("category.");
        if (at < 0) {
            return null;
        }
        String id = key.substring(at + "category.".length());
        if (id.endsWith(".plural")) {
            id = id.substring(0, id.length() - ".plural".length());
        }
        return id;
    }

    // Splits "Swords, Bows, ..." into trimmed, non empty names.
    private static List<String> splitCategories(String inner) {
        List<String> names = new ArrayList<>();
        for (String part : inner.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                names.add(trimmed);
            }
        }
        return names;
    }

    // Returns the hidden_gem_categories config as a case insensitive set, empty if unset.
    private static Set<String> hiddenSet() {
        List<? extends String> hidden = Config.HIDDEN_GEM_CATEGORIES.get();
        if (hidden == null || hidden.isEmpty()) {
            return Collections.emptySet();
        }
        Set<String> set = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        set.addAll(hidden);
        return set;
    }

    // Keeps the categories the user has not hidden, matching the Apoth id from the translation key (so
    // config can use ids like "chestplate") and the rendered name (so "Chestplates" works too).
    private static List<Category> filterHidden(List<Category> categories) {
        Set<String> hidden = hiddenSet();
        if (hidden.isEmpty()) {
            return categories;
        }
        List<Category> kept = new ArrayList<>(categories.size());
        for (Category cat : categories) {
            boolean hide = (cat.id() != null && hidden.contains(cat.id())) || hidden.contains(cat.name());
            if (!hide) {
                kept.add(cat);
            }
        }
        return kept;
    }

    private static List<String> namesOf(List<Category> categories) {
        List<String> names = new ArrayList<>(categories.size());
        for (Category cat : categories) {
            names.add(cat.name());
        }
        return names;
    }

    // Rebuilds a Fits In category bullet from the kept categories after dropping hidden ones. Apoth
    // joins the categories through a "%s, %s" translatable, so reconstruct that with one %s per kept
    // category and the original category components as args, keeping them translatable.
    private static Component rebuildCategoryBullet(List<Category> kept) {
        StringBuilder fmt = new StringBuilder();
        Object[] args = new Object[kept.size()];
        for (int i = 0; i < kept.size(); i++) {
            fmt.append("%s");
            if (i < kept.size() - 1) {
                fmt.append(", ");
            }
            args[i] = kept.get(i).component();
        }
        Component join = Component.translatable(fmt.toString(), args);
        return Component.translatable("text.apotheosis.dot_prefix", join)
                .withStyle(Style.EMPTY.withColor(FITS_IN_COLOR));
    }

    private static void stripUnique(List<Component> tooltip) {
        int j = 0;
        while (j < tooltip.size()) {
            if (TooltipMatcher.keyStartsWith(tooltip.get(j), "text.apotheosis.unique")) {
                tooltip.remove(j);
                if (j < tooltip.size() && isBlank(tooltip.get(j))) {
                    tooltip.remove(j);
                }
                continue;
            }
            j++;
        }
    }

    private static void removeRange(List<Component> tooltip, int from, int toExclusive) {
        for (int k = toExclusive - 1; k >= from; k--) {
            tooltip.remove(k);
        }
    }

    // A rewrite can leave a blank line stuck between two bullets. Drop those.
    private static void cleanupOrphanBlanks(List<Component> tooltip) {
        for (int k = tooltip.size() - 2; k >= 1; k--) {
            if (!isBlank(tooltip.get(k))) {
                continue;
            }
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
}
