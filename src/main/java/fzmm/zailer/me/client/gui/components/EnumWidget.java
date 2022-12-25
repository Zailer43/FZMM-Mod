package fzmm.zailer.me.client.gui.components;

import io.wispforest.owo.config.ui.component.ConfigEnumButton;
import io.wispforest.owo.ui.component.ButtonComponent;

import java.util.function.Consumer;


@SuppressWarnings("UnstableApiUsage")
public class EnumWidget extends ConfigEnumButton {

    public EnumWidget() {
        super();
    }

    public void init(Enum<? extends IMode> enumeration) {
        this.backingValues = enumeration.getClass().getEnumConstants();
        this.select(enumeration.ordinal());
    }

    @Override
    protected void updateMessage() {
        if (this.backingValues == null)
            return;

        this.setMessage(((IMode) this.backingValues[this.selectedIndex]).getTranslation());
    }

    public Enum<?> getValue() {
        return this.backingValues[this.selectedIndex];
    }


    @Override
    public ButtonComponent onPress(Consumer<ButtonComponent> button) {
        this.onPress();
        return super.onPress(button);
    }
}
