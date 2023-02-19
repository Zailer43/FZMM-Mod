package fzmm.zailer.me.client.gui;


import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.SliderWidget;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.NumberRow;
import fzmm.zailer.me.client.gui.components.row.SliderRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.utils.IMementoObject;
import fzmm.zailer.me.client.gui.utils.IMementoScreen;
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

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public class EncryptBookScreen extends BaseFzmmScreen implements IMementoScreen {
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
    private static EncryptBookMemento memento = null;
    private ConfigTextBox seedField;
    private TextFieldWidget messageField;
    private TextFieldWidget paddingCharactersField;
    private TextFieldWidget authorField;
    private TextFieldWidget titleField;
    private SliderWidget maxMessageLengthField;

    public EncryptBookScreen(@Nullable Screen parent) {
        super("encrypt_book", "encryptbook", parent);
    }

    @Override
    protected void setupButtonsCallbacks(FlowLayout rootComponent) {
        //general
        FzmmConfig.Encryptbook config = FzmmClient.CONFIG.encryptbook;
        this.messageField = TextBoxRow.setup(rootComponent, MESSAGE_ID, config.defaultBookMessage(), config.maxMessageLength());
        this.seedField = NumberRow.setup(rootComponent, SEED_ID, 0, Integer.class);
        this.paddingCharactersField = TextBoxRow.setup(rootComponent, PADDING_CHARACTERS_ID, config.padding(), 512);
        assert MinecraftClient.getInstance().player != null;
        this.authorField = TextBoxRow.setup(rootComponent, AUTHOR_ID, MinecraftClient.getInstance().player.getName().getString(), 512);
        this.titleField = TextBoxRow.setup(rootComponent, TITLE_ID, config.defaultBookTitle(), 512);
        this.maxMessageLengthField = SliderRow.setup(rootComponent, MAX_MESSAGE_LENGTH_ID, config.maxMessageLength(),
                1, 512, Integer.class, 0,
                aDouble -> this.messageField.setMaxLength(aDouble.intValue())
        );
        //bottom buttons
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(GIVE_ID), true, this::giveBook);
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(GET_DECODER_ID), true, this::getDecoder);
        //other
        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(FAQ_ID), true, this::faqExecute);
    }

    private void giveBook(ButtonWidget buttonWidget) {
        FzmmConfig.Encryptbook config = FzmmClient.CONFIG.encryptbook;

        String message = this.messageField.getText();
        if (message.isEmpty())
            message = config.defaultBookMessage();

        String paddingChars = this.paddingCharactersField.getText();
        if (paddingChars.isEmpty())
            paddingChars = config.padding();

        int seed = (int) this.seedField.parsedValue();
        String author = this.authorField.getText();
        String title = this.titleField.getText();
        int maxMsgLength = (int) this.maxMessageLengthField.parsedValue();

        EncryptbookLogic.give(seed, message, author, paddingChars, maxMsgLength, title);
    }

    private void getDecoder(ButtonWidget buttonWidget) {
        int maxMsgLength = (int) this.maxMessageLengthField.parsedValue();
        int seed = (int) this.seedField.parsedValue();
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

    @Override
    public void setMemento(IMementoObject memento) {
        EncryptBookScreen.memento = (EncryptBookMemento) memento;
    }

    @Override
    public Optional<IMementoObject> getMemento() {
        return Optional.ofNullable(memento);
    }

    @Override
    public IMementoObject createMemento() {
        return new EncryptBookMemento((int) this.seedField.parsedValue(),
                this.messageField.getText(),
                this.authorField.getText(),
                this.paddingCharactersField.getText(),
                (int) this.maxMessageLengthField.parsedValue(),
                this.titleField.getText()
        );
    }

    @Override
    public void restoreMemento(IMementoObject mementoObject) {
        EncryptBookMemento memento = (EncryptBookMemento) mementoObject;
        this.seedField.setText(String.valueOf(memento.seed));
        this.seedField.setCursor(0);
        this.messageField.setText(memento.message);
        this.messageField.setCursor(0);
        this.authorField.setText(memento.author);
        this.authorField.setCursor(0);
        this.paddingCharactersField.setText(memento.paddingCharacters);
        this.paddingCharactersField.setCursor(0);
        this.maxMessageLengthField.setFromDiscreteValue(memento.maxMessageLength);
        this.titleField.setText(memento.title);
        this.titleField.setCursor(0);
    }

    private record EncryptBookMemento(int seed, String message, String author, String paddingCharacters,
                                      int maxMessageLength, String title) implements IMementoObject {
    }
}