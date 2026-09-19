package de.j0byte.mcpets.paper.config;

import de.j0byte.chameleon.api.config.ConfigSection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Der paginierte Pet-Bereich eines Menues.
 *
 * <p>Beschreibt die Flaeche statt einer ausgerechneten Seitengroesse - die ergibt
 * sich bei Shark aus der Flaeche selbst.</p>
 *
 * @param mode       welche Pets angezeigt werden
 * @param fromRow    erste Zeile der Flaeche (0-basiert)
 * @param fromColumn erste Spalte der Flaeche (0-basiert)
 * @param toRow      letzte Zeile der Flaeche (0-basiert, inklusiv)
 * @param toColumn   letzte Spalte der Flaeche (0-basiert, inklusiv)
 * @param loreBlocks Lore-Bloecke, die in der Pet-Lore ueber Marker eingesetzt werden
 */
public record PetListConfig(
        @NotNull Mode mode,
        int fromRow,
        int fromColumn,
        int toRow,
        int toColumn,
        @NotNull Map<String, Map<String, List<String>>> loreBlocks) {

    public enum Mode {

        /** Alle Pets aus der {@code pets.yml}. */
        ALL,

        /** Nur die Pets, die der Spieler besitzt. */
        OWNED;

        @NotNull
        static Mode parse(@NotNull final String raw) {
            return "OWNED".equalsIgnoreCase(raw.trim()) ? OWNED : ALL;
        }
    }

    /**
     * Welchen Zustand ein Pet fuer den betrachtenden Spieler hat. Steuert, welche
     * Lore-Variante aus {@code lore-blocks} gezogen wird.
     */
    public enum State {

        ACTIVE,
        OWNED,
        NOT_OWNED;

        @NotNull
        public String key() {
            return name().toLowerCase(Locale.ROOT).replace('_', '-');
        }
    }

    @Nullable
    public static PetListConfig read(@Nullable final ConfigSection section) {
        if (section == null) {
            return null;
        }

        final ConfigSection area = section.section("area");
        return new PetListConfig(
                Mode.parse(section.getString("mode", "ALL")),
                area == null ? 1 : area.getInt("from-row", 1),
                area == null ? 1 : area.getInt("from-column", 1),
                area == null ? 3 : area.getInt("to-row", 3),
                area == null ? 7 : area.getInt("to-column", 7),
                readLoreBlocks(section.section("lore-blocks")));
    }

    /**
     * Liest {@code lore-blocks: <marker>: <zustand>: [zeilen]}.
     */
    @NotNull
    private static Map<String, Map<String, List<String>>> readLoreBlocks(@Nullable final ConfigSection section) {
        if (section == null) {
            return Map.of();
        }

        final var result = new java.util.LinkedHashMap<String, Map<String, List<String>>>();
        for (final String marker : section.keys(false)) {
            final ConfigSection states = section.section(marker);
            if (states == null) {
                continue;
            }
            final var perState = new java.util.LinkedHashMap<String, List<String>>();
            for (final String state : states.keys(false)) {
                perState.put(state, List.copyOf(states.getStringList(state)));
            }
            result.put(marker, Map.copyOf(perState));
        }
        return Map.copyOf(result);
    }

    /**
     * Baut die Lore-Bloecke fuer genau einen Pet-Zustand, so wie Sharks
     * {@code ButtonFactory} sie erwartet: Marker-Name auf Zeilenliste.
     */
    @NotNull
    public Map<String, List<String>> blocksFor(@NotNull final State state) {
        final var result = new java.util.LinkedHashMap<String, List<String>>();
        for (final var entry : this.loreBlocks.entrySet()) {
            final List<String> lines = entry.getValue().get(state.key());
            if (lines != null) {
                result.put(entry.getKey(), lines);
            }
        }
        return result;
    }
}
