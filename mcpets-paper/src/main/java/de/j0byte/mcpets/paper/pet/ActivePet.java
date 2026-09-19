package de.j0byte.mcpets.paper.pet;

import de.j0byte.mcpets.paper.config.PetDefinition;
import java.util.UUID;
import kr.toxicity.model.api.tracker.EntityTracker;
import lombok.Getter;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

/**
 * Ein gerade gespawntes Pet.
 *
 * <p>Besteht aus zwei Teilen: der unsichtbaren Basis-Entity, die sich bewegt, und
 * dem {@link EntityTracker} von BetterModel, der das Modell darauf rendert.</p>
 */
@Getter
public final class ActivePet {

    private final UUID owner;
    private final PetDefinition definition;
    private final Entity baseEntity;
    private final EntityTracker tracker;

    /** Ob sich das Pet im letzten Durchlauf bewegt hat - steuert die Lauf-Animation. */
    private boolean walking;

    public ActivePet(
            @NotNull final UUID owner,
            @NotNull final PetDefinition definition,
            @NotNull final Entity baseEntity,
            @NotNull final EntityTracker tracker) {

        this.owner = owner;
        this.definition = definition;
        this.baseEntity = baseEntity;
        this.tracker = tracker;
    }

    @NotNull
    public String petId() {
        return this.definition.id();
    }

    public boolean alive() {
        return this.baseEntity.isValid() && !this.tracker.isClosed();
    }

    /**
     * Schaltet zwischen Lauf- und Leerlauf-Animation um.
     *
     * <p>Passiert nur beim Wechsel, nicht in jedem Tick - sonst wuerde die Animation
     * dauernd neu gestartet.</p>
     *
     * @return {@code true} wenn sich der Zustand geaendert hat
     */
    public boolean walking(final boolean walking) {
        if (this.walking == walking) {
            return false;
        }
        this.walking = walking;
        return true;
    }
}
