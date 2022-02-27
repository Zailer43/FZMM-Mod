package fzmm.zailer.me.client.gui.widget;

import fi.dy.masa.malilib.gui.widgets.WidgetListConfigOptionsBase;
import fzmm.zailer.me.client.gui.GuiOptionsBase;
import fzmm.zailer.me.client.gui.wrapper.OptionWrapper;

import java.util.Collection;

public class WidgetListOptions extends WidgetListConfigOptionsBase<OptionWrapper, WidgetOption> {
    private final GuiOptionsBase parent;

    public WidgetListOptions(int x, int y, int width, int height, int configWidth, GuiOptionsBase parent) {
        super(x, y, width, height, configWidth);
        this.parent = parent;
    }

    @Override
    protected WidgetOption createListEntryWidget(int x, int y, int listIndex, boolean isOdd, OptionWrapper wrapper) {
        return new WidgetOption(x, y, this.browserEntryWidth, this.browserEntryHeight, this.maxLabelWidth, this.configWidth, wrapper, listIndex, this);
    }

    @Override
    protected Collection<OptionWrapper> getAllEntries() {
        return this.parent.getOptions();
    }

    @Override
    protected void reCreateListEntryWidgets() {
        this.maxLabelWidth = this.getMaxNameLengthWrapped();
        super.reCreateListEntryWidgets();
    }

    private int getMaxNameLengthWrapped() {
        int width = 0;

        for (OptionWrapper wrapper : this.listContents) {
            if (wrapper.getType() == OptionWrapper.Type.OPTION) {
                assert wrapper.getConfig() != null;
                width = Math.max(width, this.getStringWidth(wrapper.getConfig().getConfigGuiDisplayName()));
            }
        }

        return width;
    }
}
