package fzmm.zailer.me.client.gui.bannereditor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.bannereditor.BannerEditorScreen;
import fzmm.zailer.me.client.gui.components.containers.VerticalGridLayout;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.FlowLayout;
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

public class AddPatternsTab implements IBannerEditorTab {
    private static final String PATTERNS_GRID = "add-patterns-grid";
    private VerticalGridLayout patternsGrid;

    @Override
    public String getId() {
        return "addPatterns";
    }

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.patternsGrid = rootComponent.childById(VerticalGridLayout.class, PATTERNS_GRID);
        BaseFzmmScreen.checkNull(patternsGrid, "vertical-grid-layout", PATTERNS_GRID);
    }

    @Override
    public void update(BannerEditorScreen parent, BannerBuilder currentBanner, DyeColor color) {
        this.patternsGrid.clearChildren();
        List<Component> bannerList = new ArrayList<>();
        BannerPattern basePattern = Registries.BANNER_PATTERN.get(BannerPatterns.BASE);
        if (basePattern == null) {
            FzmmClient.LOGGER.error("[Banner editor: add pattern] base pattern is null");
            return;
        }

        for (var pattern : Registries.BANNER_PATTERN.stream().toList()) {
            if (basePattern == pattern)
                continue;

            ItemStack banner = currentBanner.copy()
                    .addPattern(color, pattern)
                    .get();

            Component itemComponent = Components.item(banner)
                    .sizing(Sizing.fixed(32), Sizing.fixed(32));

            itemComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
                UISounds.playButtonSound();

                currentBanner.addPattern(color, pattern);

                parent.updatePreview(currentBanner);
                return true;
            });
            itemComponent.cursorStyle(CursorStyle.HAND);

            bannerList.add(itemComponent);
        }
        this.patternsGrid.children(bannerList);
    }
}
