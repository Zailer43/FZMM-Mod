package fzmm.zailer.me.compat.symbol_chat;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.compat.CompatMods;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.VanillaWidgetComponent;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;

public class SymbolChatComponentHandler<T extends VanillaWidgetComponent> {
    @Nullable
    private T component;
    private final Text buttonText;
    private final Text buttonTooltip;
    private final Text notAvailableTooltip;
    private final SymbolChatCompat compat;

    public SymbolChatComponentHandler(SymbolChatCompat compat, Text buttonText, Text buttonTooltip, Text notAvailableTooltip) {
        this.compat = compat;

        this.buttonText = buttonText;
        this.buttonTooltip = buttonTooltip;
        this.notAvailableTooltip = buttonTooltip.copy().append("\n\n").append(notAvailableTooltip);
    }

    public Component initButton(BaseFzmmScreen screen, TextFieldWidget selectedComponent, Supplier<T> componentSupplier) {
        Component result = Components.button(this.buttonText, button -> this.buttonExecute(screen, selectedComponent, componentSupplier));
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

    protected void buttonExecute(BaseFzmmScreen screen, TextFieldWidget selectedComponent, Supplier<T> componentSupplier) {
        if (!CompatMods.SYMBOL_CHAT_PRESENT) {
            return;
        }

        try {
            if (this.isMounted()) {
                TextFieldWidget newSelected = selectedComponent;

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

    public boolean charTyped(char chr, int modifiers) {
        return this.component != null && this.component.onCharTyped(chr, modifiers);
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return this.component != null && this.component.onKeyPress(keyCode, scanCode, modifiers);
    }
    
    public Optional<T> getComponent() {
        return Optional.ofNullable(this.component);
    }
}
