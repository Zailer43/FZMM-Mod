package fzmm.zailer.me.client.gui.converters.tabs;

import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.components.tabs.ITab;
import fzmm.zailer.me.client.gui.converters.ConvertersScreen;
import fzmm.zailer.me.utils.SnackBarManager;
import fzmm.zailer.me.utils.TextUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class ConverterBase64Tab implements ITab {

    @Override
    public String getId() {
        return "base64";
    }

    @Override
    public String getTranslationKey() {
        return ConvertersScreen.BUTTON_TRANSLATION_KEY + this.getId();
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        TextFieldWidget messageField = TextBoxRow.setup(rootComponent, "message", "", 5000);

        rootComponent.childByIdOrThrow(ButtonComponent.class, "copyDecoded-button").onPress(buttonComponent ->
                TextUtils.decodeBase64(messageField.getText()).ifPresent(SnackBarManager::copyToClipboard)
        );

        rootComponent.childByIdOrThrow(ButtonComponent.class, "copyEncoded-button").onPress(buttonComponent ->
                SnackBarManager.copyToClipboard(TextUtils.encodeBase64(messageField.getText()))
        );
    }
}
