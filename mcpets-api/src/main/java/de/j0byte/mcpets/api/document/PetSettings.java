package de.j0byte.mcpets.api.document;

import de.j0byte.octopus.api.mongo.annotation.Document;
import de.j0byte.octopus.api.mongo.annotation.Id;
import de.j0byte.octopus.api.mongo.annotation.Index;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Einstellungen eines Spielers fuer genau ein Pet - pro Spieler und pro Pet.
 *
 * <p>Die ID ist zusammengesetzt aus Spieler-UUID und Pet-ID, damit ein Spieler
 * fuer jedes Pet eigene Einstellungen hat und ein einzelner Lookup reicht.</p>
 */
@Document
@Index("owner")
@Getter
@Setter
public class PetSettings {

    @Id
    private String id;

    private UUID owner;

    private String petId;

    /** Vom Spieler gesetzter Name, oder {@code null} fuer den Namen aus der {@code pets.yml}. */
    private String displayName;

    private Instant updatedAt;

    public PetSettings() {
    }

    public PetSettings(@NotNull final UUID owner, @NotNull final String petId) {
        this.id = documentId(owner, petId);
        this.owner = owner;
        this.petId = petId;
        this.updatedAt = Instant.now();
    }

    @NotNull
    public static String documentId(@NotNull final UUID owner, @NotNull final String petId) {
        return owner + ":" + petId;
    }

    public boolean hasCustomName() {
        return this.displayName != null && !this.displayName.isBlank();
    }

    /**
     * @param fallback der Name aus der {@code pets.yml}
     * @return der eigene Name des Spielers, sonst {@code fallback}
     */
    @NotNull
    public String displayNameOr(@NotNull final String fallback) {
        return hasCustomName() ? this.displayName : fallback;
    }

    public void resetDisplayName() {
        this.displayName = null;
        this.updatedAt = Instant.now();
    }

    public void displayName(@Nullable final String displayName) {
        this.displayName = displayName == null || displayName.isBlank() ? null : displayName;
        this.updatedAt = Instant.now();
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof PetSettings settings && Objects.equals(this.id, settings.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.id);
    }
}
