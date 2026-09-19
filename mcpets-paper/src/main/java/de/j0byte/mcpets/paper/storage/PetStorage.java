package de.j0byte.mcpets.paper.storage;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.j0byte.mcpets.api.document.PetProfile;
import de.j0byte.mcpets.api.document.PetSettings;
import de.j0byte.mcpets.paper.config.ConfigManager;
import de.j0byte.octopus.api.Octopus;
import de.j0byte.octopus.api.mongo.MongoCollection;
import de.j0byte.octopus.api.mongo.MongoFilter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.jetbrains.annotations.NotNull;

/**
 * MongoDB-Zugriff ueber Octopus.
 *
 * <p>Alles hier ist asynchron. {@code join()} ist nur beim Start erlaubt - im
 * laufenden Betrieb blockiert es den Main-Thread.</p>
 */
@Singleton
public class PetStorage {

    private final MongoCollection<PetProfile> profiles;
    private final MongoCollection<PetSettings> settings;

    @Inject
    public PetStorage(@NotNull final Octopus octopus, @NotNull final ConfigManager configs) {
        this.profiles = octopus.mongo()
                .collection(configs.general().getProfilesCollection(), PetProfile.class);
        this.settings = octopus.mongo()
                .collection(configs.general().getSettingsCollection(), PetSettings.class);
    }

    /**
     * Legt die Indizes an. Nur beim Start aufrufen.
     */
    @NotNull
    public CompletableFuture<Void> prepare() {
        return CompletableFuture.allOf(
                this.profiles.createIndexes(),
                this.settings.createIndexes());
    }

    // ------------------------------------------------------------------ Profile

    @NotNull
    public CompletableFuture<Optional<PetProfile>> findProfile(@NotNull final UUID uuid) {
        return this.profiles.findById(uuid);
    }

    /**
     * Laedt das Profil oder legt ein leeres an, ohne es schon zu speichern.
     */
    @NotNull
    public CompletableFuture<PetProfile> loadOrCreateProfile(
            @NotNull final UUID uuid, @NotNull final String name) {

        return findProfile(uuid).thenApply(found -> {
            final PetProfile profile = found.orElseGet(() -> new PetProfile(uuid, name));
            profile.setName(name);
            return profile;
        });
    }

    @NotNull
    public CompletableFuture<PetProfile> saveProfile(@NotNull final PetProfile profile) {
        profile.setLastSeen(java.time.Instant.now());
        return this.profiles.save(profile);
    }

    // ------------------------------------------------------------------ Einstellungen

    @NotNull
    public CompletableFuture<Optional<PetSettings>> findSettings(
            @NotNull final UUID owner, @NotNull final String petId) {

        return this.settings.findById(PetSettings.documentId(owner, petId));
    }

    /**
     * Alle Pet-Einstellungen eines Spielers in einem Rutsch - ein Lookup beim Join
     * statt einer Abfrage pro Pet.
     */
    @NotNull
    public CompletableFuture<List<PetSettings>> findAllSettings(@NotNull final UUID owner) {
        return this.settings.find(MongoFilter.eq("owner", owner)).toList();
    }

    @NotNull
    public CompletableFuture<PetSettings> saveSettings(@NotNull final PetSettings value) {
        return this.settings.save(value);
    }

    @NotNull
    public CompletableFuture<Boolean> deleteSettings(
            @NotNull final UUID owner, @NotNull final String petId) {

        return this.settings.deleteById(PetSettings.documentId(owner, petId));
    }
}
