package fzmm.zailer.me.client.gui.converters.tabs;

import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.ui.component.ButtonComponent;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class ConverterBase64Tab implements IScreenTab {

    @Override
    public String getId() {
        return "base64";
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        TextFieldWidget messageField = TextBoxRow.setup(rootComponent, "message", "", 5000);

        rootComponent.childByIdOrThrow(ButtonComponent.class, "copyDecoded-button").onPress(buttonComponent ->
                FzmmUtils.decodeBase64(messageField.getText()).ifPresent(SnackBarManager::copyToClipboard)
        );

        rootComponent.childByIdOrThrow(ButtonComponent.class, "copyEncoded-button").onPress(buttonComponent ->
                FzmmUtils.encodeBase64(messageField.getText()).ifPresent(SnackBarManager::copyToClipboard)
        );
    }
}
