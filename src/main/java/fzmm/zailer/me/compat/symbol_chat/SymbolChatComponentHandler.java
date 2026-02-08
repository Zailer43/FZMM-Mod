package fzmm.zailer.me.compat.symbol_chat;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.compat.CompatMods;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.component.VanillaWidgetComponent;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;

public class SymbolChatComponentHandler<T extends VanillaWidgetComponent> {
    @Nullable
    private T component;
    private final net.minecraft.network.chat.Component buttonText;
    private final net.minecraft.network.chat.Component buttonTooltip;
    private final net.minecraft.network.chat.Component notAvailableTooltip;
    private final SymbolChatCompat compat;

    public SymbolChatComponentHandler(SymbolChatCompat compat, net.minecraft.network.chat.Component buttonText, net.minecraft.network.chat.Component buttonTooltip, net.minecraft.network.chat.Component notAvailableTooltip) {
        this.compat = compat;

        this.buttonText = buttonText;
        this.buttonTooltip = buttonTooltip;
        this.notAvailableTooltip = buttonTooltip.copy().append("\n\n").append(notAvailableTooltip);
    }

    public UIComponent initButton(BaseFzmmScreen screen, EditBox selectedComponent, Supplier<T> componentSupplier) {
        UIComponent result = UIComponents.button(this.buttonText, button -> this.buttonExecute(screen, selectedComponent, componentSupplier));
        result.sizing(Sizing.fixed(20));

        ((ButtonComponent) result).active = CompatMods.SYMBOL_CHAT_PRESENT;

        if (CompatMods.SYMBOL_CHAT_PRESENT) {
            result.tooltip(this.buttonTooltip);
        } else {
            result = EContainers.horizontalFlow(Sizing.content(), Sizing.content())
                    .child(result)
                    .tooltip(this.notAvailableTooltip);
        }

        return result;
    }

    protected void buttonExecute(BaseFzmmScreen screen, EditBox selectedComponent, Supplier<T> componentSupplier) {
        if (!CompatMods.SYMBOL_CHAT_PRESENT) {
            return;
        }

        try {
            if (this.isMounted()) {
                EditBox newSelected = selectedComponent;

                if (this.compat.selectedComponent() == newSelected) {
                    this.remove();
                    newSelected = null;
                }

                this.compat.selectedComponent(newSelected);
                return;
            }

            this.compat.font().remove();
            this.compat.symbol().remove();
            this.compat.selectedComponent(selectedComponent);

            // TODO: make the draggable wrapper functional with this
            this.component = componentSupplier.get();
            screen.child(this.component.positioning(Positioning.absolute(0, 0)));
        } catch (NoClassDefFoundError | NoSuchMethodError e) {
            CompatMods.SYMBOL_CHAT_PRESENT = false;
            FzmmClient.LOGGER.error("[SymbolChatComponent] Failed to create component", e);
        }
    }

    public boolean isMounted() {
        if (!CompatMods.SYMBOL_CHAT_PRESENT || this.component == null) {
            return false;
        }

        return this.component.hasParent();
    }

    public void remove() {
        if (!CompatMods.SYMBOL_CHAT_PRESENT || this.component == null) {
            return;
        }

        this.component.remove();
        this.component = null;
    }

    public boolean charTyped(CharacterEvent input) {
        return this.component != null && this.component.onCharTyped(input);
    }

    public boolean keyPressed(KeyEvent input) {
        return this.component != null && this.component.onKeyPress(input);
    }
    
    public Optional<T> getComponent() {
        return Optional.ofNullable(this.component);
    }
}
