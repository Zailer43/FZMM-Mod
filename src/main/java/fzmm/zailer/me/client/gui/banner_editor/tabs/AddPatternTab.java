package fzmm.zailer.me.client.gui.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.utils.history.HistoryClipboard;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.block.entity.BannerPattern;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;

import java.util.ArrayList;
import java.util.List;

public class AddPatternTab implements IBannerTab {

    @Override
    public String buttonId() {
        return "add-pattern";
    }

    @Override
    public List<Component>  update(HistoryClipboard clipboard, BannerBuilder currentBanner, DyeColor color) {
        List<Component> bannerList = new ArrayList<>();
        BannerPattern basePattern = Registries.BANNER_PATTERN.get(BannerPatterns.BASE);
        if (basePattern == null) {
            FzmmClient.LOGGER.error("[Banner editor: add pattern] base pattern is null");
            return bannerList;
        }

        for (var pattern : Registries.BANNER_PATTERN.stream().toList()) {
            if (basePattern == pattern) {
                continue;
            }

            ItemStack banner = currentBanner.copy()
                    .addPattern(color, pattern)
                    .get();

            Component itemComponent = EComponents.item(banner)
                    .sizing(Sizing.fixed(32), Sizing.fixed(32))
                    .tooltip(BannerBuilder.tooltipOf(color, Registries.BANNER_PATTERN.getEntry(pattern)));

            itemComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
                UISounds.playButtonSound();
                clipboard.addUndo(currentBanner);

                currentBanner.addPattern(color, pattern);

                clipboard.change(currentBanner);
                return true;
            });
            itemComponent.cursorStyle(CursorStyle.HAND);

            bannerList.add(itemComponent);
        }

        return bannerList;
    }
}
