package fzmm.zailer.me.client.gui.converters.tabs;

import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.components.row.ButtonRow;
import fzmm.zailer.me.client.gui.components.row.NumberRow;
import io.wispforest.owo.config.ui.component.ConfigTextBox;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.random.Random;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class ConverterArrayToUuidTab implements IScreenTab {
    private static final int ARRAY_SIZE = 4;
    private static final String ARRAY_ID = "array%s";
    private static final String RANDOM_ID = "arrayToUuid.random";
    private static final String COPY_ID = "arrayToUuid.copy";
    private ConfigTextBox[] textBoxArray;

    @Override
    public String getId() {
        return "arrayToUuid";
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.textBoxArray = new ConfigTextBox[ARRAY_SIZE];

        for (int i = 0; i != ARRAY_SIZE; i++)
            this.textBoxArray[i] = NumberRow.setup(rootComponent, this.getArrayId(i), 0, Integer.class);

        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(RANDOM_ID), true, button -> {
            Random random = Random.create();
            for (var element : this.textBoxArray) {
                element.setText(String.valueOf(random.nextInt()));
                element.setCursorToStart(false);
            }
        });

        ButtonRow.setup(rootComponent, ButtonRow.getButtonId(COPY_ID), true, button -> {
            int[] intArray = new int[ARRAY_SIZE];
            for (int i = 0; i != ARRAY_SIZE; i++)
                intArray[i] = (int) this.textBoxArray[i].parsedValue();

            UUID uuid = arrayToUUID(intArray);
            MinecraftClient.getInstance().keyboard.setClipboard(uuid.toString());
        });
    }
    public String getArrayId(int index) {
        return String.format(ARRAY_ID, "." + index);
    }

    public static UUID arrayToUUID(int[] array) {
        long msb = Integer.toUnsignedLong(array[0]);
        long lsb = Integer.toUnsignedLong(array[2]);
        msb = (msb << 32) | Integer.toUnsignedLong(array[1]);
        lsb = (lsb << 32) | Integer.toUnsignedLong(array[3]);

        return new UUID(msb, lsb);
    }
}
