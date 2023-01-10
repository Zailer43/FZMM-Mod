package fzmm.zailer.me.client.gui.imagetext;

import fzmm.zailer.me.client.gui.IScreenTab;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;

public interface IImagetextTab extends IScreenTab {

    void generate(ImagetextLogic logic, ImagetextData data, boolean isExecute);

    void execute(ImagetextLogic logic);
}
