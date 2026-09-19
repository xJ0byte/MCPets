package de.j0byte.mcpets.paper.item;

import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.bukkit.item.BukkitItemDefinition;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Die einzige Klasse, die CraftEngine-Typen anfasst.
 *
 * <p>Sie wird bewusst nur dann geladen, wenn CraftEngine auch wirklich auf dem
 * Server liegt - siehe {@link ItemFactory}. Dadurch laeuft MCPets ohne
 * CraftEngine ganz normal weiter, statt beim ersten Menue mit einem
 * {@code NoClassDefFoundError} auszusteigen.</p>
 */
final class CraftEngineItemProvider {

    /**
     * Baut das CraftEngine-Item zu einer {@code namespace:id}.
     *
     * <p>Gebaut wird spielerbezogen, damit CraftEngine seine eigenen Platzhalter
     * im Item aufloesen kann.</p>
     *
     * @return das Item, oder {@code null} wenn CraftEngine die ID nicht kennt
     */
    @Nullable
    ItemStack item(@NotNull final String namespacedId, @Nullable final Player viewer) {
        final BukkitItemDefinition definition = CraftEngineItems.byId(namespacedId);
        if (definition == null) {
            return null;
        }
        return viewer == null ? definition.buildBukkitItem() : definition.buildBukkitItem(viewer);
    }
}
