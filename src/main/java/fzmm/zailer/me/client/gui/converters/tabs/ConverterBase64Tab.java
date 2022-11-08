package fzmm.zailer.me.client.gui.converters.tabs;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.IScreenTab;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ConverterBase64Tab implements IScreenTab {
    private static final String MESSAGE_ID = "message";
    private static final String COPY_DECODED_ID = "copyDecoded";
    private static final String COPY_ENCODED_ID = "copyEncoded";

    @Override
    public String getId() {
        return "base64";
    }

    @Override
    public Component[] getComponents(BaseFzmmScreen parent) {
        return new Component[]{
                parent.newTextFieldRow(MESSAGE_ID),
                parent.newButtonRow(COPY_DECODED_ID),
                parent.newButtonRow(COPY_ENCODED_ID)
        };
    }

    @Override
    public void setupComponents(BaseFzmmScreen parent, FlowLayout rootComponent) {
        Keyboard keyboard = MinecraftClient.getInstance().keyboard;
        TextFieldWidget messageField = parent.setupTextField(rootComponent, MESSAGE_ID, "");
        messageField.setMaxLength(10000);

        parent.setupButton(rootComponent, parent.getButtonId(COPY_DECODED_ID), true, button -> {
            try {
                byte[] decodedValue = Base64.getDecoder().decode(messageField.getText());
                String decodedMessage = new String(decodedValue, StandardCharsets.UTF_8);
                keyboard.setClipboard(decodedMessage);
            } catch (Exception ignored) {
            }
        });

        parent.setupButton(rootComponent, parent.getButtonId(COPY_ENCODED_ID), true, button -> {
            try {
                byte[] messageByte = messageField.getText().getBytes(StandardCharsets.UTF_8);
                String encodedMessage = Base64.getEncoder().encodeToString(messageByte);
                keyboard.setClipboard(encodedMessage);
            } catch (Exception ignored) {
            }
        });
    }
}
