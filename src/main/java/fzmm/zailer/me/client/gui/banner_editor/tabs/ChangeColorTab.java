package fzmm.zailer.me.client.gui.banner_editor.tabs;

import fzmm.zailer.me.builders.BannerBuilder;
import fzmm.zailer.me.client.gui.banner_editor.BannerEditorScreen;
import io.wispforest.owo.ui.component.ItemComponent;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.BannerPatternsComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

import java.util.ArrayList;
import java.util.List;

public class ChangeColorTab extends AbstractModifyPatternsTab {

    private static final String PATTERNS_LAYOUT = "change-color-layout";

    @Override
    public String getId() {
        return "changeColor";
    }

    @Override
    protected String getGridId() {
        return PATTERNS_LAYOUT;
    }

    @Override
    public boolean shouldAddBaseColor() {
        return true;
    }

    @Override
    protected void onItemComponentCreated(BannerEditorScreen parent, ItemComponent itemComponent,
                                          BannerPatternsComponent.Layer componentLayer, BannerBuilder currentBanner,
                                          DyeColor componentColor) {
        ItemStack itemComponentStack = itemComponent.stack();

        List<BannerPatternsComponent.Layer> layers = currentBanner.layers();
        int index = -1;
        int patternsSize = layers.size();

        for (int i = 0; i != patternsSize; i++) {
            if (layers.get(i) == componentLayer) {
                index = i;
                break;
            }
        }

        boolean isBaseBanner = index == -1;

        itemComponent.mouseDown().subscribe((mouseX, mouseY, button) -> {
            this.componentExecute(parent, currentBanner, componentColor, componentLayer);
            return true;
        });

        ItemStack modifiedStack;
        if (isBaseBanner && currentBanner.isShield()) {
            modifiedStack = itemComponentStack.copy();
            modifiedStack.apply(DataComponentTypes.BASE_COLOR, null, dyeColor -> componentColor);
        } else if (isBaseBanner) {
            modifiedStack = itemComponentStack.copyComponentsToNewStack(BannerBuilder.getBannerByDye(componentColor), itemComponentStack.getCount());
        } else {
            modifiedStack = itemComponentStack.copy();
            int finalIndex = index;

            modifiedStack.apply(DataComponentTypes.BANNER_PATTERNS, BannerPatternsComponent.DEFAULT, component -> {
               List<BannerPatternsComponent.Layer> layersCopy = new ArrayList<>(component.layers());

               if (layersCopy.size() < finalIndex) {
                   return component;
               }

               BannerPatternsComponent.Layer layer = layersCopy.get(finalIndex);
               BannerPatternsComponent.Layer modifiedLayer = new BannerPatternsComponent.Layer(layer.pattern(), componentColor);

               layersCopy.set(finalIndex, modifiedLayer);

                return new BannerPatternsComponent(layersCopy);
            });
        }

        itemComponent.mouseEnter().subscribe(() -> itemComponent.stack(modifiedStack));
        itemComponent.mouseLeave().subscribe(() -> itemComponent.stack(itemComponentStack));
    }

    private void componentExecute(BannerEditorScreen parent, BannerBuilder currentBanner, DyeColor selectedColor,
                                  BannerPatternsComponent.Layer componentLayer) {
        UISounds.playButtonSound();

        parent.addUndo(currentBanner);

        DyeColor componentColor = componentLayer.color();
        boolean isBaseBannerColor = currentBanner.baseBannerColor() == componentColor;

        if (Screen.hasShiftDown()) {
            if (isBaseBannerColor) {
                currentBanner.baseBannerColor(selectedColor);
            }

            currentBanner.replaceColors(componentColor, selectedColor);
        } else if (isBaseBannerColor) {
            currentBanner.baseBannerColor(selectedColor);
        } else {
            currentBanner.replaceColor(componentLayer, selectedColor);
        }

        parent.updatePreview(currentBanner);
    }

    @Override
    protected Text getTooltip(BannerPatternsComponent.Layer layer) {
        Text defaultTooltip = super.getTooltip(layer);
        MutableText result = defaultTooltip.copy();

        result.append("\n\n")
                .append(Text.translatable("fzmm.gui.bannerEditor.tab.changeColor.shiftHotkey"));

        return result;
    }
}
