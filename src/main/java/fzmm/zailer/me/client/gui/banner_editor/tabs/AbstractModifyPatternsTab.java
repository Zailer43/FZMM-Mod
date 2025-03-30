package fzmm.zailer.me.client.gui.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.banner_editor.BannerEditorScreen;
import fzmm.zailer.me.client.gui.components.style.StyledComponents;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Component;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.block.entity.BannerPatterns;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractModifyPatternsTab implements IBannerEditorTab {

    protected FlowLayout patternsLayout;

    @Override
    public void setupComponents(FlowLayout rootComponent) {
        this.patternsLayout = rootComponent.childById(FlowLayout.class, this.getGridId());
        BaseFzmmScreen.checkNull(patternsLayout, "flow-layout", this.getGridId());
    }

    protected abstract String getGridId();

    public abstract boolean shouldAddBaseColor();

    @Override
    public void update(BannerEditorScreen parent, BannerBuilder currentBanner, DyeColor color) {
        this.patternsLayout.clearChildren();
        List<Component> bannerList = new ArrayList<>();
        BannerBuilder builder = currentBanner.copy().clearPatterns();

        List<BannerPatternsComponent.Layer> layers = currentBanner.layers();
        if (this.shouldAddBaseColor()) {
            layers = currentBanner.copy()
                    .clearPatterns()
                    .addLayer(currentBanner.baseBannerColor(), BannerPatterns.BASE)
                    .addLayers(layers)
                    .layers();
        }

        for (var layer : layers) {
            builder.addLayer(layer);

            ItemComponent itemComponent = StyledComponents.item(builder.copy().get());
            itemComponent.sizing(Sizing.fixed(32), Sizing.fixed(32));

            this.onItemComponentCreated(parent, itemComponent, layer, currentBanner, color);
            itemComponent.cursorStyle(CursorStyle.HAND);

            Text tooltip = this.getTooltip(layer);
            itemComponent.tooltip(tooltip);

            bannerList.add(itemComponent);
        }
        this.patternsLayout.children(bannerList);
    }

    protected abstract void onItemComponentCreated(BannerEditorScreen parent, ItemComponent itemComponent,
                                                   BannerPatternsComponent.Layer componentLayer,
                                                   BannerBuilder currentBanner, DyeColor selectedColor);

    protected Text getTooltip(BannerPatternsComponent.Layer layer) {
        return BannerBuilder.tooltipOf(layer);
    }
}
