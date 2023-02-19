package fzmm.zailer.me.client.gui.components;

import io.wispforest.owo.config.ui.component.ConfigEnumButton;
import io.wispforest.owo.ui.component.ButtonComponent;
import net.minecraft.text.Text;

import java.util.function.Consumer;


@SuppressWarnings("UnstableApiUsage")
public class EnumWidget extends ConfigEnumButton {

    private boolean showTooltip;

    public EnumWidget() {
        super();
        this.showTooltip = false;
    }

    public void init(Enum<? extends IMode> enumeration) {
        this.backingValues = enumeration.getClass().getEnumConstants();
        this.select(enumeration.ordinal());
    }

    @Override
    protected void updateMessage() {
        if (this.backingValues == null)
            return;

        String translationKey = ((IMode) this.backingValues[this.selectedIndex]).getTranslationKey();
        this.setMessage(Text.translatable(translationKey));

        if (this.showTooltip)
            this.tooltip(Text.translatable(translationKey + ".tooltip"));
    }

    public Enum<?> getValue() {
        return this.backingValues[this.selectedIndex];
    }

    public void setValue(Enum<?> value) {
        this.select(value.ordinal());
        this.onPress.onPress(this);
        this.updateMessage();
    }

    @Override
    public ButtonComponent onPress(Consumer<ButtonComponent> button) {
        this.onPress();
        return super.onPress(button);
    }

    public void setShowTooltip(boolean showTooltip) {
        this.showTooltip = showTooltip;
    }
}
