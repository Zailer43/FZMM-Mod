package fzmm.zailer.me.client.gui.playerstatue;

import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;

public interface IPlayerStatueTab extends IScreenTab {

    void execute(HorizontalDirectionOption direction, float x, float y, float z, String name);

    boolean canExecute();
}
