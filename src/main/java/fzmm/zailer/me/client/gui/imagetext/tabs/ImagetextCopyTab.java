package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.gui.utils.CopyTextScreen;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.Minecraft;

public class ImagetextCopyTab implements IImagetextTab {
    @Override
    public void build(IImagetextAlgorithm algorithm, ImagetextLogic logic, ImagetextData data, boolean isExecute) {
        logic.buildImagetext(algorithm, data);
    }

    @Override
    public void execute(ImagetextLogic logic) {
        Minecraft client = Minecraft.getInstance();
        client.execute(() -> FzmmUtils.setScreen(new CopyTextScreen(client.screen, logic.mergeText())));
    }

    @Override
    public void setupComponents(EFlowLayout rootComponent) {
    }

    @Override
    public String getId() {
        return "copy";
    }
}
