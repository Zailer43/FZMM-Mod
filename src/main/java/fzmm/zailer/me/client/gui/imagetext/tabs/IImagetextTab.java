package fzmm.zailer.me.client.gui.imagetext.tabs;

import fzmm.zailer.me.client.gui.components.tabs.ITab;
import fzmm.zailer.me.client.gui.imagetext.algorithms.IImagetextAlgorithm;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;

public interface IImagetextTab extends ITab {

    void build(IImagetextAlgorithm algorithm, ImagetextLogic logic, ImagetextData data, boolean isExecute);

    void execute(ImagetextLogic logic);

    @Override
    default String getTranslationKey() {
        return "fzmm.gui.imagetext.tab." + this.getId();
    }
}
