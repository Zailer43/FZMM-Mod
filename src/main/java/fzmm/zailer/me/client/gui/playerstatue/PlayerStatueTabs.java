package fzmm.zailer.me.client.gui.playerstatue;

import fzmm.zailer.me.client.gui.options.HorizontalDirectionOption;
import fzmm.zailer.me.client.gui.playerstatue.tabs.PlayerStatueGenerateTab;
import fzmm.zailer.me.client.gui.playerstatue.tabs.PlayerStatueUpdateTab;
import io.wispforest.owo.ui.container.FlowLayout;

public enum PlayerStatueTabs implements IPlayerStatueTab {
    CREATE(new PlayerStatueGenerateTab()),
    UPDATE(new PlayerStatueUpdateTab());

    private final IPlayerStatueTab tab;

    PlayerStatueTabs(IPlayerStatueTab tab) {
        this.tab = tab;
    }


    @Override
    public String getId() {
        return this.tab.getId();
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.tab.setupComponents(rootComponent);
    }

    @Override
    public void execute(HorizontalDirectionOption direction, float x, float y, float z, String name) {
        this.tab.execute(direction, x, y, z, name);
    }

    @Override
    public boolean canExecute() {
        return this.tab.canExecute();
    }
}
