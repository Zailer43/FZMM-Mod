package fzmm.zailer.me.client.gui.textformat;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.ScreenTabRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.utils.CopyTextScreen;
import fzmm.zailer.me.client.gui.utils.components.BooleanButton;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
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

public class TextFormatScreen extends BaseFzmmScreen {
    public static final Text EMPTY_COLOR_TEXT = Text.translatable("fzmm.gui.textFormat.error.emptyColor").setStyle(Style.EMPTY.withColor(0xF2200D));
    private static final String MESSAGE_PREVIEW_ID = "message-preview";
    private static final String MESSAGE_ID = "message";
    private static final String BOLD_ID = "bold";
    private static final String ITALIC_ID = "italic";
    private static final String OBFUSCATED_ID = "obfuscated";
    private static final String STRIKETHROUGH_ID = "strikethrough";
    private static final String UNDERLINE_ID = "underline";
    private static final String STYLES_LAYOUT_ID = "styles-layout";
    private static final String ADD_LORE_ID = "add-lore";
    private static final String SET_NAME_ID = "set-name";
    private static final String COPY_ID = "copy";
    private static final String RANDOM_ID = "random";
    private static TextFormatTabs selectedTab = TextFormatTabs.SIMPLE;
    private LabelComponent messagePreviewLabel;
    private TextFieldWidget messageTextField;
    private BooleanButton boldToggle;
    private BooleanButton italicToggle;
    private BooleanButton obfuscatedToggle;
    private BooleanButton strikethroughToggle;
    private BooleanButton underlineToggle;
    private FlowLayout stylesLayout;
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

        this.messageTextField = TextBoxRow.setup(rootComponent, MESSAGE_ID, "Hello world", 4096, s -> this.updateMessagePreview());

        //styles
        this.stylesLayout = rootComponent.childById(FlowLayout.class, STYLES_LAYOUT_ID);
        checkNull(this.stylesLayout, "flow-layout", STYLES_LAYOUT_ID);

        this.boldToggle = this.setupStyleButton(rootComponent, BOLD_ID);
        this.italicToggle = this.setupStyleButton(rootComponent, ITALIC_ID);
        this.obfuscatedToggle = this.setupStyleButton(rootComponent, OBFUSCATED_ID);
        this.strikethroughToggle = this.setupStyleButton(rootComponent, STRIKETHROUGH_ID);
        this.underlineToggle = this.setupStyleButton(rootComponent, UNDERLINE_ID);
        //tabs
        this.setTabs(selectedTab);
        for (var tab : TextFormatTabs.values())
            this.getTab(tab, ITextFormatTab.class).componentsCallback(object -> this.updateMessagePreview());

        ScreenTabRow.setup(rootComponent, "tabs", selectedTab);
        for (var textFormatTab : TextFormatTabs.values()) {
            ITextFormatTab tab = this.getTab(textFormatTab, ITextFormatTab.class);
            tab.setupComponents(rootComponent);
            ButtonRow.setup(rootComponent, ScreenTabRow.getScreenTabButtonId(tab), !tab.getId().equals(selectedTab.getId()), button -> {
                selectedTab = this.selectScreenTab(rootComponent, tab, selectedTab);
                this.tabCallback(tab);
            });
        }
        this.selectScreenTab(rootComponent, selectedTab, selectedTab);

        this.setupBottomButtons(rootComponent);
        this.initialized = true;
        this.updateMessagePreview();
        this.tabCallback(this.getTab(selectedTab, ITextFormatTab.class));
    }

    private void tabCallback(ITextFormatTab tab) {
        this.updateMessagePreview();

        for (var child : this.stylesLayout.children()) {
            if (child instanceof ButtonComponent buttonComponent)
                buttonComponent.active = tab.hasStyles();
        }
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
        ButtonWidget randomButton = ButtonRow.setup(rootComponent, ButtonRow.getButtonId(RANDOM_ID), executeButtonsActive,
                button -> this.getTab(selectedTab, ITextFormatTab.class).setRandomValues());

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

        boolean obfuscated = this.obfuscatedToggle.enabled();
        boolean bold = this.boldToggle.enabled();
        boolean strikethrough = this.strikethroughToggle.enabled();
        boolean underline = this.underlineToggle.enabled();
        boolean italic = this.italicToggle.enabled();

        TextFormatLogic logic = new TextFormatLogic(message, obfuscated, bold, strikethrough, underline, italic);
        Text messagePreview = this.getTab(selectedTab, ITextFormatTab.class).getText(logic);
        this.messagePreviewLabel.text(messagePreview);
    }

    private void toggleExecuteButtons(boolean value) {
        for (var button : executeButtons)
            button.active = value;
    }

    private BooleanButton setupStyleButton(FlowLayout rootComponent, String id) {
        BooleanButton booleanButton = rootComponent.childById(BooleanButton.class, id);
        checkNull(booleanButton, "boolean-button", id);
        booleanButton.onPress(buttonComponent -> this.updateMessagePreview());
        booleanButton.enabled(false);
        return booleanButton;
    }
}
