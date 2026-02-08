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
import io.wispforest.owo.ui.core.Surface;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.util.FocusHandler;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.MouseButtonInfo;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.component.WrittenBookContent;
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
        assert this.minecraft.player != null;

        FzmmConfig.Encryptbook config = FzmmClient.CONFIG.encryptbook;
        // message
        this.messageTextArea = rootComponent.childByIdOrThrow(TextAreaComponent.class, "message-text-area");
        this.messageTextArea.text(config.defaultBookMessage());

        // book options
        this.authorField = TextBoxRow.setup(rootComponent, "author", this.minecraft.player.getName().getString(), 512);
        this.titleField = TextBoxRow.setup(rootComponent, "title", config.defaultBookTitle(), WrittenBookContent.TITLE_MAX_LENGTH);

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
        focusHandler.focus(this.messageTextArea, UIComponent.FocusSource.MOUSE_CLICK);
    }

    private void updateDecryptorProfileList() {
        List<TranslationEncryptProfile> decryptorProfiles = getProfiles();

        List<UIComponent> componentList = new ArrayList<>();
        for (int i = 0; i < decryptorProfiles.size(); i++) {
            TranslationEncryptProfile profile = decryptorProfiles.get(i);
            int finalI = i;
            EFlowLayout component = this.getModel().expandTemplate(EFlowLayout.class, "profile-option", Map.of()).configure(layout -> {
                ELabelComponent label = layout.childByIdOrThrow(ELabelComponent.class, "label");

                label.text(net.minecraft.network.chat.Component.translatable("fzmm.gui.encryptbook.label.profile",
                        profile.translationKey(),
                        profile.length(),
                        profile.isAsymmetric(),
                        profile.isOldAlgorithm()
                ));

                layout.mouseDown().subscribe((input, doubled) -> this.profileSelect(layout, profile, finalI));

                ButtonComponent removeButton = layout.childByIdOrThrow(ButtonComponent.class, "remove-button");

                //noinspection CodeBlock2Expr
                removeButton.onPress(button -> {
                    this.addOverlay(new ConfirmOverlay(net.minecraft.network.chat.Component.translatable("fzmm.gui.encryptbook.label.removeDecryptor"), aBoolean -> {
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
            if (!(child instanceof FlowLayout childLayout)) continue;

            Surface surface = Surface.flat(childLayout == profileLayout ? EStyles.SELECTED_COLOR : EStyles.UNSELECTED_COLOR);
            childLayout.surface(surface);
        }

        this.updateDecryptorStatus(profile);
        this.messageTextArea.setCharacterLimit(profile.length());

        return true;
    }

    public void addProfileOverlay(Button buttonWidget) {
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
        List<UIComponent> profileLayout = this.decryptorProfileLayout.children();
        if (profileLayout.isEmpty()) return;
        int selectedProfileIndex = index < profileLayout.size() ? index : 0;
        profileLayout.get(selectedProfileIndex).onMouseDown(new MouseButtonEvent(0, 0, new MouseButtonInfo(0, 0)), false);
    }

    public void updateDecryptorStatus(@Nullable TranslationEncryptProfile profile) {
        net.minecraft.network.chat.Component result;
        boolean isValid = false;

        String translationValue = "fzmm.gui.encryptbook.label.profile.";

        if (profile != null && I18n.exists(profile.translationKey())) {
            String decryptString = net.minecraft.network.chat.Component.translatable(profile.translationKey()).getString();

            isValid = decryptString.equals(profile.decryptorValue());
            String status = isValid ? "loaded" : "outdated";
            result = net.minecraft.network.chat.Component.translatable(translationValue + status);
        } else {
            result = net.minecraft.network.chat.Component.translatable(translationValue + "notFound");
        }

        result = result.copy().setStyle(Style.EMPTY
                .withColor((isValid ? EStyles.TEXT_SUCCESS_COLOR : EStyles.TEXT_ERROR_COLOR).rgb()));

        this.decryptorStatus.text(result);
    }

    private void giveBook(boolean isAddPage) {
        if (this.selectedProfile == null) return;
        FzmmConfig.Encryptbook config = FzmmClient.CONFIG.encryptbook;

        String message = this.messageTextArea.getValue();
        if (message.isEmpty()) {
            message = config.defaultBookMessage();
        }

        String paddingChars = this.paddingCharactersField.getValue();
        if (paddingChars.isEmpty()) {
            paddingChars = " ";
        }

        String author = this.authorField.getValue();
        String title = this.titleField.getValue();

        EncryptbookLogic.give(message, author, paddingChars, title, this.selectedProfile, isAddPage);
    }

    private void faqExecute(Button buttonWidget) {
        assert this.minecraft.screen != null;
        ConfirmLinkScreen.confirmLinkNow(this.minecraft.screen, FzmmWikiConstants.ENCRYPT_BOOK_WIKI_LINK, true);
    }

    public static List<TranslationEncryptProfile> getProfiles() {
        return TranslationEncryptProfile.of(FzmmClient.CONFIG.encryptbook.profiles());
    }

    @Override
    public void backup(ObjectOutputStream output) throws IOException {
        output.writeObject(this.messageTextArea.getValue());
        output.writeObject(this.authorField.getValue());
        output.writeObject(this.titleField.getValue());
        output.writeObject(this.paddingCharactersField.getValue());
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