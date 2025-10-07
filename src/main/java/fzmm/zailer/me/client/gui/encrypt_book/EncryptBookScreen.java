package fzmm.zailer.me.client.gui.encrypt_book;


import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.SuggestionTextBox;
import fzmm.zailer.me.client.gui.components.containers.ConfirmOverlay;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.component.ELabelComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.encrypt_book.components.AddEncryptProfileOverlay;
import fzmm.zailer.me.client.gui.encrypt_book.components.DecryptorSaverOverlay;
import fzmm.zailer.me.client.logic.enycrpt_book.EncryptbookLogic;
import fzmm.zailer.me.client.logic.enycrpt_book.TranslationEncryptProfile;
import fzmm.zailer.me.client.logic.history.IMemento;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmWikiConstants;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.TextAreaComponent;
import io.wispforest.owo.ui.component.TextBoxComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.util.FocusHandler;
import net.minecraft.client.gui.screen.ConfirmLinkScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EncryptBookScreen extends BaseFzmmScreen implements IMemento {
    private TextAreaComponent messageTextArea;
    private TextBoxComponent paddingCharactersField;
    private TextBoxComponent authorField;
    private TextBoxComponent titleField;
    private FlowLayout decryptorProfileLayout;
    private LabelComponent decryptorStatus;
    @Nullable
    private TranslationEncryptProfile selectedProfile = null;
    private int selectedProfileIndex;

    public EncryptBookScreen(@Nullable Screen parent) {
        super("encrypt_book", "encryptbook", parent);
    }

    @Override
    protected void setup(EFlowLayout rootComponent) {
        assert this.client != null;
        assert this.client.player != null;

        FzmmConfig.Encryptbook config = FzmmClient.CONFIG.encryptbook;
        // message
        this.messageTextArea = rootComponent.childByIdOrThrow(TextAreaComponent.class, "message-text-area");
        this.messageTextArea.text(config.defaultBookMessage());

        // book options
        this.authorField = TextBoxRow.setup(rootComponent, "author", this.client.player.getName().getString(), 512);
        this.titleField = TextBoxRow.setup(rootComponent, "title", config.defaultBookTitle(), 256);

        // encryptbook options
        String configPadding = config.padding();
        this.paddingCharactersField = TextBoxRow.setup(rootComponent, "paddingCharacters", configPadding, 512);
        rootComponent.childByIdOrThrow(ButtonComponent.class, "add-profile-button").onPress(this::addProfileOverlay);
        if (this.paddingCharactersField instanceof SuggestionTextBox suggestionTextBox) {
            suggestionTextBox.setSuggestionProvider((context, builder) -> {
                String defaultValue = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_,.";
                builder.suggest(defaultValue);
                if (!defaultValue.equals(configPadding)) {
                    builder.suggest(configPadding);
                }
                return CompletableFuture.completedFuture(builder.build());
            });
        }

        this.decryptorStatus = rootComponent.childByIdOrThrow(ELabelComponent.class, "profile-status");
        rootComponent.childByIdOrThrow(ButtonComponent.class, "get-decryptor-button").onPress(buttonComponent -> this.decryptorSaverOverlay(this.selectedProfile));

        this.decryptorProfileLayout = rootComponent.childByIdOrThrow(EFlowLayout.class, "profile-list");
        this.updateDecryptorProfileList();
        this.selectProfile(0);

        // bottom buttons
        rootComponent.childByIdOrThrow(ButtonComponent.class, "give-button").onPress(buttonComponent -> this.giveBook(false));
        rootComponent.childByIdOrThrow(ButtonComponent.class, "add-page-button").onPress(buttonComponent -> this.giveBook(true));

        // other
        rootComponent.childByIdOrThrow(ButtonComponent.class, "faq-button").onPress(this::faqExecute);
    }

    @Override
    protected void initFocus(FocusHandler focusHandler) {
        focusHandler.focus(this.messageTextArea, Component.FocusSource.MOUSE_CLICK);
    }

    private void updateDecryptorProfileList() {
        List<TranslationEncryptProfile> decryptorProfiles = getProfiles();

        List<Component> componentList = new ArrayList<>();
        for (int i = 0; i < decryptorProfiles.size(); i++) {
            TranslationEncryptProfile profile = decryptorProfiles.get(i);
            int finalI = i;
            EFlowLayout component = this.getModel().expandTemplate(EFlowLayout.class, "profile-option", Map.of()).configure(layout -> {
                ELabelComponent label = layout.childByIdOrThrow(ELabelComponent.class, "label");

                label.text(Text.translatable("fzmm.gui.encryptbook.label.profile",
                        profile.translationKey(),
                        profile.length(),
                        profile.isAsymmetric(),
                        profile.isOldAlgorithm()
                ));

                layout.mouseDown().subscribe((mouseX, mouseY, button) -> this.profileSelect(layout, profile, finalI));

                ButtonComponent removeButton = layout.childByIdOrThrow(ButtonComponent.class, "remove-button");

                //noinspection CodeBlock2Expr
                removeButton.onPress(button -> {
                    this.addOverlay(new ConfirmOverlay(Text.translatable("fzmm.gui.encryptbook.label.removeDecryptor"), aBoolean -> {
                        if (aBoolean) {
                            FzmmClient.CONFIG.encryptbook.profiles().remove(profile.toModel());
                            FzmmClient.CONFIG.save();
                            layout.remove();

                            this.updateDecryptorStatus(this.selectedProfile);
                        }
                    }));
                });
            });

            componentList.add(component);
        }

        this.decryptorProfileLayout.clearChildren();
        this.decryptorProfileLayout.children(componentList);
    }

    private boolean profileSelect(FlowLayout profileLayout, TranslationEncryptProfile profile, int index) {
        this.selectedProfile = profile;
        this.selectedProfileIndex = index;

        for (var child : this.decryptorProfileLayout.children()) {
            if (!(child instanceof FlowLayout childLayout)) {
                continue;
            }

            Surface surface = Surface.flat(childLayout == profileLayout ? EStyles.SELECTED_COLOR : EStyles.UNSELECTED_COLOR);
            childLayout.surface(surface);
        }

        this.updateDecryptorStatus(profile);
        this.messageTextArea.setMaxLength(profile.length());

        return true;
    }

    public void addProfileOverlay(ButtonWidget buttonWidget) {
        this.addOverlay(new AddEncryptProfileOverlay(profile -> {
            FzmmClient.CONFIG.encryptbook.profiles().add(profile.toModel());
            FzmmClient.CONFIG.save();
            this.updateDecryptorProfileList();
            this.selectProfile(this.decryptorProfileLayout.children().size() - 1);

            this.decryptorSaverOverlay(profile);
        }));
    }

    private void decryptorSaverOverlay(TranslationEncryptProfile profile) {
        this.addOverlay(new DecryptorSaverOverlay(profile));
    }

    public void selectProfile(int index) {
        List<Component> profileLayout = this.decryptorProfileLayout.children();
        if (profileLayout.isEmpty()) {
            return;
        }
        int selectedProfileIndex = index < profileLayout.size() ? index : 0;
        profileLayout.get(selectedProfileIndex).onMouseDown(0, 0, 0);
    }

    public void updateDecryptorStatus(@Nullable TranslationEncryptProfile profile) {
        Text result;
        boolean isValid = false;

        String translationValue = "fzmm.gui.encryptbook.label.profile.";

        if (profile != null && I18n.hasTranslation(profile.translationKey())) {
            String decryptString = Text.translatable(profile.translationKey()).getString();

            isValid = decryptString.equals(profile.decryptorValue());
            String status = isValid ? "loaded" : "outdated";
            result = Text.translatable(translationValue + status);
        } else {
            result = Text.translatable(translationValue + "notFound");
        }

        result = result.copy().setStyle(Style.EMPTY
                .withColor((isValid ? EStyles.TEXT_SUCCESS_COLOR : EStyles.TEXT_ERROR_COLOR).rgb()));

        this.decryptorStatus.text(result);
    }

    private void giveBook(boolean isAddPage) {
        if (this.selectedProfile == null) {
            return;
        }
        FzmmConfig.Encryptbook config = FzmmClient.CONFIG.encryptbook;

        String message = this.messageTextArea.getText();
        if (message.isEmpty()) {
            message = config.defaultBookMessage();
        }

        String paddingChars = this.paddingCharactersField.getText();
        if (paddingChars.isEmpty()) {
            paddingChars = " ";
        }

        String author = this.authorField.getText();
        String title = this.titleField.getText();

        EncryptbookLogic.give(message, author, paddingChars, title, this.selectedProfile, isAddPage);
    }

    private void faqExecute(ButtonWidget buttonWidget) {
        assert this.client != null;

        ConfirmLinkScreen.open(client.currentScreen, FzmmWikiConstants.ENCRYPT_BOOK_WIKI_LINK);
    }

    public static List<TranslationEncryptProfile> getProfiles() {
        return TranslationEncryptProfile.of(FzmmClient.CONFIG.encryptbook.profiles());
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.messageTextArea.getText());
        output.writeObject(this.authorField.getText());
        output.writeObject(this.titleField.getText());
        output.writeObject(this.paddingCharactersField.getText());
        output.writeInt(this.selectedProfileIndex);
    }

    @Override
    public void restore(ObjectInputStream input) throws IOException, ClassNotFoundException {
        this.messageTextArea.text((String) input.readObject());
        this.authorField.text((String) input.readObject());
        this.titleField.text((String) input.readObject());
        this.paddingCharactersField.text((String) input.readObject());
        this.selectProfile(input.readInt());
    }
}