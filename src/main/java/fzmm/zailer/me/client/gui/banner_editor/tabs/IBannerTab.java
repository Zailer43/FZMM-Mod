package fzmm.zailer.me.client.gui.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.utils.history.HistoryClipboard;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.util.DyeColor;

import java.util.List;

public interface IBannerTab {

    String buttonId();

    List<Component> update(HistoryClipboard clipboard, BannerBuilder currentBanner, DyeColor color);
}
