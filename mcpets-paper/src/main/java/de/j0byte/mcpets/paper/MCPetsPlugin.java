package de.j0byte.mcpets.paper;

import com.google.inject.Guice;
import com.google.inject.Injector;
import de.j0byte.chameleon.api.ChameleonProvider;
import de.j0byte.chameleon.api.config.ConfigStore;
import de.j0byte.mcpets.api.message.PetSyncMessage;
import de.j0byte.mcpets.paper.command.PetsCommand;
import de.j0byte.mcpets.paper.config.ConfigManager;
import de.j0byte.mcpets.paper.listener.PetListener;
import de.j0byte.mcpets.paper.pet.PetService;
import de.j0byte.mcpets.paper.storage.PetDataService;
import de.j0byte.mcpets.paper.storage.PetStorage;
import de.j0byte.mcpets.paper.storage.PetSyncService;
import de.j0byte.octopus.api.OctopusModule;
import de.j0byte.shark.api.Shark;
import de.j0byte.shark.api.SharkProvider;
import java.util.logging.Level;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

/**
 * MCPets auf Paper.
 *
 * <p>Das Plugin bringt selbst fast nichts mit: Configs kommen von Chameleon,
 * Menues und Nachrichten von Shark, Speicherung und Messaging von Octopus und die
 * Modelle von BetterModel. Alle vier laufen als eigene Plugins auf dem Server und
 * werden in der {@code paper-plugin.yml} als Abhaengigkeit deklariert.</p>
 */
public final class MCPetsPlugin extends JavaPlugin {

    private Injector injector;
    private PetService pets;
    private PetSyncService sync;

    @Override
    public void onEnable() {
        final Shark shark = SharkProvider.get();
        final ConfigStore store = ChameleonProvider.get().store(this);

        // OctopusModule bindet lazy, der Injector darf also gebaut werden,
        // bevor ByteOctopus fertig gestartet ist.
        this.injector = Guice.createInjector(new OctopusModule(), new MCPetsModule(this, shark, store));

        final ConfigManager configs = this.injector.getInstance(ConfigManager.class);
        configs.load();

        // Indizes anlegen: join() ist nur hier im Start erlaubt.
        try {
            this.injector.getInstance(PetStorage.class).prepare().join();
        } catch (final RuntimeException exception) {
            getLogger().log(Level.SEVERE,
                    "Could not prepare the MongoDB collections. Is ByteOctopus connected?", exception);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        this.pets = this.injector.getInstance(PetService.class);
        this.pets.start();

        this.sync = this.injector.getInstance(PetSyncService.class);
        this.sync.start(this::onSyncMessage);

        getServer().getPluginManager()
                .registerEvents(this.injector.getInstance(PetListener.class), this);
        this.injector.getInstance(PetsCommand.class).register(this);

        getLogger().info("MCPets enabled with " + configs.pets().size() + " pets and "
                + configs.menus().size() + " menus.");
    }

    @Override
    public void onDisable() {
        if (this.sync != null) {
            this.sync.stop();
        }
        if (this.pets != null) {
            this.pets.stop();
            this.pets.despawnAll();
        }
    }

    /**
     * Reagiert auf die Nachricht eines anderen Servers.
     *
     * <p>Die gecachten Daten werden verworfen, damit der naechste Zugriff sie frisch
     * aus MongoDB holt. Die Datenbank bleibt die verlaessliche Quelle - eine
     * verlorene Redis-Nachricht bedeutet nur kurz veraltete Daten.</p>
     */
    private void onSyncMessage(@NotNull final PetSyncMessage message) {
        final PetDataService data = this.injector.getInstance(PetDataService.class);

        switch (message.type()) {
            case RELOAD -> getServer().getScheduler().runTask(this, () -> {
                this.pets.despawnAll();
                this.injector.getInstance(ConfigManager.class).reload();
                this.pets.start();
                data.invalidateAll();
                getLogger().info("Reloaded the configuration after a sync message.");
            });
            case PROFILE_CHANGED, PET_ACTIVATED, PET_DESPAWNED -> {
                if (message.player() != null) {
                    data.invalidate(message.player());
                }
            }
        }
    }
}
