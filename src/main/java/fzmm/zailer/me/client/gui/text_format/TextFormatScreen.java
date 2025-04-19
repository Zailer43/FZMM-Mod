package fzmm.zailer.me.client.gui.text_format;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.component.EBooleanButton;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.ScreenTabRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.text_format.tabs.ITextFormatTab;
import fzmm.zailer.me.client.gui.text_format.tabs.TextFormatTabs;
import fzmm.zailer.me.client.gui.utils.CopyTextScreen;
import fzmm.zailer.me.client.gui.utils.memento.IMementoObject;
import fzmm.zailer.me.client.gui.utils.memento.IMementoScreen;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.ItemUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.util.FocusHandler;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class TextFormatScreen extends BaseFzmmScreen implements IMementoScreen {
    public static final Text EMPTY_COLOR_TEXT = Text.translatable("fzmm.gui.textFormat.error.emptyColor").setStyle(Style.EMPTY.withColor(EStyles.TEXT_ERROR_COLOR.rgb()));
    private static TextFormatMemento memento = null;
    private static TextFormatTabs selectedTab = TextFormatTabs.SIMPLE;
    private LabelComponent messagePreviewLabel;
    private TextBoxComponent messageTextField;
    private EBooleanButton boldToggle;
    private EBooleanButton italicToggle;
    private EBooleanButton obfuscatedToggle;
    private EBooleanButton strikethroughToggle;
    private EBooleanButton underlineToggle;
    private FlowLayout stylesLayout;
    private List<ButtonWidget> executeButtons;
    private boolean initialized;

    public TextFormatScreen(@Nullable Screen parent) {
        super("text_format", "textFormat", parent);
        this.initialized = false;
    }

    @Override
    protected void setup(EFlowLayout rootComponent) {
        this.messagePreviewLabel = rootComponent.childByIdOrThrow(LabelComponent.class, "message-preview");
        this.messageTextField = TextBoxRow.setup(rootComponent, "message", "Hello world", 4096, s -> this.updateMessagePreview());

        //styles
        this.stylesLayout = rootComponent.childByIdOrThrow(FlowLayout.class, "styles-layout");

        this.boldToggle = this.setupStyleButton(rootComponent, "bold");
        this.italicToggle = this.setupStyleButton(rootComponent, "italic");
        this.obfuscatedToggle = this.setupStyleButton(rootComponent, "obfuscated");
        this.strikethroughToggle = this.setupStyleButton(rootComponent, "strikethrough");
        this.underlineToggle = this.setupStyleButton(rootComponent, "underline");
        //tabs
        this.setTabs(selectedTab);
        for (var tab : TextFormatTabs.values())
            this.getTab(tab, ITextFormatTab.class).componentsCallback(object -> this.updateMessagePreview());

        ScreenTabRow.setup(rootComponent, "tabs", selectedTab);
        for (var textFormatTab : TextFormatTabs.values()) {
            ITextFormatTab tab = this.getTab(textFormatTab, ITextFormatTab.class);
            tab.setupComponents(rootComponent);
            ButtonComponent button = rootComponent.childByIdOrThrow(ButtonComponent.class, ScreenTabRow.getScreenTabButtonId(tab));
            button.active(!tab.getId().equals(selectedTab.getId()));
            button.onPress(buttonComponent -> {
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

    @Override
    protected void initFocus(FocusHandler focusHandler) {
        focusHandler.focus(this.messageTextField, Component.FocusSource.MOUSE_CLICK);
    }

    private void tabCallback(ITextFormatTab tab) {
        this.updateMessagePreview();

        for (var child : this.stylesLayout.children()) {
            if (child instanceof ButtonComponent buttonComponent)
                buttonComponent.active = tab.hasStyles();
        }
    }

    private void setupBottomButtons(EFlowLayout rootComponent) {
        assert this.client != null;
        assert client.player != null;
        FzmmConfig.TextFormat config = FzmmClient.CONFIG.textFormat;

        boolean executeButtonsActive = this.messageTextField.getText().length() > 1;
        ButtonComponent addLoreButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "add-lore-button");
        addLoreButton.active(executeButtonsActive);
        addLoreButton.onPress(button -> {
            ItemStack handItem = ItemUtils.from(Hand.MAIN_HAND);
            Text text = this.messagePreviewLabel.text();

            DisplayBuilder builder = DisplayBuilder.of(handItem.isEmpty() ?
                    ItemUtils.from(config.defaultItem()).getDefaultStack() : handItem
            ).addLore(text);

            ItemUtils.give(builder.get());
        });
        ButtonComponent setNameButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "set-name-button");
        setNameButton.active(executeButtonsActive);
        setNameButton.onPress(button -> {
            ItemStack handItem = ItemUtils.from(Hand.MAIN_HAND);
            Text text = this.messagePreviewLabel.text();

            DisplayBuilder builder = DisplayBuilder.of(handItem.isEmpty() ?
                    ItemUtils.from(config.defaultItem()).getDefaultStack() : handItem
            ).setName(text.copy());

            ItemUtils.give(builder.get());
        });
        ButtonComponent randomButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "random-button");
        randomButton.active(executeButtonsActive);
        randomButton.onPress(button -> this.getTab(selectedTab, ITextFormatTab.class).setRandomValues());

        ButtonComponent copyButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "copy-button");
        copyButton.active(executeButtonsActive);
        copyButton.onPress(button -> this.setScreen(new CopyTextScreen(this, this.messagePreviewLabel.text())));
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

    private EBooleanButton setupStyleButton(EFlowLayout rootComponent, String id) {
        EBooleanButton booleanButton = rootComponent.childByIdOrThrow(EBooleanButton.class, id);
        booleanButton.onPress(buttonComponent -> this.updateMessagePreview());
        booleanButton.enabled(false);
        return booleanButton;
    }

    @Override
    public void setMemento(IMementoObject memento) {
        TextFormatScreen.memento = (TextFormatMemento) memento;
    }

    @Override
    public Optional<IMementoObject> getMemento() {
        return Optional.ofNullable(memento);
    }

    @Override
    public TextFormatMemento createMemento() {
        return new TextFormatMemento(
                this.messageTextField.getText(),
                this.obfuscatedToggle.enabled(),
                this.boldToggle.enabled(),
                this.strikethroughToggle.enabled(),
                this.underlineToggle.enabled(),
                this.italicToggle.enabled(),
                this.createMementoTabs()
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        TextFormatMemento memento = (TextFormatMemento) mementoObject;
        this.messageTextField.text(memento.message);
        this.obfuscatedToggle.enabled(memento.obfuscated);
        this.boldToggle.enabled(memento.bold);
        this.strikethroughToggle.enabled(memento.strikethrough);
        this.underlineToggle.enabled(memento.underline);
        this.italicToggle.enabled(memento.italic);
        this.restoreMementoTabs(memento.mementoTabHashMap);
    }

    public record TextFormatMemento(String message, boolean obfuscated, boolean bold,
                                    boolean strikethrough, boolean underline, boolean italic,
                                    HashMap<String, IMementoObject> mementoTabHashMap) implements IMementoObject {
    }
}
