package fzmm.zailer.me.client.gui.playerstatue;


import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class PlayerStatueScreen extends BaseFzmmScreen {
    private static final String PLAYER_STATUE_FAQ_LINK = "https://github.com/Zailer43/FZMM-Mod/wiki/FAQ-Player-Statue";
    private static final String HORIZONTAL_DIRECTION_ID = "horizontal-direction";
    private static final String POS_X_ID = "posX";
    private static final String POS_Y_ID = "posY";
    private static final String POS_Z_ID = "posZ";
    private static final String POS_TOOLTIP_ID = "pos";
    private static final String NAME_ID = "name";
    public static final String EXECUTE_ID = "execute";
    private static PlayerStatueTabs selectedTab = PlayerStatueTabs.CREATE;
    private EnumWidget directionEnum;
    private ConfigTextBox posX;
    private ConfigTextBox posY;
    private ConfigTextBox posZ;
    private TextFieldWidget nameField;

    private ButtonWidget executeButton;


    public PlayerStatueScreen(@Nullable Screen parent) {
        super("player_statue", "playerStatue", parent);
    }

    @Override
    protected void tryAddComponentList(FlowLayout rootComponent) {
        this.tryAddComponentList(rootComponent, "player-statue-options-list",
                this.newEnumRow(HORIZONTAL_DIRECTION_ID),
                this.newNumberRow(POS_X_ID, POS_TOOLTIP_ID),
                this.newNumberRow(POS_Y_ID, POS_TOOLTIP_ID),
                this.newNumberRow(POS_Z_ID, POS_TOOLTIP_ID),
                this.newTextFieldRow(NAME_ID),
                this.newScreenTabRow(selectedTab)
        );

        FlowLayout container = rootComponent.childById(FlowLayout.class, "player-statue-options-list");
        if (container == null)
            return;

        for (var tab : PlayerStatueTabs.values())
            container.child(this.newScreenTab(tab.getId(), tab.getComponents(this)));
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        PlayerEntity player = MinecraftClient.getInstance().player;
        assert player != null;
        //general
        this.directionEnum = this.setupEnum(rootComponent, HORIZONTAL_DIRECTION_ID, HorizontalDirectionOption.getPlayerHorizontalDirection(), null);
        this.posX = this.setupNumberField(rootComponent, POS_X_ID, player.getBlockX(), Float.class);
        this.posY = this.setupNumberField(rootComponent, POS_Y_ID, player.getY(), Float.class);
        this.posZ = this.setupNumberField(rootComponent, POS_Z_ID, player.getBlockZ(), Float.class);
        this.nameField = this.setupTextField(rootComponent, NAME_ID, "");
        //tabs
        for (var tab : PlayerStatueTabs.values()) {
            tab.setupComponents(this, rootComponent);
            this.setupButton(rootComponent, this.getScreenTabButtonId(tab), tab != selectedTab, button -> {
                this.selectScreenTab(rootComponent, tab);
                selectedTab = tab;
                this.executeButton.active = selectedTab.canExecute();
            });
        }
        this.selectScreenTab(rootComponent, selectedTab);
        //buttons
        this.setupButton(rootComponent, this.getButtonId("faq"), true, this::faqExecute);
        this.executeButton = this.setupButton(rootComponent, this.getButtonId(EXECUTE_ID), true, this::execute);
        this.executeButton.active = selectedTab.canExecute();
    }

    private void faqExecute(ButtonWidget buttonWidget) {
        assert this.client != null;

        this.client.setScreen(new ConfirmLinkScreen(bool -> {
            if (bool)
                Util.getOperatingSystem().open(PLAYER_STATUE_FAQ_LINK);

            this.client.setScreen(this);
        }, PLAYER_STATUE_FAQ_LINK, true));
    }

    private void execute(ButtonWidget buttonWidget) {
        HorizontalDirectionOption direction = (HorizontalDirectionOption) this.directionEnum.parsedValue();
        float x = (float) this.posX.parsedValue();
        float y = (float) this.posY.parsedValue();
        float z = (float) this.posZ.parsedValue();
        String name = this.nameField.getText();

        selectedTab.execute(direction, x, y, z, name);
    }

}