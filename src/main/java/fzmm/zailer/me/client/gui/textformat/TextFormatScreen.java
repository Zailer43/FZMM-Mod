package fzmm.zailer.me.client.gui.textformat;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.utils.CopyTextScreen;
import fzmm.zailer.me.client.gui.components.row.BooleanRow;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.ScreenTabRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.config.ui.component.ConfigToggleButton;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;


@SuppressWarnings("UnstableApiUsage")
public class TextFormatScreen extends BaseFzmmScreen {
    public static final Text EMPTY_COLOR_TEXT = Text.translatable("fzmm.gui.textFormat.error.emptyColor").setStyle(Style.EMPTY.withColor(0xF2200D));
    private static final String MESSAGE_PREVIEW_ID = "message-preview";
    private static final String MESSAGE_ID = "message";
    private static final String BOLD_ID = "bold";
    private static final String ITALIC_ID = "italic";
    private static final String OBFUSCATED_ID = "obfuscated";
    private static final String STRIKETHROUGH_ID = "strikethrough";
    private static final String UNDERLINE_ID = "underline";
    private static final String ADD_LORE_ID = "add-lore";
    private static final String SET_NAME_ID = "set-name";
    private static final String COPY_ID = "copy";
    private static final String RANDOM_ID = "random";
    private static TextFormatTabs selectedTab = TextFormatTabs.TWO_COLORS;
    private LabelComponent messagePreviewLabel;
    private TextFieldWidget messageTextField;
    private ConfigToggleButton boldToggle;
    private ConfigToggleButton italicToggle;
    private ConfigToggleButton obfuscatedToggle;
    private ConfigToggleButton strikethroughToggle;
    private ConfigToggleButton underlineToggle;
    private List<ButtonWidget> executeButtons;
    private boolean initialized;

    public TextFormatScreen(@Nullable Screen parent) {
        super("text_format", "textFormat", parent);
        this.initialized = false;
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        this.messagePreviewLabel = rootComponent.childById(LabelComponent.class, MESSAGE_PREVIEW_ID);
        BaseFzmmScreen.checkNull(this.messagePreviewLabel, "label", MESSAGE_PREVIEW_ID);

        for (var tab : TextFormatTabs.values())
            tab.componentsCallback(object -> this.updateMessagePreview());
        //general
        this.messageTextField = TextBoxRow.setup(rootComponent, MESSAGE_ID, "Hello world", s -> this.updateMessagePreview());
        this.messageTextField.setMaxLength(4096);
        this.boldToggle = BooleanRow.setup(rootComponent, BOLD_ID, false, button -> this.updateMessagePreview());
        this.italicToggle = BooleanRow.setup(rootComponent, ITALIC_ID, false, button -> this.updateMessagePreview());
        this.obfuscatedToggle = BooleanRow.setup(rootComponent, OBFUSCATED_ID, false, button -> this.updateMessagePreview());
        this.strikethroughToggle = BooleanRow.setup(rootComponent, STRIKETHROUGH_ID, false, button -> this.updateMessagePreview());
        this.underlineToggle = BooleanRow.setup(rootComponent, UNDERLINE_ID, false, button -> this.updateMessagePreview());
        //tabs
        ScreenTabRow.setup(rootComponent, "tabs", selectedTab);
        for (var tab : TextFormatTabs.values()) {
            tab.setupComponents(rootComponent);
            ButtonRow.setup(rootComponent, ScreenTabRow.getScreenTabButtonId(tab), tab != selectedTab, button -> {
                this.selectScreenTab(rootComponent, tab);
                selectedTab = tab;
                this.updateMessagePreview();
            });
        }
        this.selectScreenTab(rootComponent, selectedTab);

        this.setupBottomButtons(rootComponent);
        this.initialized = true;
        this.updateMessagePreview();
    }

    private void setupBottomButtons(FlowLayout rootComponent) {
        assert this.client != null;
        assert client.player != null;
        FzmmConfig.TextFormat config = FzmmClient.CONFIG.textFormat;

        boolean executeButtonsActive = this.messageTextField.getText().length() > 1;
        ButtonWidget addLoreButton = ButtonRow.setup(rootComponent, ButtonRow.getButtonId(ADD_LORE_ID), executeButtonsActive, button -> {
            ItemStack handItem = client.player.getInventory().getMainHandStack();
            Text text = this.messagePreviewLabel.text();

            DisplayBuilder builder = DisplayBuilder.of(handItem.isEmpty() ?
                    FzmmUtils.getItem(config.defaultItem()).getDefaultStack() : handItem
            ).addLore(text);

            FzmmUtils.giveItem(builder.get());
        });
        ButtonWidget setNameButton = ButtonRow.setup(rootComponent, ButtonRow.getButtonId(SET_NAME_ID), executeButtonsActive, button -> {
            ItemStack handItem = client.player.getInventory().getMainHandStack();
            Text text = this.messagePreviewLabel.text();

            DisplayBuilder builder = DisplayBuilder.of(handItem.isEmpty() ?
                    FzmmUtils.getItem(config.defaultItem()).getDefaultStack() : handItem
            ).setName(text);

            FzmmUtils.giveItem(builder.get());
        });
        ButtonWidget randomButton = ButtonRow.setup(rootComponent, ButtonRow.getButtonId(RANDOM_ID), executeButtonsActive, button -> selectedTab.setRandomValues());
        ButtonWidget copyButton = ButtonRow.setup(rootComponent, ButtonRow.getButtonId(COPY_ID), executeButtonsActive,
                button -> this.client.setScreen(new CopyTextScreen(this, this.messagePreviewLabel.text())));
        this.executeButtons = List.of(addLoreButton, setNameButton, randomButton, copyButton);

    }

    public void updateMessagePreview() {
        if (!this.initialized)
            return;
        String message = this.messageTextField.getText();
        if (message.length() < 2) {
            this.toggleExecuteButtons(false);
            this.messagePreviewLabel.text(Text.translatable("fzmm.gui.textFormat.error.messageLength")
                    .setStyle(Style.EMPTY.withColor(0x913144)));
        }
        this.toggleExecuteButtons(true);

        boolean obfuscated = (boolean) this.obfuscatedToggle.parsedValue();
        boolean bold = (boolean) this.boldToggle.parsedValue();
        boolean strikethrough = (boolean) this.strikethroughToggle.parsedValue();
        boolean underline = (boolean) this.underlineToggle.parsedValue();
        boolean italic = (boolean) this.italicToggle.parsedValue();

        TextFormatLogic logic = new TextFormatLogic(message, obfuscated, bold, strikethrough, underline, italic);
        Text messagePreview = selectedTab.getText(logic);
        this.messagePreviewLabel.text(messagePreview);
    }

    private void toggleExecuteButtons(boolean value) {
        for (var button : executeButtons)
            button.active = value;
    }
}
