package fzmm.zailer.me.client.gui.imagetext;

import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.utils.IMemento;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;

public interface IImagetextTab extends IScreenTab, IMemento {

    void generate(ImagetextLogic logic, ImagetextData data, boolean isExecute);

    void execute(ImagetextLogic logic);
}
