package fzmm.zailer.me.client.gui.player_statue;


import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.NumberRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.components.tabs.ITab;
import fzmm.zailer.me.client.gui.components.tabs.TabContainer;
import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.gui.player_statue.tabs.IPlayerStatueTab;
import fzmm.zailer.me.client.gui.player_statue.tabs.PlayerStatueGenerateTab;
import fzmm.zailer.me.client.gui.player_statue.tabs.PlayerStatueUpdateTab;
import fzmm.zailer.me.client.gui.utils.InvisibleEntityWarning;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.client.logic.player_statue.StatuePart;
import fzmm.zailer.me.utils.FzmmWikiConstants;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class PlayerStatueScreen extends BaseFzmmScreen implements IMemento {
    public static final String EXECUTE_ID = "execute-button";
    private HorizontalDirectionOption direction;
    private ConfigTextBox posX;
    private ConfigTextBox posY;
    private ConfigTextBox posZ;
    private TextBoxComponent nameField;
    private TabContainer tabContainer;

    public PlayerStatueScreen(@Nullable Screen parent) {
        super("player_statue", "playerStatue", parent);
    }

    @Override
    protected void setup(EFlowLayout rootComponent) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        //general
        ContextMenuButton directionButton = rootComponent.childByIdOrThrow(ContextMenuButton.class, "horizontal-direction-context-menu-option");
        directionButton.setContextMenuOptions(dropdownComponent -> {
            for (var option : HorizontalDirectionOption.values()) {
                dropdownComponent.button(Text.translatable(option.getTranslationKey()), dropdownButton -> {
                    this.direction = option;
                    directionButton.setMessage(Text.translatable(option.getTranslationKey()));
                    dropdownButton.remove();
                });
            }
        });
        this.direction = HorizontalDirectionOption.getPlayerHorizontalDirection();
        directionButton.setMessage(Text.translatable(this.direction.getTranslationKey()));
        this.posX = NumberRow.setup(rootComponent, "posX", player.getBlockX(), Float.class);
        this.posY = NumberRow.setup(rootComponent, "posY", player.getY(), Float.class);
        this.posZ = NumberRow.setup(rootComponent, "posZ", player.getBlockZ(), Float.class);
        this.nameField = TextBoxRow.setup(rootComponent, "name", "", 0xFFFF);
        //tabs
        this.tabContainer = rootComponent.childByIdOrThrow(TabContainer.class, "tabs");
        List<ITab> tabs = List.of(new PlayerStatueGenerateTab(), new PlayerStatueUpdateTab());
        this.tabContainer.addParsedTabs(tabs);
        this.tabContainer.setupTabs(rootComponent, tabs.get(0).getId(), IPlayerStatueTab::canExecute);
        this.tabContainer.selectTab();
        //buttons
        rootComponent.childByIdOrThrow(ButtonComponent.class, "faq-button").onPress(this::faqExecute);
        ButtonComponent executeButton = rootComponent.childByIdOrThrow(ButtonComponent.class, EXECUTE_ID).onPress(this::execute);
        executeButton.active(this.tabContainer.<IPlayerStatueTab>selectedTab().canExecute());

        rootComponent.childByIdOrThrow(ButtonComponent.class, "difficult-to-remove-entity-button").onPress(buttonComponent ->
                InvisibleEntityWarning.addOverlay(true, true, Text.translatable("fzmm.snack_bar.entityDifficultToRemove.entity.playerStatue"), StatuePart.PLAYER_STATUE_TAG)
        );
    }

    private void faqExecute(ButtonWidget buttonWidget) {
        assert this.client != null;
        ConfirmLinkScreen.open(client.currentScreen, FzmmWikiConstants.PLAYER_STATUE_WIKI_LINK);
    }

    private void execute(ButtonWidget buttonWidget) {
        float x = (float) this.posX.parsedValue();
        float y = (float) this.posY.parsedValue();
        float z = (float) this.posZ.parsedValue();
        String name = this.nameField.getText();

        this.tabContainer.<IPlayerStatueTab>selectedTab().execute(this.direction, x, y, z, name);
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.nameField.getText());
        this.tabContainer.backup(output);
    }

    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.nameField.text((String) input.readObject());
        this.tabContainer.restore(input);
    }
}