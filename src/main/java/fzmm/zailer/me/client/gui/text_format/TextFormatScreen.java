package fzmm.zailer.me.client.gui.text_format;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.component.EBooleanButton;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.components.tabs.ITab;
import fzmm.zailer.me.client.gui.components.tabs.TabContainer;
import fzmm.zailer.me.client.gui.text_format.tabs.*;
import fzmm.zailer.me.client.gui.utils.CopyTextScreen;
import fzmm.zailer.me.client.logic.TextFormatLogic;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.ItemUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.util.FocusHandler;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class TextFormatScreen extends BaseFzmmScreen implements IMemento {
    public static final net.minecraft.network.chat.Component EMPTY_COLOR_TEXT = net.minecraft.network.chat.Component.translatable("fzmm.gui.textFormat.error.emptyColor").setStyle(Style.EMPTY.withColor(EStyles.TEXT_ERROR_COLOR.rgb()));
    private LabelComponent messagePreviewLabel;
    private TextBoxComponent messageTextField;
    private EBooleanButton boldToggle;
    private EBooleanButton italicToggle;
    private EBooleanButton obfuscatedToggle;
    private EBooleanButton strikethroughToggle;
    private EBooleanButton underlineToggle;
    private FlowLayout stylesLayout;
    private List<Button> executeButtons;
    private TabContainer tabContainer;
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
        this.tabContainer = rootComponent.childByIdOrThrow(TabContainer.class, "tabs");
        List<ITextFormatTab> tabs = List.of(new TextFormatSimpleTab(), new TextFormatGradientTab(),
                new TextFormatInterleavedColorsTab(), new TextFormatRainbowTab(), new TextFormatPlaceholderApiTab()
        );
        this.tabContainer.addParsedTabs(tabs)
                .onSelect(this::onSelectTab)
                .setupTabs(rootComponent, tabs.get(0).getId());
        for (var tab : tabs) {
            tab.componentsCallback(object -> this.updateMessagePreview());
        }
        this.tabContainer.selectTab();

        this.setupBottomButtons(rootComponent);
        this.initialized = true;
    }

    @Override
    protected void initFocus(FocusHandler focusHandler) {
        focusHandler.focus(this.messageTextField, UIComponent.FocusSource.MOUSE_CLICK);
    }

    private void onSelectTab(ITab tab) {
        for (var child : this.stylesLayout.children()) {
            if (child instanceof ButtonComponent buttonComponent) {
                buttonComponent.active(((ITextFormatTab) tab).hasStyles());
            }
        }
        this.updateMessagePreview();
    }

    private void setupBottomButtons(EFlowLayout rootComponent) {
        assert minecraft.player != null;
        FzmmConfig.TextFormat config = FzmmClient.CONFIG.textFormat;

        boolean executeButtonsActive = this.messageTextField.getValue().length() > 1;
        ButtonComponent addLoreButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "add-lore-button");
        addLoreButton.active(executeButtonsActive);
        addLoreButton.onPress(button -> {
            ItemStack handItem = ItemUtils.from(InteractionHand.MAIN_HAND);
            net.minecraft.network.chat.Component text = this.messagePreviewLabel.text();

            DisplayBuilder builder = DisplayBuilder.of(handItem.isEmpty() ?
                    ItemUtils.from(config.defaultItem()).getDefaultInstance() : handItem
            ).addLore(text);

            ItemUtils.give(builder.get());
        });
        ButtonComponent setNameButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "set-name-button");
        setNameButton.active(executeButtonsActive);
        setNameButton.onPress(button -> {
            ItemStack handItem = ItemUtils.from(InteractionHand.MAIN_HAND);
            net.minecraft.network.chat.Component text = this.messagePreviewLabel.text();

            DisplayBuilder builder = DisplayBuilder.of(handItem.isEmpty() ?
                    ItemUtils.from(config.defaultItem()).getDefaultInstance() : handItem
            ).setName(text.copy());

            ItemUtils.give(builder.get());
        });
        ButtonComponent randomButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "random-button");
        randomButton.active(executeButtonsActive);
        randomButton.onPress(button -> ((ITextFormatTab) this.tabContainer.selectedTab()).setRandomValues());

        ButtonComponent copyButton = rootComponent.childByIdOrThrow(ButtonComponent.class, "copy-button");
        copyButton.active(executeButtonsActive);
        copyButton.onPress(button -> this.setScreen(new CopyTextScreen(this, this.messagePreviewLabel.text())));
        this.executeButtons = List.of(addLoreButton, setNameButton, randomButton, copyButton);

    }

    public void updateMessagePreview() {
        if (!this.initialized) return;
        String message = this.messageTextField.getValue();
        if (message.length() < 2) {
            this.toggleExecuteButtons(false);
            this.messagePreviewLabel.text(net.minecraft.network.chat.Component.translatable("fzmm.gui.textFormat.error.messageLength")
                    .setStyle(Style.EMPTY.withColor(0x913144)));
        }
        this.toggleExecuteButtons(true);

        boolean obfuscated = this.obfuscatedToggle.enabled();
        boolean bold = this.boldToggle.enabled();
        boolean strikethrough = this.strikethroughToggle.enabled();
        boolean underline = this.underlineToggle.enabled();
        boolean italic = this.italicToggle.enabled();

        TextFormatLogic logic = new TextFormatLogic(message, obfuscated, bold, strikethrough, underline, italic);
        net.minecraft.network.chat.Component messagePreview = ((ITextFormatTab) this.tabContainer.selectedTab()).getText(logic);
        this.messagePreviewLabel.text(messagePreview);
    }

    private void toggleExecuteButtons(boolean value) {
        for (var button : this.executeButtons) {
            button.active = value;
        }
    }

    private EBooleanButton setupStyleButton(EFlowLayout rootComponent, String id) {
        EBooleanButton booleanButton = rootComponent.childByIdOrThrow(EBooleanButton.class, id);
        booleanButton.onPress(buttonComponent -> this.updateMessagePreview());
        booleanButton.enabled(false);
        return booleanButton;
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.messageTextField.getValue());
        output.writeBoolean(this.obfuscatedToggle.enabled());
        output.writeBoolean(this.boldToggle.enabled());
        output.writeBoolean(this.strikethroughToggle.enabled());
        output.writeBoolean(this.underlineToggle.enabled());
        output.writeBoolean(this.italicToggle.enabled());
        this.tabContainer.backup(output);
    }

    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.messageTextField.text((String) input.readObject());
        this.obfuscatedToggle.enabled(input.readBoolean());
        this.boldToggle.enabled(input.readBoolean());
        this.strikethroughToggle.enabled(input.readBoolean());
        this.underlineToggle.enabled(input.readBoolean());
        this.italicToggle.enabled(input.readBoolean());
        this.tabContainer.restore(input);
    }
}
