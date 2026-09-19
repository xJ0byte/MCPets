package de.j0byte.mcpets.api.document;

import de.j0byte.octopus.api.mongo.annotation.Document;
import de.j0byte.octopus.api.mongo.annotation.Id;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Was ein Spieler an Pets besitzt und welches davon gerade aktiv ist.
 *
 * <p>Liegt in MongoDB, verwaltet ueber Octopus. Ein Dokument pro Spieler; die
 * Sammlung kommt aus der {@code config.yml} und ist nicht fest verdrahtet.</p>
 */
@Document
@Getter
@Setter
public class PetProfile {

    @Id
    private UUID uuid;

    private String name;

    /** Pet-IDs aus der {@code pets.yml}, die dieser Spieler besitzt. */
    private List<String> ownedPets = new ArrayList<>();

    /** Pet-ID des zuletzt aktivierten Pets, oder {@code null}. */
    private String activePet;

    private Instant lastSeen;

    public PetProfile() {
    }

    public PetProfile(@NotNull final UUID uuid, @NotNull final String name) {
        this.uuid = uuid;
        this.name = name;
        this.lastSeen = Instant.now();
    }

    public boolean owns(@NotNull final String petId) {
        return this.ownedPets.contains(petId);
    }

    /**
     * @return {@code true} wenn das Pet vorher nicht im Besitz war
     */
    public boolean grant(@NotNull final String petId) {
        if (this.ownedPets.contains(petId)) {
            return false;
        }
        this.ownedPets.add(petId);
        return true;
    }

    /**
     * Nimmt das Pet weg. War es gerade aktiv, wird es auch abgewaehlt.
     *
     * @return {@code true} wenn das Pet vorher im Besitz war
     */
    public boolean revoke(@NotNull final String petId) {
        if (!this.ownedPets.remove(petId)) {
            return false;
        }
        if (petId.equals(this.activePet)) {
            this.activePet = null;
        }
        return true;
    }

    @Nullable
    public String activePet() {
        return this.activePet;
    }

    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        return other instanceof PetProfile profile && Objects.equals(this.uuid, profile.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.uuid);
    }
}
