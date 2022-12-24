package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.client.gui.imagetext.IImagetextTab;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;

public class ImagetextJsonTab implements IImagetextTab {
    @Override
    public void execute(ImagetextLogic logic) {
        MinecraftClient.getInstance().keyboard.setClipboard(logic.getImagetextString());
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
    }

    @Override
    public String getId() {
        return "json";
    }
}
