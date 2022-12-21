package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.converters.ConvertersScreen;
import fzmm.zailer.me.client.gui.headgenerator.HeadGeneratorScreen;
import fzmm.zailer.me.client.gui.imagetext.ImagetextScreen;
import fzmm.zailer.me.client.gui.playerstatue.PlayerStatueScreen;
import fzmm.zailer.me.client.gui.textformat.TextFormatScreen;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class FzmmMainScreen extends BaseFzmmScreen {

    public FzmmMainScreen(@Nullable Screen parent) {
        super("main", "main", parent);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        rootComponent.childById(ButtonComponent.class, "config-button")
                .onPress(button -> this.client.setScreen(ConfigScreen.create(FzmmClient.CONFIG, this)));

        Map<String, Screen> openScreenButtons = Map.of(
                "imagetext-button", new ImagetextScreen(this),
                "textFormat-button", new TextFormatScreen(this),
                "playerStatue-button", new PlayerStatueScreen(this),
                "encryptbook-button", new EncryptBookScreen(this),
                "headGenerator-button", new HeadGeneratorScreen(this),
                "converters-button", new ConvertersScreen(this)
        );

        for (var key : openScreenButtons.keySet()) {
            ButtonComponent button = rootComponent.childById(ButtonComponent.class, key);

            if (button != null)
                button.onPress(button1 -> this.client.setScreen(openScreenButtons.get(key)));
        }
    }
}