package fzmm.zailer.me.client.gui.converters.tabs;

import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.row.NumberRow;
import fzmm.zailer.me.client.gui.components.tabs.ITab;
import fzmm.zailer.me.client.gui.converters.ConvertersScreen;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.component.ButtonComponent;
import net.minecraft.util.RandomSource;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class ConverterArrayToUuidTab implements ITab {
    private static final int ARRAY_SIZE = 4;
    private ConfigTextBox[] textBoxArray;

    @Override
    public String getId() {
        return "arrayToUuid";
    }

    @Override
    public String getTranslationKey() {
        return ConvertersScreen.BUTTON_TRANSLATION_KEY + this.getId();
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
        this.textBoxArray = new ConfigTextBox[ARRAY_SIZE];

        for (int i = 0; i != ARRAY_SIZE; i++)
            this.textBoxArray[i] = NumberRow.setup(rootComponent, this.getArrayId(i), 0, Integer.class);

        rootComponent.childByIdOrThrow(ButtonComponent.class, "arrayToUuid.random-button").onPress(button -> {
            RandomSource random = RandomSource.create();
            for (var element : this.textBoxArray) {
                element.text(String.valueOf(random.nextInt()));
            }
        });

        rootComponent.childByIdOrThrow(ButtonComponent.class, "arrayToUuid.copy-button").onPress(button -> {
            int[] intArray = new int[ARRAY_SIZE];
            for (int i = 0; i != ARRAY_SIZE; i++)
                intArray[i] = (int) this.textBoxArray[i].parsedValue();

            long msb = Integer.toUnsignedLong(intArray[0]);
            long lsb = Integer.toUnsignedLong(intArray[2]);
            msb = (msb << 32) | Integer.toUnsignedLong(intArray[1]);
            lsb = (lsb << 32) | Integer.toUnsignedLong(intArray[3]);

            SnackBarManager.copyToClipboard(new UUID(msb, lsb).toString());
        });
    }
    public String getArrayId(int index) {
        return String.format("array%s", "." + index);
    }
}
