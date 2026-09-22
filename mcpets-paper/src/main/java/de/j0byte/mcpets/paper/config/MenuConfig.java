package de.j0byte.mcpets.paper.config;

import de.j0byte.chameleon.api.config.ConfigSection;
import de.j0byte.shark.gui.SlotPos;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Ein komplettes Menue aus der {@code menus.yml}.
 *
 * <p>Titel, Zeilenzahl, Fueller, alle Buttons samt Slot und Aktion und der
 * paginierte Pet-Bereich kommen aus der Config. Im Code steht keine Slot-Nummer
 * und kein Anzeigetext.</p>
 *
 * @param id             Schluessel in der {@code menus.yml}
 * @param title          MiniMessage-Titel
 * @param rows           Zeilen des Inventars
 * @param filler         Fueller-Item fuer leere Slots, oder {@code null}
 * @param fillBorders    ob der Fueller auf die Randslots kommt
 * @param fillEmpty      ob der Fueller alle Slots belegt, die am Ende noch leer sind
 * @param items          alle Buttons, Schluessel ist der Config-Schluessel
 * @param petList        der paginierte Pet-Bereich, oder {@code null}
 * @param previousSlot   Slot des Zurueck-Buttons der Paginierung, oder {@code null}
 * @param nextSlot       Slot des Weiter-Buttons der Paginierung, oder {@code null}
 * @param searchSlot     Slot des Such-Buttons, oder {@code null}
 * @param sortSlot       Slot des Sortier-Buttons, oder {@code null}
 */
public record MenuConfig(
        @NotNull String id,
        @NotNull String title,
        int rows,
        @Nullable ItemConfig filler,
        boolean fillBorders,
        boolean fillEmpty,
        @NotNull Map<String, MenuItemConfig> items,
        @Nullable PetListConfig petList,
        @Nullable SlotPos previousSlot,
        @Nullable SlotPos nextSlot,
        @Nullable SlotPos searchSlot,
        @Nullable SlotPos sortSlot) {

    /** Spalten eines Chest-Inventars. Vom Vanilla-Format vorgegeben, nicht konfigurierbar. */
    public static final int COLUMNS = 9;

    private static final int DEFAULT_ROWS = 3;
    private static final int MAX_ROWS = 6;

    @NotNull
    public static MenuConfig read(@NotNull final String id, @NotNull final ConfigSection section) {
        final Map<String, MenuItemConfig> items = new LinkedHashMap<>();
        final ConfigSection itemsSection = section.section("items");
        if (itemsSection != null) {
            for (final MenuItemConfig item : readItems(itemsSection)) {
                items.put(item.key(), item);
            }
        }

        final ConfigSection controls = section.section("controls");
        return new MenuConfig(
                id,
                section.getString("title", ""),
                clampRows(section.getInt("rows", DEFAULT_ROWS)),
                ItemConfig.read(section.section("filler")),
                section.getBoolean("fill-borders", false),
                section.getBoolean("fill-empty", false),
                Map.copyOf(items),
                PetListConfig.read(section.section("pet-list")),
                slot(controls, "previous"),
                slot(controls, "next"),
                slot(controls, "search"),
                slot(controls, "sort"));
    }

    @NotNull
    private static java.util.List<MenuItemConfig> readItems(@NotNull final ConfigSection itemsSection) {
        final var result = new java.util.ArrayList<MenuItemConfig>();
        for (final String key : itemsSection.keys(false)) {
            final ConfigSection item = itemsSection.section(key);
            if (item != null) {
                result.add(MenuItemConfig.read(key, item));
            }
        }
        return result;
    }

    /**
     * Liest einen Steuer-Slot. Fehlt der Eintrag oder steht er auf einem negativen
     * Wert, wird der Button nicht gezeichnet.
     */
    @Nullable
    private static SlotPos slot(@Nullable final ConfigSection controls, @NotNull final String key) {
        if (controls == null) {
            return null;
        }
        final int raw = controls.getInt(key, MenuItemConfig.HIDDEN);
        return raw <= MenuItemConfig.HIDDEN ? null : SlotPos.of(raw / COLUMNS, raw % COLUMNS);
    }

    private static int clampRows(final int rows) {
        return Math.clamp(rows, 1, MAX_ROWS);
    }

    @NotNull
    public Optional<MenuItemConfig> item(@NotNull final String key) {
        return Optional.ofNullable(this.items.get(key));
    }

    /**
     * @return der Slot als Zeile/Spalte, passend zur Groesse dieses Menues
     */
    @NotNull
    public SlotPos position(final int slot) {
        return SlotPos.of(slot / COLUMNS, slot % COLUMNS);
    }
}
