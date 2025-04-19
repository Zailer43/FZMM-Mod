package fzmm.zailer.me.client.gui.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.utils.history.HistoryClipboard;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
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
                                          @Nullable BannerPatternsComponent.Layer componentLayer, BannerBuilder currentBanner,
                                          DyeColor componentColor) {
        ItemStack itemComponentStack = itemComponent.stack();
        boolean isBaseBanner = componentLayer == null;

        itemComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
            this.componentExecute(clipboard, currentBanner, componentColor, componentLayer);
            return true;
        });

        ItemStack modifiedStack;
        if (isBaseBanner && currentBanner.isShield()) {
            modifiedStack = itemComponentStack.copy();
            modifiedStack.apply(DataComponentTypes.BASE_COLOR, null, dyeColor -> componentColor);
        } else if (isBaseBanner) {
            modifiedStack = itemComponentStack.copyComponentsToNewStack(BannerBuilder.getBannerByDye(componentColor), itemComponentStack.getCount());
        } else { // Add preview of edited color
            modifiedStack = itemComponentStack.copy();
            int index = currentBanner.indexOf(componentLayer);
            if (index == -1) {
                return;
            }

            modifiedStack.apply(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT, component -> {
               List<BannerPatternsComponent.Layer> layersCopy = new ArrayList<>(component.layers());

               if (layersCopy.size() < index) {
                   return component;
               }

               BannerPatternsComponent.Layer layer = layersCopy.get(index);
               BannerPatternsComponent.Layer modifiedLayer = new BannerPatternsComponent.Layer(layer.pattern(), componentColor);

               layersCopy.set(index, modifiedLayer);

                return new BannerPatternsComponent(layersCopy);
            });
        }

        itemComponent.mouseEnter().subscribe(() -> itemComponent.stack(modifiedStack));
        itemComponent.mouseLeave().subscribe(() -> itemComponent.stack(itemComponentStack));
    }

    private void componentExecute(HistoryClipboard clipboard, BannerBuilder currentBanner, DyeColor selectedColor,
                                  @Nullable BannerPatternsComponent.Layer componentLayer) {
        UISounds.playButtonSound();

        clipboard.addUndo(currentBanner);

        DyeColor componentColor = componentLayer == null ? currentBanner.baseBannerColor() : componentLayer.color();
        boolean isBaseBannerColor = currentBanner.baseBannerColor() == componentColor;

        if (Screen.hasShiftDown()) {
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
    protected Text getTooltip(@Nullable BannerPatternsComponent.Layer layer, Item item) {
        Text defaultTooltip = super.getTooltip(layer, item);
        MutableText result = defaultTooltip.copy();

        result.append("\n\n")
                .append(Text.translatable("fzmm.gui.bannerEditor.tab.changeColor.shiftHotkey"));

        return result;
    }
}
