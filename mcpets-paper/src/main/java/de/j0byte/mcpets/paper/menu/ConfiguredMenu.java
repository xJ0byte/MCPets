package de.j0byte.mcpets.paper.menu;

import de.j0byte.mcpets.paper.config.ItemConfig;
import de.j0byte.mcpets.paper.config.MenuAction;
import de.j0byte.mcpets.paper.config.MenuConfig;
import de.j0byte.mcpets.paper.config.MenuItemConfig;
import de.j0byte.mcpets.paper.config.PetListConfig;
import de.j0byte.mcpets.paper.config.PetDefinition;
import de.j0byte.mcpets.paper.item.ItemFactory;
import de.j0byte.shark.gui.ClickableItem;
import de.j0byte.shark.gui.InventoryContents;
import de.j0byte.shark.gui.InventoryProvider;
import de.j0byte.shark.gui.PagedBuilder;
import de.j0byte.shark.gui.SlotPos;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Zeichnet ein beliebiges Menue aus der {@code menus.yml}.
 *
 * <p>Es gibt genau diesen einen Provider fuer alle Menues. Was er zeichnet,
 * entscheidet allein die Config: Fueller, paginierte Pet-Flaeche, Buttons mit
 * Slot, Item und Aktion. Deshalb laesst sich ein Menue umbauen, ohne dass eine
 * Zeile Java angefasst wird.</p>
 */
final class ConfiguredMenu implements InventoryProvider {

    private final MenuService service;
    private final MenuConfig menu;

    /** Das Pet, um das es geht - gesetzt im Einstellungsmenue, sonst {@code null}. */
    private final PetDefinition context;

    ConfiguredMenu(
            @NotNull final MenuService service,
            @NotNull final MenuConfig menu,
            @Nullable final PetDefinition context) {

        this.service = service;
        this.menu = menu;
        this.context = context;
    }

    /**
     * Mindestabstand zwischen zwei Neuzeichnungen.
     *
     * <p>Reine Vorsichtsmassnahme: sollte ein Zeichenvorgang jemals selbst eine
     * Aenderung ausloesen, gaebe es sonst einen Render pro Tick. Vier Ticks sind
     * fuer den Spieler nicht wahrnehmbar und deckeln das zuverlaessig.</p>
     */
    private static final int MIN_REFRESH_TICKS = 4;

    /** Stand der Spielerdaten, mit dem dieses Menue zuletzt gezeichnet wurde. */
    private long renderedRevision = Long.MIN_VALUE;

    private int ticksSinceRender;

    @Override
    public void init(@NotNull final Player player, @NotNull final InventoryContents contents) {
        this.renderedRevision = this.service.data().revision(player.getUniqueId());
        this.ticksSinceRender = 0;

        // Reihenfolge ist wichtig: der Rand zuerst, dann Pets und Buttons darueber,
        // und ganz zum Schluss der Fueller fuer das, was dann noch leer ist.
        drawBorderFiller(player, contents);
        drawPetList(player, contents);
        drawItems(player, contents);
        drawEmptyFiller(player, contents);
    }

    /**
     * Zeichnet neu, sobald sich die Daten des Spielers geaendert haben.
     *
     * <p>Laeuft jeden Tick, vergleicht aber nur eine Zahl. Dadurch steht ein neuer
     * Pet-Name sofort in der Lore, auch wenn das Menue waehrenddessen offen bleibt -
     * und genauso ein Pet, das gerade von einem anderen Server dazugekommen ist.</p>
     */
    @Override
    public void update(@NotNull final Player player, @NotNull final InventoryContents contents) {
        this.ticksSinceRender++;

        final long current = this.service.data().revision(player.getUniqueId());
        if (current == this.renderedRevision || this.ticksSinceRender < MIN_REFRESH_TICKS) {
            return;
        }
        init(player, contents);
    }

    // ------------------------------------------------------------------ Fueller

    /**
     * Legt den Fueller auf die Randslots - nur bei {@code fill-borders: true}.
     */
    private void drawBorderFiller(@NotNull final Player player, @NotNull final InventoryContents contents) {
        if (!this.menu.fillBorders()) {
            return;
        }
        filler(player).ifPresent(contents::fillBorders);
    }

    /**
     * Legt den Fueller auf alles, was am Ende noch leer ist - nur bei {@code fill-empty: true}.
     *
     * <p>Laeuft bewusst als letztes und ueberschreibt nichts: belegte Slots bleiben,
     * wie sie sind. Stehen beide Schalter auf {@code false}, bleibt das Menue leer -
     * genau das, was "Fueller aus" heissen soll.</p>
     */
    private void drawEmptyFiller(@NotNull final Player player, @NotNull final InventoryContents contents) {
        if (!this.menu.fillEmpty()) {
            return;
        }

        final Optional<ClickableItem> filler = filler(player);
        if (filler.isEmpty()) {
            return;
        }

        for (int row = 0; row < this.menu.rows(); row++) {
            for (int column = 0; column < MenuConfig.COLUMNS; column++) {
                final SlotPos slot = SlotPos.of(row, column);
                if (contents.get(slot).isEmpty()) {
                    contents.set(slot, filler.get());
                }
            }
        }
    }

    @NotNull
    private Optional<ClickableItem> filler(@NotNull final Player player) {
        final ItemConfig filler = this.menu.filler();
        return filler == null
                ? Optional.empty()
                : Optional.of(ClickableItem.empty(build(filler, player, Map.of())));
    }

    // ------------------------------------------------------------------ Pet-Liste

    /**
     * Baut die paginierte Pet-Flaeche. Die Seitengroesse ergibt sich aus der Flaeche,
     * die Blaetter-, Such- und Sortierbuttons kommen aus Sharks GUI-Steuerung.
     */
    private void drawPetList(@NotNull final Player player, @NotNull final InventoryContents contents) {
        final PetListConfig list = this.menu.petList();
        if (list == null) {
            return;
        }

        final PagedBuilder paged = contents.paged()
                .items(petItems(player, list))
                .area(list.fromRow(), list.fromColumn(), list.toRow(), list.toColumn());

        apply(paged::previous, this.menu.previousSlot());
        apply(paged::next, this.menu.nextSlot());
        apply(paged::search, this.menu.searchSlot());
        apply(paged::sort, this.menu.sortSlot());

        try {
            paged.render();
        } catch (final IllegalArgumentException exception) {
            // Flaeche ausserhalb des Inventars oder ein Steuer-Button mitten in der
            // Pet-Flaeche. Ein Config-Fehler soll das Menue nicht komplett sprengen:
            // lieber ohne Pet-Liste oeffnen und im Log sagen, was zu korrigieren ist.
            this.service.logger().warning("Menu '" + this.menu.id() + "' has an invalid pet-list layout: "
                    + exception.getMessage());
        }
    }

    private void apply(
            @NotNull final java.util.function.Consumer<SlotPos> setter, @Nullable final SlotPos slot) {

        if (slot != null) {
            setter.accept(slot);
        }
    }

    /**
     * Ein Eintrag pro Pet. Linksklick aktiviert, Rechtsklick oeffnet die
     * Einstellungen - beides nur, wenn der Spieler das Pet besitzt.
     */
    @NotNull
    private List<ClickableItem> petItems(@NotNull final Player player, @NotNull final PetListConfig list) {
        final var result = new ArrayList<ClickableItem>();

        for (final PetDefinition definition : this.service.configs().pets().values()) {
            final boolean owned = this.service.data().owns(player, definition);

            if (list.mode() == PetListConfig.Mode.OWNED && !owned) {
                continue;
            }
            if (!owned && definition.hiddenWhenLocked()) {
                continue;
            }

            result.add(petItem(player, list, definition, owned));
        }
        return result;
    }

    @NotNull
    private ClickableItem petItem(
            @NotNull final Player player,
            @NotNull final PetListConfig list,
            @NotNull final PetDefinition definition,
            final boolean owned) {

        final boolean active = definition.id()
                .equals(this.service.data().activePet(player.getUniqueId()));
        final PetListConfig.State state = active
                ? PetListConfig.State.ACTIVE
                : owned ? PetListConfig.State.OWNED : PetListConfig.State.NOT_OWNED;

        final ItemStack stack = build(definition.icon(), player, list.blocksFor(state), definition);

        if (!owned) {
            // Kein Handler: ein fremdes Pet reagiert auf keinen Klick.
            return ClickableItem.empty(stack);
        }

        return ClickableItem.builder(stack)
                .sortKey(this.service.pets().resolveName(player.getUniqueId(), definition))
                .left(click -> this.service.activate(click.player(), definition))
                .right(click -> this.service.openSettings(click.player(), definition))
                .build();
    }

    // ------------------------------------------------------------------ Buttons

    private void drawItems(@NotNull final Player player, @NotNull final InventoryContents contents) {
        final int slots = this.menu.rows() * MenuConfig.COLUMNS;

        for (final MenuItemConfig config : this.menu.items().values()) {
            if (!config.visible()) {
                continue;
            }
            if (config.slot() >= slots) {
                this.service.logger().warning("Button '" + config.key() + "' of menu '" + this.menu.id()
                        + "' sits on slot " + config.slot() + ", but the menu only has " + slots + " slots.");
                continue;
            }
            item(player, config).ifPresent(item -> contents.set(this.menu.position(config.slot()), item));
        }
    }

    @NotNull
    private Optional<ClickableItem> item(@NotNull final Player player, @NotNull final MenuItemConfig config) {
        return config.type() == MenuItemConfig.Type.ACTIVE_PET
                ? activePetItem(player, config)
                : staticItem(player, config);
    }

    @NotNull
    private Optional<ClickableItem> staticItem(
            @NotNull final Player player, @NotNull final MenuItemConfig config) {

        if (config.item() == null) {
            return Optional.empty();
        }
        final ItemStack stack = build(config.item(), player, Map.of(), this.context);
        return Optional.of(clickable(stack, config, this.context));
    }

    /**
     * Der Button, der das gerade aktive Pet zeigt.
     *
     * <p>Hat der Spieler keins aktiviert, kommt {@code empty-item} zum Zug - in der
     * mitgelieferten Config eine Barrier, aber frei konfigurierbar.</p>
     */
    @NotNull
    private Optional<ClickableItem> activePetItem(
            @NotNull final Player player, @NotNull final MenuItemConfig config) {

        final Optional<PetDefinition> active = this.service.configs()
                .pet(this.service.data().activePet(player.getUniqueId()));

        if (active.isEmpty()) {
            if (config.emptyItem() == null) {
                return Optional.empty();
            }
            return Optional.of(ClickableItem.empty(build(config.emptyItem(), player, Map.of())));
        }

        // Das Pet selbst zeigen, mit genau dem Icon aus der pets.yml, das auch in der
        // Pet-Liste steht. Ein eigenes "item" am Button waere hier nur eine zweite
        // Stelle, an der dasselbe Pet anders aussieht.
        final PetDefinition definition = active.get();
        final ItemStack stack = build(definition.icon(), player, config.loreBlocks(), definition);
        return Optional.of(clickable(stack, config, definition));
    }

    /**
     * Haengt die konfigurierte Aktion an das Item. {@link MenuAction#NONE} bleibt
     * bewusst ohne Handler - dann ist das Item reine Deko und laesst sich auch nicht
     * herausnehmen.
     */
    @NotNull
    private ClickableItem clickable(
            @NotNull final ItemStack stack,
            @NotNull final MenuItemConfig config,
            @Nullable final PetDefinition target) {

        if (config.action() == MenuAction.NONE) {
            return ClickableItem.empty(stack);
        }
        return ClickableItem.builder(stack)
                .any(click -> run(click.player(), config, target))
                .build();
    }

    private void run(
            @NotNull final Player player,
            @NotNull final MenuItemConfig config,
            @Nullable final PetDefinition target) {

        switch (config.action()) {
            case OPEN_MENU -> {
                if (config.target() != null) {
                    this.service.open(player, config.target(), this.context);
                }
            }
            case CLOSE -> player.closeInventory();
            case DESPAWN -> this.service.despawn(player);
            case OPEN_ACTIVE_SETTINGS -> openActiveSettings(player);
            case RENAME -> withContext(player, this.service::rename);
            case RESET_NAME -> withContext(player, this.service::resetName);
            case NONE -> {
                // Kann hier nicht auftreten, siehe clickable(...).
            }
        }
    }

    /**
     * Oeffnet die Einstellungen des gerade aktiven Pets.
     *
     * <p>Das aktive Pet wird hier frisch nachgeschlagen, damit die Aktion auch an
     * einem gewoehnlichen Button funktioniert und nicht nur an dem mit
     * {@code type: ACTIVE_PET}.</p>
     */
    private void openActiveSettings(@NotNull final Player player) {
        this.service.configs()
                .pet(this.service.data().activePet(player.getUniqueId()))
                .ifPresent(definition -> this.service.openSettings(player, definition));
    }

    /**
     * Aktionen des Einstellungsmenues brauchen das Pet, um das es geht.
     */
    private void withContext(
            @NotNull final Player player,
            @NotNull final java.util.function.BiConsumer<Player, PetDefinition> action) {

        if (this.context != null) {
            action.accept(player, this.context);
        }
    }

    // ------------------------------------------------------------------ Items bauen

    @NotNull
    private ItemStack build(
            @NotNull final ItemConfig item,
            @NotNull final Player player,
            @NotNull final Map<String, List<String>> loreBlocks) {

        return build(item, player, loreBlocks, this.context);
    }

    /**
     * Baut das Item ueber die {@link ItemFactory}: CraftEngine-Items ueber
     * CraftEngine, alles andere ueber Sharks {@code ButtonFactory} - in beiden
     * Faellen mit Lore-Bloecken und Pet-Platzhaltern.
     */
    @NotNull
    private ItemStack build(
            @NotNull final ItemConfig item,
            @NotNull final Player player,
            @NotNull final Map<String, List<String>> loreBlocks,
            @Nullable final PetDefinition definition) {

        final TagResolver[] placeholders = this.service.placeholders(player, definition);
        return this.service.items().create(item, player, loreBlocks, placeholders);
    }
}
