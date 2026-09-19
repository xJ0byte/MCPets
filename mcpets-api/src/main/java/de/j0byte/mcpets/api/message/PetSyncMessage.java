package de.j0byte.mcpets.api.message;

import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Serveruebergreifende Nachricht ueber Octopus.
 *
 * <p>Geht per Redis raus: geht eine Nachricht verloren, ist der Zustand nur kurz
 * veraltet und korrigiert sich beim naechsten Laden aus MongoDB von selbst.
 * Die verlaessliche Quelle bleibt immer die Datenbank.</p>
 *
 * @param type   was passiert ist
 * @param player betroffener Spieler, {@code null} bei {@link Type#RELOAD}
 * @param petId  betroffenes Pet, {@code null} wenn die Nachricht kein einzelnes Pet meint
 */
public record PetSyncMessage(@NotNull Type type, @Nullable UUID player, @Nullable String petId) {

    /**
     * Name des Redis-Channels. Der konkrete Wert kommt aus der {@code config.yml},
     * das hier ist nur der Standard.
     */
    public static final String DEFAULT_CHANNEL = "mcpets:sync";

    public enum Type {

        /** Alle Server sollen ihre Configs neu laden. */
        RELOAD,

        /** Besitz oder Einstellungen des Spielers haben sich geaendert. */
        PROFILE_CHANGED,

        /** Der Spieler hat sein Pet auf einem anderen Server aktiviert. */
        PET_ACTIVATED,

        /** Der Spieler hat sein Pet abgesetzt. */
        PET_DESPAWNED
    }

    @NotNull
    public static PetSyncMessage reload() {
        return new PetSyncMessage(Type.RELOAD, null, null);
    }

    @NotNull
    public static PetSyncMessage profileChanged(@NotNull final UUID player) {
        return new PetSyncMessage(Type.PROFILE_CHANGED, player, null);
    }

    @NotNull
    public static PetSyncMessage petActivated(@NotNull final UUID player, @NotNull final String petId) {
        return new PetSyncMessage(Type.PET_ACTIVATED, player, petId);
    }

    @NotNull
    public static PetSyncMessage petDespawned(@NotNull final UUID player) {
        return new PetSyncMessage(Type.PET_DESPAWNED, player, null);
    }
}
