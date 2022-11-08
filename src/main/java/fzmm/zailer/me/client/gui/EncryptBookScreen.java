package fzmm.zailer.me.client.gui;


import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.logic.EncryptbookLogic;
import fzmm.zailer.me.config.FzmmConfig;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.util.Util;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("UnstableApiUsage")
public class EncryptBookScreen extends BaseFzmmScreen {
    private static final String ENCRYPTBOOK_FAQ_LINK = "https://github.com/Zailer43/FZMM-Mod/wiki/FAQ-Encryptbook";
    private static final String MESSAGE_ID = "message";
    private static final String SEED_ID = "seed";
    private static final String PADDING_CHARACTERS_ID = "paddingCharacters";
    private static final String AUTHOR_ID = "author";
    private static final String TITLE_ID = "title";
    private static final String MAX_MESSAGE_LENGTH_ID = "maxMessageLength";
    private static final String GIVE_ID = "give";
    private static final String GET_DECODER_ID = "get-decoder";
    private static final String FAQ_ID = "faq";
    private static int seed = 0;
    private TextFieldWidget messageField;
    private TextFieldWidget paddingCharactersField;
    private TextFieldWidget authorField;
    private TextFieldWidget titleField;
    private SliderWidget maxMessageLengthField;

    public EncryptBookScreen(@Nullable Screen parent) {
        super("encrypt_book", "encryptbook", parent);
    }

    @Override
    protected void tryAddComponentList(FlowLayout rootComponent) {
        this.tryAddComponentList(rootComponent, "encryptBook-options-list",
                this.newTextFieldRow(MESSAGE_ID),
                this.newNumberRow(SEED_ID),
                this.newTextFieldRow(PADDING_CHARACTERS_ID),
                this.newTextFieldRow(AUTHOR_ID),
                this.newTextFieldRow(TITLE_ID),
                this.newSliderRow(MAX_MESSAGE_LENGTH_ID, 0)
        );
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        //general
        FzmmConfig.Encryptbook config = FzmmClient.CONFIG.encryptbook;
        this.messageField = this.setupTextField(rootComponent, MESSAGE_ID, config.defaultBookMessage());
        ConfigTextBox seedField = this.setupNumberField(rootComponent, SEED_ID, 0, Integer.class, s -> {
            try {
                seed = Integer.parseInt(s);
            } catch (NumberFormatException ignored) {
            }
        });

        seedField.setText(String.valueOf(seed));
        seedField.setCursor(0);
        this.paddingCharactersField = this.setupTextField(rootComponent, PADDING_CHARACTERS_ID, config.padding());
        this.paddingCharactersField.setMaxLength(512);
        assert MinecraftClient.getInstance().player != null;
        this.authorField = this.setupTextField(rootComponent, AUTHOR_ID, MinecraftClient.getInstance().player.getName().getString());
        this.titleField = this.setupTextField(rootComponent, TITLE_ID, config.defaultBookTitle());
        this.maxMessageLengthField = this.setupSlider(rootComponent, MAX_MESSAGE_LENGTH_ID, config.maxMessageLength(), 1, 512, Integer.class,
                aDouble -> this.messageField.setMaxLength((aDouble.intValue()))
        );
        //bottom buttons
        this.setupButton(rootComponent, this.getButtonId(GIVE_ID), true, this::giveBook);
        this.setupButton(rootComponent, this.getButtonId(GET_DECODER_ID), true, this::getDecoder);
        //other
        this.setupButton(rootComponent, this.getButtonId(FAQ_ID), true, this::faqExecute);
    }

    private void giveBook(ButtonWidget buttonWidget) {
        FzmmConfig.Encryptbook config = FzmmClient.CONFIG.encryptbook;

        String message = this.messageField.getText();
        if (message.isEmpty())
            message = config.defaultBookMessage();

        String paddingChars = this.paddingCharactersField.getText();
        if (paddingChars.isEmpty())
            paddingChars = config.padding();

        String author = this.authorField.getText();
        String title = this.titleField.getText();
        int maxMsgLength = (int) this.maxMessageLengthField.parsedValue();

        EncryptbookLogic.give(seed, message, author, paddingChars, maxMsgLength, title);
    }

    private void getDecoder(ButtonWidget buttonWidget) {
        int maxMsgLength = (int) this.maxMessageLengthField.parsedValue();
        EncryptbookLogic.showDecryptorInChat(seed, maxMsgLength);
    }

    private void faqExecute(ButtonWidget buttonWidget) {
        assert this.client != null;

        this.client.setScreen(new ConfirmLinkScreen(bool -> {
            if (bool)
                Util.getOperatingSystem().open(ENCRYPTBOOK_FAQ_LINK);

            this.client.setScreen(this);
        }, ENCRYPTBOOK_FAQ_LINK, true));
    }
}