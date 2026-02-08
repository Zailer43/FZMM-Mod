package fzmm.zailer.me.client.gui.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.utils.history.HistoryClipboard;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ChangeColorTab extends AbstractModifyPatternTab {

    @Override
    public String buttonId() {
        return "change-color";
    }

    @Override
    public boolean shouldAddBase() {
        return true;
    }

    @Override
    protected void onItemComponentCreated(HistoryClipboard clipboard, ItemComponent itemComponent,
                                          @Nullable BannerPatternLayers.Layer componentLayer, BannerBuilder currentBanner,
                                          DyeColor componentColor) {
        ItemStack itemComponentStack = itemComponent.stack();
        boolean isBaseBanner = componentLayer == null;

        itemComponent.mouseDown().subscribe((input, doubled) -> {
            this.componentExecute(input, clipboard, currentBanner, componentColor, componentLayer);
            return true;
        });

        ItemStack modifiedStack;
        if (isBaseBanner && currentBanner.isShield()) {
            modifiedStack = itemComponentStack.copy();
            modifiedStack.update(DataComponents.BASE_COLOR, null, dyeColor -> componentColor);
        } else if (isBaseBanner) {
            modifiedStack = itemComponentStack.transmuteCopy(BannerBuilder.getBannerByDye(componentColor), itemComponentStack.getCount());
        } else { // Add preview of edited color
            modifiedStack = itemComponentStack.copy();
            int index = currentBanner.indexOf(componentLayer);
            if (index == -1) {
                return;
            }

            modifiedStack.update(DataComponents.BANNER_PATTERNS, BannerPatternLayers.EMPTY, component -> {
               List<BannerPatternLayers.Layer> layersCopy = new ArrayList<>(component.layers());

               if (layersCopy.size() < index) {
                   return component;
               }

               BannerPatternLayers.Layer layer = layersCopy.get(index);
               BannerPatternLayers.Layer modifiedLayer = new BannerPatternLayers.Layer(layer.pattern(), componentColor);

               layersCopy.set(index, modifiedLayer);

                return new BannerPatternLayers(layersCopy);
            });
        }

        itemComponent.mouseEnter().subscribe(() -> itemComponent.stack(modifiedStack));
        itemComponent.mouseLeave().subscribe(() -> itemComponent.stack(itemComponentStack));
    }

    private void componentExecute(InputWithModifiers input, HistoryClipboard clipboard, BannerBuilder currentBanner, DyeColor selectedColor,
                                  @Nullable BannerPatternLayers.Layer componentLayer) {
        UISounds.playButtonSound();

        clipboard.addUndo(currentBanner);

        DyeColor componentColor = componentLayer == null ? currentBanner.baseBannerColor() : componentLayer.color();
        boolean isBaseBannerColor = currentBanner.baseBannerColor() == componentColor;

        if (input.hasShiftDown()) {
            if (isBaseBannerColor) {
                currentBanner.baseBannerColor(selectedColor);
            }

            currentBanner.replaceColors(componentColor, selectedColor);
        } else if (componentLayer == null) {
            currentBanner.baseBannerColor(selectedColor);
        } else {
            currentBanner.replaceColor(componentLayer, selectedColor);
        }

        clipboard.change(currentBanner);
    }

    @Override
    protected Component getTooltip(@Nullable BannerPatternLayers.Layer layer, Item item) {
        Component defaultTooltip = super.getTooltip(layer, item);
        MutableComponent result = defaultTooltip.copy();

        result.append("\n\n")
                .append(Component.translatable("fzmm.gui.bannerEditor.tab.changeColor.shiftHotkey"));

        return result;
    }
}
