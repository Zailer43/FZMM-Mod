package fzmm.zailer.me.client.gui.converters.tabs;

import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.TextBoxRow;
import io.wispforest.owo.ui.container.FlowLayout;
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
    public void setupComponents(FlowLayout rootComponent) {
        Keyboard keyboard = MinecraftClient.getInstance().keyboard;
        TextFieldWidget messageField = TextBoxRow.setup(rootComponent, MESSAGE_ID, "", 5000);

        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(COPY_DECODED_ID), true, button -> {
            try {
                String decodedMessage = decode(messageField.getText());
                keyboard.setClipboard(decodedMessage);
            } catch (Exception ignored) {
            }
        });

        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(COPY_ENCODED_ID), true, button -> {
            try {
                String encodedMessage = encode(messageField.getText());
                keyboard.setClipboard(encodedMessage);
            } catch (Exception ignored) {
            }
        });
    }

    public static String decode(String base64) throws IllegalArgumentException{
        byte[] decodedValue = Base64.getDecoder().decode(base64);
        return new String(decodedValue, StandardCharsets.UTF_8);
    }

    public static String encode(String message) throws IllegalArgumentException{
        byte[] messageByte = message.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(messageByte);
    }
}
