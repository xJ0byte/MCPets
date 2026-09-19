package de.j0byte.mcpets.velocity;

import com.velocitypowered.api.command.SimpleCommand;
import de.j0byte.mcpets.api.message.PetSyncMessage;
import de.j0byte.octopus.api.messaging.Channel;
import java.util.List;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.jetbrains.annotations.NotNull;

/**
 * Der Proxy-Befehl von MCPets.
 *
 * <p>Schickt ueber Octopus einen Reload an alle Paper-Server. Praktisch, wenn man
 * ohnehin auf der Proxy-Konsole sitzt und nicht jeden Server einzeln anfassen will.</p>
 */
public final class MCPetsVelocityCommand implements SimpleCommand {

    private static final String RELOAD = "reload";

    private final VelocityConfig config;
    private final Channel channel;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public MCPetsVelocityCommand(@NotNull final VelocityConfig config, @NotNull final Channel channel) {
        this.config = config;
        this.channel = channel;
    }

    @Override
    public void execute(@NotNull final Invocation invocation) {
        if (!hasPermission(invocation)) {
            invocation.source().sendMessage(
                    this.miniMessage.deserialize(this.config.getNoPermissionMessage()));
            return;
        }

        final String[] arguments = invocation.arguments();
        if (arguments.length == 0 || !RELOAD.equalsIgnoreCase(arguments[0])) {
            invocation.source().sendMessage(this.miniMessage.deserialize(
                    this.config.getUsageMessage(), Placeholder.unparsed("command", invocation.alias())));
            return;
        }

        this.channel.publish(PetSyncMessage.reload())
                .thenRun(() -> invocation.source().sendMessage(
                        this.miniMessage.deserialize(this.config.getReloadSentMessage())))
                .exceptionally(error -> {
                    invocation.source().sendMessage(this.miniMessage.deserialize(
                            this.config.getReloadFailedMessage(),
                            Placeholder.unparsed("reason", String.valueOf(error.getMessage()))));
                    return null;
                });
    }

    @Override
    @NotNull
    public List<String> suggest(@NotNull final Invocation invocation) {
        if (!hasPermission(invocation) || invocation.arguments().length > 1) {
            return List.of();
        }
        return List.of(RELOAD);
    }

    @Override
    public boolean hasPermission(@NotNull final Invocation invocation) {
        final String permission = this.config.getCommandPermission();
        return permission.isBlank() || invocation.source().hasPermission(permission);
    }
}
