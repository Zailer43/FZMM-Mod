package fzmm.zailer.me.client.gui.item_editor.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.gui.item_editor.banner_editor.BannerEditorScreen;
import fzmm.zailer.me.client.gui.components.tabs.IScreenTab;
import net.minecraft.util.DyeColor;

public interface IBannerEditorTab extends IScreenTab {

    void update(BannerEditorScreen parent, BannerBuilder currentBanner, DyeColor color);
}
