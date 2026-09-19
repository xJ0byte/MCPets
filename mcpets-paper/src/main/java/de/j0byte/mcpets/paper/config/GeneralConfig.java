package de.j0byte.mcpets.paper.config;

import de.j0byte.chameleon.api.annotation.Comment;
import de.j0byte.chameleon.api.annotation.Config;
import de.j0byte.chameleon.api.annotation.Key;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * Die {@code config.yml}, typisiert ueber Chameleon gebunden.
 *
 * <p>Die Feldwerte sind die Standardwerte: fehlt ein Schluessel in der Datei,
 * schreibt Chameleon ihn beim Start mit genau diesem Wert nach.</p>
 */
@Config("config.yml")
@Comment("MCPets - Hauptkonfiguration. Menues stehen in menus.yml, Pets in pets.yml.")
@Getter
public class GeneralConfig {

    @Key("storage.profiles-collection")
    @Comment("MongoDB-Collection fuer Besitz und aktives Pet je Spieler.")
    private String profilesCollection = "mcpets_profiles";

    @Key("storage.settings-collection")
    @Comment("MongoDB-Collection fuer die Pet-Einstellungen je Spieler und Pet.")
    private String settingsCollection = "mcpets_settings";

    @Key("messaging.channel")
    @Comment("Redis-Channel, ueber den die Server sich gegenseitig informieren.")
    private String syncChannel = "mcpets:sync";

    @Key("messaging.broker.enabled")
    @Comment({
            "Zusaetzlich ueber RabbitMQ verteilen.",
            "Redis reicht fuer den Normalfall: geht eine Nachricht verloren, ist der",
            "Zustand nur kurz veraltet und korrigiert sich beim naechsten Laden aus MongoDB.",
            "RabbitMQ lohnt sich, wenn ein Reload garantiert jeden Server erreichen muss."})
    private boolean brokerEnabled = false;

    @Key("messaging.broker.exchange")
    private String brokerExchange = "mcpets";

    @Key("messaging.broker.routing-key")
    private String brokerRoutingKey = "sync.broadcast";

    @Key("messaging.broker.queue-prefix")
    @Comment("Jeder Server braucht eine eigene Queue, sonst bekommt nur einer die Nachricht.")
    private String brokerQueuePrefix = "mcpets.sync";

    @Key("pets.despawn-on-quit")
    @Comment("Pet beim Verlassen des Servers absetzen.")
    private boolean despawnOnQuit = true;

    @Key("pets.respawn-on-join")
    @Comment("Zuletzt aktives Pet beim Betreten wieder spawnen.")
    private boolean respawnOnJoin = true;

    @Key("pets.despawn-on-death")
    private boolean despawnOnDeath = true;

    @Key("pets.follow.interval-ticks")
    @Comment("Wie oft die Folge-Logik laeuft. Kleiner = fluessiger, aber teurer.")
    private int followIntervalTicks = 2;

    @Key("pets.follow.start-distance")
    @Comment("Ab dieser Entfernung laeuft das Pet dem Besitzer hinterher.")
    private double followStartDistance = 4.0D;

    @Key("pets.follow.stop-distance")
    @Comment("Naeher als das kommt das Pet nicht heran.")
    private double followStopDistance = 2.5D;

    @Key("pets.follow.teleport-distance")
    @Comment("Ab dieser Entfernung wird das Pet teleportiert statt zu laufen.")
    private double followTeleportDistance = 25.0D;

    @Key("pets.follow.speed")
    @Comment("Blocks pro Durchlauf, die sich das Pet dem Besitzer naehert.")
    private double followSpeed = 0.35D;

    @Key("pets.spawn.offset")
    @Comment("Wie weit hinter dem Besitzer das Pet erscheint.")
    private double spawnOffset = 1.5D;

    @Key("pets.name.max-length")
    private int nameMaxLength = 32;

    @Key("pets.name.strip-formatting")
    @Comment("Farb- und Formatierungs-Tags aus selbst gesetzten Namen entfernen.")
    private boolean nameStripFormatting = true;

    @Key("pets.name.blocked")
    @Comment("Namen, die niemand setzen darf. Gross-/Kleinschreibung egal.")
    private List<String> nameBlocked = new ArrayList<>(List.of("admin", "owner"));

    @Key("permissions.use")
    private String usePermission = "mcpets.use";

    @Key("permissions.admin")
    private String adminPermission = "mcpets.admin";

    @Key("menus.main")
    @Comment("Welches Menue /pets oeffnet.")
    private String mainMenu = "main";

    @Key("menus.settings")
    @Comment("Welches Menue der Rechtsklick auf ein Pet oeffnet.")
    private String settingsMenu = "pet-settings";

    @Key("debug")
    private boolean debug = false;
}
