package de.j0byte.mcpets.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import de.j0byte.chameleon.api.ChameleonProvider;
import de.j0byte.chameleon.api.config.ConfigStore;
import de.j0byte.mcpets.api.message.PetSyncMessage;
import de.j0byte.octopus.api.Octopus;
import de.j0byte.octopus.api.messaging.Channel;
import de.j0byte.octopus.api.messaging.Subscription;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

/**
 * MCPets auf dem Proxy.
 *
 * <p>Der Proxy spawnt keine Pets - das passiert ausschliesslich auf den
 * Paper-Servern. Er haengt nur am selben Octopus-Channel und kann von dort aus
 * einen Reload an alle Server schicken.</p>
 *
 * <p>Der Pet-Zustand selbst wandert nicht ueber den Proxy: er steht in MongoDB,
 * und der Zielserver liest ihn beim Join des Spielers.</p>
 */
@Plugin(
        id = "mcpets",
        name = "MCPets",
        version = BuildConstants.VERSION,
        description = "Proxy-Teil von MCPets: Reload an alle Server und Sync-Nachrichten.",
        authors = {"j0byte"},
        dependencies = {
            @Dependency(id = "byteoctopus"),
            @Dependency(id = "chameleon")
        })
public final class MCPetsVelocityPlugin {

    private final ProxyServer server;
    private final Logger logger;

    private VelocityConfig config;
    private Channel channel;
    private Subscription subscription;
    private CommandMeta commandMeta;

    @Inject
    public MCPetsVelocityPlugin(@NotNull final ProxyServer server, @NotNull final Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialize(@NotNull final ProxyInitializeEvent event) {
        final ConfigStore store = ChameleonProvider.get().store(this);
        this.config = store.bind(VelocityConfig.class);

        final Octopus octopus = Octopus.get();
        this.channel = octopus.messaging().channel(this.config.getSyncChannel());
        this.subscription = this.channel.subscribe(PetSyncMessage.class, message -> {
            if (this.config.isLogSyncMessages()) {
                this.logger.info("Pet sync message from {}: {}", message.source(), message.payload().type());
            }
        });

        registerCommand();
        this.logger.info("MCPets is listening on the pet sync channel '{}'.", this.config.getSyncChannel());
    }

    @Subscribe
    public void onProxyShutdown(@NotNull final ProxyShutdownEvent event) {
        if (this.commandMeta != null) {
            this.server.getCommandManager().unregister(this.commandMeta);
            this.commandMeta = null;
        }
        if (this.subscription != null) {
            this.subscription.unsubscribe();
            this.subscription = null;
        }
        this.channel = null;
    }

    private void registerCommand() {
        final CommandManager manager = this.server.getCommandManager();
        this.commandMeta = manager.metaBuilder(this.config.getCommandName())
                .plugin(this)
                .build();
        manager.register(this.commandMeta, new MCPetsVelocityCommand(this.config, this.channel));
    }
}
