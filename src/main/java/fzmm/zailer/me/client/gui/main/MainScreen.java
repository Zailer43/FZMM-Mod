package fzmm.zailer.me.client.gui.main;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.EncryptBookScreen;
import fzmm.zailer.me.client.gui.converters.ConvertersScreen;
import fzmm.zailer.me.client.gui.headgenerator.HeadGeneratorScreen;
import fzmm.zailer.me.client.gui.history.HistoryScreen;
import fzmm.zailer.me.client.gui.imagetext.ImagetextScreen;
import fzmm.zailer.me.client.gui.main.components.MainButtonComponent;
import fzmm.zailer.me.client.gui.playerstatue.PlayerStatueScreen;
import fzmm.zailer.me.client.gui.textformat.TextFormatScreen;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class MainScreen extends BaseFzmmScreen {

    public MainScreen(@Nullable Screen parent) {
        super("main", "main", parent);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        rootComponent.childById(ButtonComponent.class, "config-button")
                .onPress(button -> this.client.setScreen(ConfigScreen.create(FzmmClient.CONFIG, this)));

        Map<String, ButtonData> openScreenButtons = Map.of(
                "imagetext-button", new ButtonData(new ImagetextScreen(this), MainIcon.IMAGETEXT),
                "textFormat-button", new ButtonData(new TextFormatScreen(this), MainIcon.TEXT_FORMAT),
                "playerStatue-button", new ButtonData(new PlayerStatueScreen(this), MainIcon.PLAYER_STATUE),
                "encryptbook-button", new ButtonData(new EncryptBookScreen(this), MainIcon.ENCRYPTBOOK),
                "headGenerator-button", new ButtonData(new HeadGeneratorScreen(this), MainIcon.HEAD_GENERATOR),
                "converters-button", new ButtonData(new ConvertersScreen(this), MainIcon.CONVERTERS),
                "history-button", new ButtonData(new HistoryScreen(this), MainIcon.HISTORY)
        );

        for (var key : openScreenButtons.keySet()) {
            MainButtonComponent button = rootComponent.childById(MainButtonComponent.class, key);
            ButtonData data = openScreenButtons.get(key);
            if (button != null) {
                button.onPress(button1 -> this.client.setScreen(data.screen));
                button.setIcon(data.icon);
            }
        }
    }

    private record ButtonData(Screen screen, MainIcon icon) {
    }
}