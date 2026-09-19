# MCPets

Modellierte Pets fuer Paper-Server. Das Plugin bringt selbst fast nichts mit -
alles Querschnittliche kommt aus den eigenen Frameworks:

| Aufgabe | Erledigt von |
|---|---|
| Configs (`config.yml`, `pets.yml`, `menus.yml`, `messages.yml`) | **Chameleon** |
| Menues, Items und Nachrichten | **Shark** |
| MongoDB, Redis und RabbitMQ | **Octopus** |
| Modelle und Animationen | **BetterModel** |
| Commands | **CommandAPI 12** |

MythicMobs, ModelEngine und MySQL werden nicht mehr gebraucht.

## Module

| Modul | Inhalt |
|---|---|
| `mcpets-api` | Mongo-Dokumente und die Sync-Nachricht - von beiden Plattformen genutzt |
| `mcpets-paper` | Das Server-Plugin: Menues, Pets, Commands |
| `mcpets-velocity` | Der Proxy-Teil: Reload an alle Server, Sync-Nachrichten mitlesen |

## Bauen

```bash
./gradlew build
```

Die fertigen Jars liegen danach unter
`mcpets-paper/build/libs/MCPets-Paper-<version>.jar` und
`mcpets-velocity/build/libs/MCPets-Velocity-<version>.jar`.

Java 25 wird bei Bedarf automatisch nachgeladen (Foojay-Toolchain-Resolver), eine
lokale Installation ist also nicht noetig.

### Framework-Jars in `libs/`

Shark, Octopus und Chameleon laufen als eigene Plugins auf dem Server. Sie werden
deshalb **nicht** ueber JitPack eingebunden, sondern als lokale Jars per
`compileOnly` - und landen nie im eigenen Jar.

```
libs/shark-api-1.0.0.jar
libs/shark-gui-1.0.0.jar
libs/octopus-api-1.1.0.jar
libs/chameleon-api-1.0.0.jar
```

Aktualisiert werden sie von Hand, wenn sich eine der APIs weiterentwickelt:

```bash
# im jeweiligen Framework-Repository
./gradlew :shark-api:jar :shark-gui:jar    # Shark
./gradlew :api:jar                          # Octopus
./gradlew :chameleon-api:jar                # Chameleon
```

Die Dateinamen stehen in `gradle.properties` - wer eine andere Version ablegt,
passt sie dort an.

## Server-Voraussetzungen

Auf dem Paper-Server muessen laufen: `Shark`, `ByteOctopus`, `Chameleon`,
`CommandAPI` und `BetterModel`. Alle fuenf stehen in der `paper-plugin.yml` mit
`load: BEFORE` und `join-classpath: true`.

Auf dem Proxy reichen `ByteOctopus` und `Chameleon`.

## Commands

| Command | Permission | Wirkung |
|---|---|---|
| `/pets` | `mcpets.use` | oeffnet das Hauptmenue |
| `/pets admin reload` | `mcpets.admin` | laedt alle Configs neu - auch auf den anderen Servern |
| `/pets admin give <spieler> <pet>` | `mcpets.admin` | gibt einem Spieler ein Pet |
| `/pets admin remove <spieler> <pet>` | `mcpets.admin` | nimmt es wieder weg |
| `/mcpetsproxy reload` (Velocity) | `mcpets.admin` | schickt den Reload vom Proxy aus an alle Server |

Die Permissions stehen in der `config.yml` und sind frei aenderbar.

## Menues

Im Java-Code steht keine Slot-Nummer, kein Material und kein Anzeigetext. Alles
kommt aus `menus.yml`; es gibt genau einen Provider, der zeichnet, was dort steht.

* **Hauptmenue** - drei Buttons: das gerade aktive Pet (ohne aktives Pet ein frei
  konfigurierbares Ersatz-Item, standardmaessig eine Barrier), das Menue mit allen
  Pets und das Menue mit den eigenen Pets.
* **Alle Pets / Deine Pets** - paginiert, mit Suche und Sortierung aus Sharks
  GUI-Steuerung. Die Slots dafuer stehen unter `controls`.
* **Einstellungen** - Namen aendern (ueber die Paper Dialog API), Namen
  zuruecksetzen, Pet absetzen.

Auf einem Pet-Eintrag gilt: **Linksklick aktiviert**, **Rechtsklick oeffnet die
Einstellungen** - beides nur, wenn der Spieler das Pet besitzt.

## Besitz

Ein Pet gehoert einem Spieler auf zwei Wegen:

1. es wurde ihm mit `/pets admin give` gegeben (steht in MongoDB), oder
2. seine Permission aus `pets.yml` schaltet es frei.

`/pets admin remove` nimmt nur den Datenbank-Besitz weg - ein Pet aus einer
Permission wird ueber die Permission entzogen.

## Speicherung und Serversync

Besitz, aktives Pet und die Einstellungen je Spieler und Pet liegen in MongoDB.
Aendert sich etwas, geht eine kurze Nachricht ueber Redis an die anderen Server,
die daraufhin ihren Cache verwerfen. Geht so eine Nachricht verloren, ist der
Zustand nur kurz veraltet - die verlaessliche Quelle bleibt die Datenbank.

Wer einen Reload garantiert auf jedem Server sehen will, schaltet in der
`config.yml` zusaetzlich RabbitMQ dazu (`messaging.broker.enabled`). Jeder Server
bekommt dann seine eigene Queue am selben Fanout-Exchange.

## Lizenz

GNU General Public License v3.0, siehe [LICENSE](LICENSE).
