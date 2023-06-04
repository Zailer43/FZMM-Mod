package fzmm.zailer.me.client.gui.imagetext.algorithms;

import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.utils.memento.IMemento;
import fzmm.zailer.me.client.logic.imagetext.ImagetextData;
import fzmm.zailer.me.client.logic.imagetext.ImagetextLogic;
import net.minecraft.text.MutableText;

import java.util.List;

public interface IImagetextAlgorithm extends IMemento, IScreenTab {

    String getId();

    List<MutableText> get(ImagetextLogic logic, ImagetextData data, int lineSplitInterval);

    /**
     * Sometimes it is necessary to know which characters are used in the lines,
     * such as in the BookPage tab to determine the potential number of characters
     * per page with specific characters, or in the Sign tab to know when to split
     * to a different sign.
     */
    String getCharacters();
}
