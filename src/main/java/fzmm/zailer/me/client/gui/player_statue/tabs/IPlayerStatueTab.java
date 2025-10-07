package fzmm.zailer.me.client.gui.player_statue.tabs;

import fzmm.zailer.me.client.gui.components.tabs.ITab;
import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;

public interface IPlayerStatueTab extends ITab {

    void execute(HorizontalDirectionOption direction, float x, float y, float z, String name);

    boolean canExecute();

    @Override
    default String getTranslationKey() {
        return "fzmm.gui.playerStatue.tab." + this.getId();
    }
}
