package de.j0byte.mcpets.paper.config;

import de.j0byte.chameleon.api.config.ConfigSection;
import de.j0byte.shark.api.config.ButtonDefinition;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Ein Item, wie es in {@code menus.yml} oder {@code pets.yml} steht.
 *
 * <p>Nichts davon ist im Code festgelegt: Material, Name, Lore, CustomModelData,
 * Item-Model und Stackgroesse kommen alle aus der Config. Gebaut wird das Item am
 * Ende von Sharks {@code ButtonFactory}, damit Farb-Tags und PlaceholderAPI greifen.</p>
 */
public record ItemConfig(
        @NotNull String material,
        @NotNull String name,
        @NotNull List<String> lore,
        int customModelData,
        @Nullable String itemModel,
        int amount) {

    /** Material, mit dem ein fehlerhaft konfiguriertes Item angezeigt wird. */
    private static final String FALLBACK_MATERIAL = "BARRIER";

    public ItemConfig {
        material = material == null || material.isBlank() ? FALLBACK_MATERIAL : material;
        name = name == null ? "" : name;
        lore = lore == null ? List.of() : List.copyOf(lore);
        amount = amount <= 0 ? 1 : amount;
    }

    /**
     * Liest ein Item aus einer Config-Section.
     *
     * @param section die Section, darf {@code null} sein
     * @return das Item, oder {@code null} wenn die Section fehlt
     */
    @Nullable
    public static ItemConfig read(@Nullable final ConfigSection section) {
        if (section == null) {
            return null;
        }
        return new ItemConfig(
                section.getString("material", FALLBACK_MATERIAL),
                section.getString("name", ""),
                section.getStringList("lore"),
                section.getInt("custom-model-data", 0),
                section.getString("item-model"),
                section.getInt("amount", 1));
    }

    /**
     * Wie {@link #read(ConfigSection)}, liefert aber nie {@code null}.
     */
    @NotNull
    public static ItemConfig readOr(@Nullable final ConfigSection section, @NotNull final ItemConfig fallback) {
        final ItemConfig read = read(section);
        return read == null ? fallback : read;
    }

    /**
     * Uebersetzt das Item in Sharks {@link ButtonDefinition}.
     *
     * <p>{@code customModelData == 0} heisst laut Shark-Konvention "nicht setzen",
     * deshalb wird daraus hier {@code null}.</p>
     */
    @NotNull
    public ButtonDefinition toButton() {
        return new ButtonDefinition(
                this.material,
                this.name,
                this.lore,
                this.customModelData == 0 ? null : this.customModelData,
                this.itemModel == null || this.itemModel.isBlank() ? null : this.itemModel,
                this.amount);
    }
}
