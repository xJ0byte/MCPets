package de.j0byte.mcpets.paper.config;

import java.util.Locale;
import org.jetbrains.annotations.NotNull;

/**
 * Was ein Klick auf ein Menue-Item ausloest. Steht als {@code action} in der
 * {@code menus.yml} und ist damit pro Button frei waehlbar.
 */
public enum MenuAction {

    /** Nichts - reine Deko. */
    NONE,

    /** Oeffnet das Menue aus {@code target}. */
    OPEN_MENU,

    /** Schliesst das Menue. */
    CLOSE,

    /** Setzt das gerade aktive Pet ab. */
    DESPAWN,

    /** Oeffnet die Einstellungen des gerade aktiven Pets. */
    OPEN_ACTIVE_SETTINGS,

    /** Oeffnet den Dialog zum Umbenennen (nur im Einstellungsmenue). */
    RENAME,

    /** Setzt den Namen auf den Standard aus der {@code pets.yml} zurueck. */
    RESET_NAME;

    /**
     * @return die Aktion zum Config-Wert, {@link #NONE} wenn der Wert unbekannt ist
     */
    @NotNull
    public static MenuAction parse(@NotNull final String raw) {
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (final IllegalArgumentException exception) {
            return NONE;
        }
    }
}
