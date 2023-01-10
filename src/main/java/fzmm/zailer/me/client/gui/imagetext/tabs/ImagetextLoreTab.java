package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.EnumWidget;
import fzmm.zailer.me.client.gui.components.row.EnumRow;
import fzmm.zailer.me.client.gui.imagetext.IImagetextTab;
import fzmm.zailer.me.client.gui.options.LoreOption;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtList;

public class ImagetextLoreTab implements IImagetextTab {
    private static final String LORE_MODE_ID = "loreMode";
    private EnumWidget loreModeOption;

    @Override
    public void generate(ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        logic.generateImagetext(data);
    }

    @Override
    public void execute(ImagetextLogic logic) {
        assert MinecraftClient.getInstance().player != null;
        ItemStack stack = MinecraftClient.getInstance().player.getMainHandStack();
        LoreOption loreOption = (LoreOption) this.loreModeOption.getValue();
        NbtList imagetext = logic.get();

        if (stack.isEmpty())
            stack = FzmmUtils.getItem(FzmmClient.CONFIG.imagetext.defaultItem()).getDefaultStack();
        DisplayBuilder display = DisplayBuilder.of(stack);
        if (loreOption == LoreOption.ADD)
            display.addLore(imagetext).get();
        else
            display.setLore(imagetext).get();

        FzmmUtils.giveItem(display.get());
    }

    @Override
    public String getId() {
        return "lore";
    }
    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.loreModeOption = EnumRow.setup(rootComponent, LORE_MODE_ID, LoreOption.ADD, null);
    }
}
