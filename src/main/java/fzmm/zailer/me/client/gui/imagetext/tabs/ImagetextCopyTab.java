package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.client.gui.utils.CopyTextScreen;
import fzmm.zailer.me.client.gui.imagetext.IImagetextTab;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.client.MinecraftClient;

public class ImagetextCopyTab implements IImagetextTab {
    @Override
    public void execute(ImagetextLogic logic) {
        MinecraftClient client = MinecraftClient.getInstance();
        CopyTextScreen copyTextScreen = new CopyTextScreen(client.currentScreen, logic.getText());
        client.setScreen(copyTextScreen);
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
    }

    @Override
    public String getId() {
        return "copy";
    }
}
