package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.FzmmClient;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;

import javax.annotation.Nullable;

public class FzmmMainScreen extends BaseFzmmScreen {

    public FzmmMainScreen(@Nullable Screen parent) {
        super("main", "main", parent);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        rootComponent.childById(ButtonWidget.class, "config-button")
                .onPress(button -> this.client.setScreen(ConfigScreen.create(FzmmClient.CONFIG, this)));
        rootComponent.childById(ButtonWidget.class, "imagetext-button")
                .onPress(button -> this.client.setScreen(new ImagetextScreen(this)));
        rootComponent.childById(ButtonWidget.class, "headGenerator-button")
                .onPress(button -> this.client.setScreen(new HeadGeneratorScreen(this)));
    }
}