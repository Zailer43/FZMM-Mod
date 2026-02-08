package fzmm.zailer.me.client.gui.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.utils.history.HistoryClipboard;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractModifyPatternTab implements IBannerTab {

    public abstract boolean shouldAddBase();

    @Override
    public List<UIComponent> update(HistoryClipboard clipboard, BannerBuilder currentBanner, DyeColor color) {
        List<UIComponent> bannerList = new ArrayList<>();
        BannerBuilder builder = currentBanner.copy().clearPatterns();

        List<BannerPatternLayers.Layer> layers = currentBanner.layers();
        if (this.shouldAddBase()) {
            this.addPreview(clipboard, currentBanner, color, null, builder, bannerList);
        }

        for (var layer : layers) {
            builder.addLayer(layer);
            this.addPreview(clipboard, currentBanner, color, layer, builder, bannerList);
        }
        return bannerList;
    }

    private void addPreview(HistoryClipboard clipboard, BannerBuilder currentBanner, DyeColor color,
                            @Nullable BannerPatternLayers.Layer layer, BannerBuilder builder, List<UIComponent> bannerList) {
        ItemComponent itemComponent = EComponents.item(builder.copy().get());
        itemComponent.sizing(Sizing.fixed(32), Sizing.fixed(32));

        this.onItemComponentCreated(clipboard, itemComponent, layer, currentBanner, color);
        itemComponent.cursorStyle(CursorStyle.HAND);

        net.minecraft.network.chat.Component tooltip = this.getTooltip(layer, itemComponent.stack().getItem());
        itemComponent.tooltip(tooltip);

        bannerList.add(itemComponent);
    }

    protected abstract void onItemComponentCreated(HistoryClipboard clipboard, ItemComponent itemComponent,
                                                   @Nullable BannerPatternLayers.Layer componentLayer,
                                                   BannerBuilder currentBanner, DyeColor selectedColor);

    protected net.minecraft.network.chat.Component getTooltip(@Nullable BannerPatternLayers.Layer layer, Item item) {
        if (layer == null) {
            return net.minecraft.network.chat.Component.translatable("block.minecraft.banner.base." + BannerBuilder.baseBannerColor(item).getName());
        } else {
            return BannerBuilder.tooltipOf(layer);
        }
    }
}
