package fzmm.zailer.me.client.gui.playerstatue.tabs;

import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.gui.utils.IMemento;

public interface IPlayerStatueTab extends IScreenTab, IMemento {

    void execute(HorizontalDirectionOption direction, float x, float y, float z, String name);

    boolean canExecute();
}
