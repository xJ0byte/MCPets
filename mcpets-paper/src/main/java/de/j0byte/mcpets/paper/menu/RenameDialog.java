package de.j0byte.mcpets.paper.menu;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import de.j0byte.mcpets.paper.config.ConfigManager;
import de.j0byte.mcpets.paper.config.PetDefinition;
import de.j0byte.shark.api.Shark;
import io.papermc.paper.dialog.Dialog;
import io.papermc.paper.registry.data.dialog.ActionButton;
import io.papermc.paper.registry.data.dialog.DialogBase;
import io.papermc.paper.registry.data.dialog.action.DialogAction;
import io.papermc.paper.registry.data.dialog.input.DialogInput;
import java.util.List;
import java.util.function.Consumer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickCallback;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * Freitext-Eingabe zum Umbenennen eines Pets ueber die Paper Dialog API.
 *
 * <p>Konvention in diesem Projekt: Freitext laeuft immer ueber Dialoge, nie ueber
 * einen Chat-Prompt oder ein AnvilGUI. Alle Beschriftungen kommen aus der
 * {@code messages.yml}.</p>
 */
@Singleton
public class RenameDialog {

    private static final String INPUT_KEY = "name";
    private static final int INPUT_WIDTH = 200;
    private static final int BUTTON_WIDTH = 100;

    private final Plugin plugin;
    private final ConfigManager configs;
    private final Shark shark;

    @Inject
    public RenameDialog(
            @NotNull final Plugin plugin,
            @NotNull final ConfigManager configs,
            @NotNull final Shark shark) {

        this.plugin = plugin;
        this.configs = configs;
        this.shark = shark;
    }

    /**
     * Schliesst das Menue, zeigt den Dialog und ruft danach auf dem Main-Thread zurueck.
     *
     * @param prefill  vorbelegter Name
     * @param onSubmit bekommt den eingegebenen Text
     * @param onCancel laeuft beim Abbrechen
     */
    public void open(
            @NotNull final Player player,
            @NotNull final PetDefinition definition,
            @NotNull final String prefill,
            @NotNull final Consumer<String> onSubmit,
            @NotNull final Runnable onCancel) {

        final var petName = Placeholder.unparsed("pet_id", definition.id());

        final Component title = render(player, "dialog.rename.title", petName);
        final Component label = render(player, "dialog.rename.label", petName);
        final Component confirm = render(player, "dialog.rename.confirm", petName);
        final Component cancel = render(player, "dialog.rename.cancel", petName);

        final DialogInput input = DialogInput.text(
                INPUT_KEY,
                INPUT_WIDTH,
                label,
                true,
                prefill,
                this.configs.general().getNameMaxLength(),
                null);

        final ActionButton confirmButton = ActionButton.create(
                confirm,
                null,
                BUTTON_WIDTH,
                DialogAction.customClick(
                        (response, audience) -> {
                            final String value = response.getText(INPUT_KEY);
                            runOnMain(() -> onSubmit.accept(value == null ? "" : value));
                        },
                        ClickCallback.Options.builder().uses(1).build()));

        final ActionButton cancelButton = ActionButton.create(
                cancel,
                null,
                BUTTON_WIDTH,
                DialogAction.customClick(
                        (response, audience) -> runOnMain(onCancel),
                        ClickCallback.Options.builder().uses(1).build()));

        final Dialog dialog = Dialog.create(builder -> builder.empty()
                .base(DialogBase.builder(title)
                        .canCloseWithEscape(true)
                        .inputs(List.of(input))
                        .build())
                .type(io.papermc.paper.registry.data.dialog.type.DialogType
                        .confirmation(confirmButton, cancelButton)));

        player.closeInventory();
        player.showDialog(dialog);
    }

    /**
     * Der Dialog-Callback kommt nicht garantiert auf dem Main-Thread an, das Oeffnen
     * eines GUIs danach braucht ihn aber.
     */
    private void runOnMain(@NotNull final Runnable action) {
        if (this.plugin.getServer().isPrimaryThread()) {
            action.run();
            return;
        }
        this.plugin.getServer().getScheduler().runTask(this.plugin, action);
    }

    @NotNull
    private Component render(
            @NotNull final Player player,
            @NotNull final String messageId,
            final net.kyori.adventure.text.minimessage.tag.resolver.TagResolver... extra) {

        return this.shark.messages().render(player, this.configs.message(messageId), extra);
    }
}
