package fzmm.zailer.me.client.gui.converters.tabs;

import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.ConfigTextBoxRow;
import fzmm.zailer.me.client.gui.components.tabs.ITab;
import fzmm.zailer.me.client.gui.converters.ConvertersScreen;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.ButtonComponent;

import java.util.UUID;

public class ConverterUuidToArrayTab implements ITab {

    @Override
    public String getId() {
        return "uuidToArray";
    }

    @Override
    public String getTranslationKey() {
        return ConvertersScreen.BUTTON_TRANSLATION_KEY + this.getId();
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        ConfigTextBox uuidField = ConfigTextBoxRow.setup(rootComponent, "uuidField", "");
        uuidField.applyPredicate(s -> {
            try {
                UUID ignored = UUID.fromString(s);
                return true;
            } catch (Exception ignored) {
                return false;
            }
        });

        rootComponent.childByIdOrThrow(ButtonComponent.class, "uuidToArray.random-button")
                .onPress(button -> uuidField.text(UUID.randomUUID().toString()));
        rootComponent.childByIdOrThrow(ButtonComponent.class, "uuidToArray.copy-button").onPress(button -> {
            if (!uuidField.isValid())
                return;

            String stringOfUuidArray = toString(UUIDtoArray(UUID.fromString(uuidField.getValue())));
            SnackBarManager.copyToClipboard(stringOfUuidArray);
        });
    }

    public static int[] UUIDtoArray(UUID uuid) {
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        int[] intArray = new int[4];

        intArray[0] = (int) (msb >> 32);
        intArray[1] = (int) (msb);
        intArray[2] = (int) (lsb >> 32);
        intArray[3] = (int) (lsb);

        return intArray;
    }

    public static String toString(int[] uuid) {
        return String.format("[I;%s,%s,%s,%s]", uuid[0], uuid[1], uuid[2], uuid[3]);
    }
}
