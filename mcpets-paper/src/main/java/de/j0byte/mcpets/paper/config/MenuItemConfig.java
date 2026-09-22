package de.j0byte.mcpets.paper.config;

import de.j0byte.chameleon.api.config.ConfigSection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Ein einzelner Button in einem Menue aus der {@code menus.yml}.
 *
 * @param key       der Schluessel in der Config, nur fuer Fehlermeldungen
 * @param slot      Slot im Inventar, {@code -1} blendet den Button aus
 * @param type      wie das Item gerendert wird
 * @param action    was ein Klick ausloest
 * @param target    Ziel-Menue bei {@link MenuAction#OPEN_MENU}
 * @param item      das Item selbst
 * @param emptyItem  Ersatz-Item fuer {@link Type#ACTIVE_PET}, wenn kein Pet aktiv ist
 * @param loreBlocks Lore-Bloecke fuer die Marker im Pet-Icon, Marker-Name auf Zeilen
 */
public record MenuItemConfig(
        @NotNull String key,
        int slot,
        @NotNull Type type,
        @NotNull MenuAction action,
        @Nullable String target,
        @Nullable ItemConfig item,
        @Nullable ItemConfig emptyItem,
        @NotNull java.util.Map<String, java.util.List<String>> loreBlocks) {

    /** Slot-Wert, der einen Button ausblendet. */
    public static final int HIDDEN = -1;

    public enum Type {

        /** Immer dasselbe Item aus der Config. */
        STATIC,

        /**
         * Zeigt das gerade aktive Pet mit seinem eigenen Icon aus der {@code pets.yml} -
         * also genau so, wie es auch in der Pet-Liste aussieht. Hat der Spieler keins
         * aktiv, wird {@code empty-item} angezeigt, in der Standard-Config eine Barrier.
         */
        ACTIVE_PET;

        @NotNull
        static Type parse(@NotNull final String raw) {
            return "ACTIVE_PET".equalsIgnoreCase(raw.trim().replace('-', '_')) ? ACTIVE_PET : STATIC;
        }
    }

    @NotNull
    public static MenuItemConfig read(@NotNull final String key, @NotNull final ConfigSection section) {
        return new MenuItemConfig(
                key,
                section.getInt("slot", HIDDEN),
                Type.parse(section.getString("type", "STATIC")),
                MenuAction.parse(section.getString("action", "NONE")),
                section.getString("target"),
                ItemConfig.read(section.section("item")),
                ItemConfig.read(section.section("empty-item")),
                readLoreBlocks(section.section("lore-blocks")));
    }

    /**
     * Liest {@code lore-blocks: <marker>: [zeilen]}.
     */
    @NotNull
    private static java.util.Map<String, java.util.List<String>> readLoreBlocks(
            @Nullable final ConfigSection section) {

        if (section == null) {
            return java.util.Map.of();
        }
        final var result = new java.util.LinkedHashMap<String, java.util.List<String>>();
        for (final String marker : section.keys(false)) {
            result.put(marker, java.util.List.copyOf(section.getStringList(marker)));
        }
        return java.util.Map.copyOf(result);
    }

    public boolean visible() {
        return this.slot > HIDDEN;
    }
}
