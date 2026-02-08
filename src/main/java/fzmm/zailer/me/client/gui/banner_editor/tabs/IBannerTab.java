package fzmm.zailer.me.client.gui.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.utils.history.HistoryClipboard;
import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.world.item.DyeColor;

import java.util.List;

public interface IBannerTab {

    String buttonId();

    List<UIComponent> update(HistoryClipboard clipboard, BannerBuilder currentBanner, DyeColor color);
}
