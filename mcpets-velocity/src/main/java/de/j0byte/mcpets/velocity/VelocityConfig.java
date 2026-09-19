package de.j0byte.mcpets.velocity;

import de.j0byte.chameleon.api.annotation.Comment;
import de.j0byte.chameleon.api.annotation.Config;
import de.j0byte.chameleon.api.annotation.Key;
import lombok.Getter;

/**
 * Die {@code config.yml} des Proxy-Teils, ueber Chameleon gebunden.
 *
 * <p>Der Channel muss mit dem der Paper-Server uebereinstimmen, sonst reden sie
 * aneinander vorbei.</p>
 */
@Config("config.yml")
@Comment("MCPets auf dem Proxy: Reload an alle Server schicken und Sync-Nachrichten mitlesen.")
@Getter
public class VelocityConfig {

    @Key("messaging.channel")
    @Comment("Muss derselbe Channel sein wie in der config.yml der Paper-Server.")
    private String syncChannel = "mcpets:sync";

    @Key("command.name")
    private String commandName = "mcpetsproxy";

    @Key("command.permission")
    private String commandPermission = "mcpets.admin";

    @Key("messages.usage")
    private String usageMessage = "<gray>Nutzung<dark_gray>: <aqua>/<command> reload";

    @Key("messages.no-permission")
    private String noPermissionMessage = "<red>Dafuer fehlt dir die Berechtigung.";

    @Key("messages.reload-sent")
    private String reloadSentMessage = "<gray>Reload an alle Server geschickt.";

    @Key("messages.reload-failed")
    private String reloadFailedMessage = "<red>Der Reload konnte nicht verschickt werden<dark_gray>: <aqua><reason>";

    @Key("log-sync-messages")
    @Comment("Jede empfangene Sync-Nachricht ins Proxy-Log schreiben. Nur zum Debuggen.")
    private boolean logSyncMessages = false;
}
