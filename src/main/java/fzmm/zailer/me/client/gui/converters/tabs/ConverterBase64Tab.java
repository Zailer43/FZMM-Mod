package fzmm.zailer.me.client.gui.converters.tabs;

import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.gui.widget.TextFieldWidget;

public class ConverterBase64Tab implements IScreenTab {
    private static final String MESSAGE_ID = "message";
    private static final String COPY_DECODED_ID = "copyDecoded";
    private static final String COPY_ENCODED_ID = "copyEncoded";

    @Override
    public String getId() {
        return "base64";
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        TextFieldWidget messageField = TextBoxRow.setup(rootComponent, MESSAGE_ID, "", 5000);

        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(COPY_DECODED_ID), true, button ->
                FzmmUtils.decodeBase64(messageField.getText()).ifPresent(SnackBarManager::copyToClipboard)
        );

        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(COPY_ENCODED_ID), true, button ->
                FzmmUtils.encodeBase64(messageField.getText()).ifPresent(SnackBarManager::copyToClipboard)
        );
    }
}
