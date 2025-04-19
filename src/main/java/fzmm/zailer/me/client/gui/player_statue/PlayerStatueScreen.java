package fzmm.zailer.me.client.gui.player_statue;


import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.ContextMenuButton;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.NumberRow;
import fzmm.zailer.me.client.gui.components.row.ScreenTabRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.gui.player_statue.tabs.IPlayerStatueTab;
import fzmm.zailer.me.client.gui.player_statue.tabs.PlayerStatueTabs;
import fzmm.zailer.me.client.gui.utils.InvisibleEntityWarning;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.gui.utils.memento.IMementoScreen;
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

import java.util.HashMap;
import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class PlayerStatueScreen extends BaseFzmmScreen implements IMementoScreen {
    public static final String EXECUTE_ID = "execute-button";
    private static PlayerStatueTabs selectedTab = PlayerStatueTabs.CREATE;
    private static PlayerStatueMemento memento = null;
    private HorizontalDirectionOption direction;
    private ConfigTextBox posX;
    private ConfigTextBox posY;
    private ConfigTextBox posZ;
    private TextBoxComponent nameField;

    private ButtonWidget executeButton;


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
        this.setTabs(selectedTab);
        ScreenTabRow.setup(rootComponent, "tabs", selectedTab);
        for (var playerStatueTab : PlayerStatueTabs.values()) {
            IScreenTab tab = this.getTab(playerStatueTab, IPlayerStatueTab.class);
            tab.setupComponents(rootComponent);
            ButtonComponent button = rootComponent.childByIdOrThrow(ButtonComponent.class, ScreenTabRow.getScreenTabButtonId(tab));
            button.active(!tab.getId().equals(selectedTab.getId()));
            button.onPress(buttonComponent -> {
                selectedTab = this.selectScreenTab(rootComponent, tab, selectedTab);
                this.executeButton.active = this.getTab(selectedTab, IPlayerStatueTab.class).canExecute();
            });
        }
        this.selectScreenTab(rootComponent, selectedTab, selectedTab);
        //buttons
        rootComponent.childByIdOrThrow(ButtonComponent.class, "faq-button").onPress(this::faqExecute);
        this.executeButton = rootComponent.childByIdOrThrow(ButtonComponent.class, EXECUTE_ID).onPress(this::execute);
        this.executeButton.active = this.getTab(selectedTab, IPlayerStatueTab.class).canExecute();

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

        this.getTab(selectedTab, IPlayerStatueTab.class).execute(this.direction, x, y, z, name);
    }

    @Override
    public void setMemento(IMementoObject memento) {
        PlayerStatueScreen.memento = (PlayerStatueMemento) memento;
    }

    @Override
    public Optional<IMementoObject> getMemento() {
        return Optional.ofNullable(memento);
    }

    @Override
    public IMementoObject createMemento() {
        return new PlayerStatueMemento(
                this.nameField.getText(),
                this.createMementoTabs()
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        PlayerStatueMemento memento = (PlayerStatueMemento) mementoObject;
        this.nameField.text(memento.name());
        this.restoreMementoTabs(memento.mementoTabHashMap);
    }

    private record PlayerStatueMemento(String name,
                                       HashMap<String, IMementoObject> mementoTabHashMap) implements IMementoObject {

    }
}