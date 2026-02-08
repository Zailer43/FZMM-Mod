package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import net.minecraft.network.chat.Component;

public interface IImagetextTooltip {

    Component getTooltip(ImagetextLogic logic);
}
