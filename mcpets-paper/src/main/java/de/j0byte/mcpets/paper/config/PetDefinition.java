package de.j0byte.mcpets.paper.config;

import de.j0byte.chameleon.api.config.ConfigSection;
import java.util.List;
import java.util.Locale;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Ein Pet, wie es in der {@code pets.yml} steht.
 *
 * <p>Das Modell kommt von BetterModel: {@link #model()} ist der Modellname, den
 * BetterModel kennt. Getragen wird es von einer unsichtbaren Basis-Entity, deren
 * Typ ebenfalls in der Config steht.</p>
 *
 * @param id              Schluessel in der {@code pets.yml}
 * @param model           BetterModel-Modellname
 * @param defaultName     Standard-Anzeigename im MiniMessage-Format
 * @param permission      Permission zum Besitzen, leer = keine Permission noetig
 * @param baseEntity      Entity-Typ, der das Modell traegt
 * @param scale           Groesse der Basis-Entity
 * @param nameVisible     ob das Nametag dauerhaft sichtbar ist
 * @param nametagBone     Name des Bones im Modell, an dem das Nametag haengt
 * @param idleAnimation   Animation im Stand, oder {@code null}
 * @param walkAnimation   Animation beim Laufen, oder {@code null}
 * @param spawnAnimation  Animation beim Spawnen, oder {@code null}
 * @param icon            Item im Menue
 * @param hiddenWhenLocked ob das Pet im Alle-Pets-Menue versteckt wird, solange man es nicht besitzt
 */
public record PetDefinition(
        @NotNull String id,
        @NotNull String model,
        @NotNull String defaultName,
        @NotNull String permission,
        @NotNull EntityType baseEntity,
        double scale,
        boolean nameVisible,
        @NotNull String nametagBone,
        @Nullable String idleAnimation,
        @Nullable String walkAnimation,
        @Nullable String spawnAnimation,
        @NotNull ItemConfig icon,
        boolean hiddenWhenLocked) {

    private static final EntityType DEFAULT_BASE_ENTITY = EntityType.ARMOR_STAND;

    /** Bone-Name, an dem BetterModel-Modelle ueblicherweise ihr Nametag tragen. */
    private static final String DEFAULT_NAMETAG_BONE = "name";

    private static final ItemConfig DEFAULT_ICON =
            new ItemConfig("NAME_TAG", "<primary><pet_name>", List.of(), 0, null, 1);

    @NotNull
    public static PetDefinition read(@NotNull final String id, @NotNull final ConfigSection section) {
        return new PetDefinition(
                id,
                section.getString("model", id),
                section.getString("name", id),
                section.getString("permission", ""),
                baseEntity(section.getString("base-entity", DEFAULT_BASE_ENTITY.name())),
                section.getDouble("scale", 1.0D),
                section.getBoolean("name-visible", true),
                section.getString("nametag-bone", DEFAULT_NAMETAG_BONE),
                nullIfBlank(section.getString("animations.idle")),
                nullIfBlank(section.getString("animations.walk")),
                nullIfBlank(section.getString("animations.spawn")),
                ItemConfig.readOr(section.section("icon"), DEFAULT_ICON),
                section.getBoolean("hidden-when-locked", false));
    }

    /**
     * Ob dieses Pet dem Spieler schon durch seine Permission gehoert.
     *
     * <p>Ein Pet kann auf zwei Wegen zu einem Spieler kommen: ueber
     * {@code /pets admin give} (steht dann in der Datenbank) oder ueber die hier
     * eingetragene Permission, z. B. aus einem Rang. Ein leeres Feld heisst
     * ausdruecklich <b>nicht</b> "gehoert allen" - dann zaehlt nur die Datenbank.</p>
     */
    public boolean grantedByPermission(@NotNull final org.bukkit.permissions.Permissible permissible) {
        return !this.permission.isBlank() && permissible.hasPermission(this.permission);
    }

    /**
     * Ein unbekannter Entity-Typ soll kein Pet verschlucken, deshalb Fallback statt Fehler.
     */
    @NotNull
    private static EntityType baseEntity(@NotNull final String raw) {
        try {
            return EntityType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (final IllegalArgumentException exception) {
            return DEFAULT_BASE_ENTITY;
        }
    }

    @Nullable
    private static String nullIfBlank(@Nullable final String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
