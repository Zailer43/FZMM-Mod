package fzmm.zailer.me.client.gui.bannereditor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.bannereditor.BannerEditorScreen;
import fzmm.zailer.me.client.gui.bannereditor.IBannerEditorTab;
import fzmm.zailer.me.client.gui.utils.containers.VerticalGridLayout;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.DyeColor;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractModifyPatternsTab implements IBannerEditorTab {

    protected VerticalGridLayout patternsGrid;

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.patternsGrid = rootComponent.childById(VerticalGridLayout.class, this.getGridId());
        BaseFzmmScreen.checkNull(patternsGrid, "vertical-grid-layout", this.getGridId());
    }

    protected abstract String getGridId();

    @Override
    public void update(BannerEditorScreen parent, BannerBuilder currentBanner, DyeColor color) {
        this.patternsGrid.clearChildren();
        List<Component> bannerList = new ArrayList<>();
        BannerBuilder builder = currentBanner.copy().clear();

        for (var pattern : currentBanner.patterns()) {
            builder.addPattern(pattern);

            ItemComponent itemComponent = Components.item(builder.get());
            itemComponent.sizing(Sizing.fixed(32), Sizing.fixed(32));

            this.onItemCreated(parent, itemComponent, pattern, currentBanner, color);
            itemComponent.cursorStyle(CursorStyle.HAND);

            bannerList.add(itemComponent);
        }
        this.patternsGrid.children(bannerList);
    }

    protected abstract void onItemCreated(BannerEditorScreen parent, ItemComponent itemComponent, NbtElement pattern, BannerBuilder currentBanner, DyeColor color);
}
