package fzmm.zailer.me.client.gui.imagetext;

import fzmm.zailer.me.builders.DisplayBuilder;
import fzmm.zailer.me.client.gui.imagetext.tabs.ImagetextHologramTab;
import fzmm.zailer.me.client.gui.utils.autoplacer.AbstractAutoPlacer;
import fzmm.zailer.me.client.gui.utils.autoplacer.AutoPlacerHud;
import fzmm.zailer.me.utils.InventoryUtils;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Component;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class HologramPlacerScreen extends AbstractAutoPlacer {
    public static boolean isActive = false;
    private final ItemStack hologramStack;
    private final List<ItemStack> containerItems;

    public HologramPlacerScreen(ItemStack hologramStack) {
        super("utils/base_auto_placer", "imagetextHologramPlacer", null);
        this.hologramStack = hologramStack;
        this.containerItems = new ArrayList<>();

        for (var stack : InventoryUtils.getItemsFromContainer(hologramStack)) {
            this.containerItems.addAll(InventoryUtils.getItemsFromContainer(stack));
        }
    }

    public static AutoPlacerHud.Activation getActivation() {
        Predicate<ItemStack> predicate = itemStack -> !HologramPlacerScreen.isActive &&
                itemStack.getItem() instanceof BlockItem blockItem &&
                blockItem.getBlock() instanceof BlockWithEntity &&
                HologramPlacerScreen.isHologram(itemStack);

        return new AutoPlacerHud.Activation(predicate, HologramPlacerScreen::new, new ArrayList<>());
    }

    @Override
    protected List<Component> getInfoLabels() {
        List<Component> labelList = new ArrayList<>();

        labelList.add(Components.label(this.hologramStack.getName()));

        for (var text : DisplayBuilder.of(this.hologramStack).getLoreText()) {
            labelList.add(Components.label(text));
        }

        return labelList;
    }

    @Override
    protected List<ItemStack> getItems() {
        return this.containerItems;
    }

    @Override
    protected ItemStack getFinalStack() {
        return this.hologramStack;
    }

    @Override
    protected boolean isActive() {
        return isActive;
    }


    public static boolean isHologram(ItemStack container) {
        List<ItemStack> containerItems = InventoryUtils.getItemsFromContainer(container);

        if (containerItems.isEmpty())
            return false;

        for (var containerStack : containerItems) {
            List<ItemStack> subContainerItems = InventoryUtils.getItemsFromContainer(containerStack);

            if (subContainerItems.isEmpty())
                return false;

            for (var subContainerStack : subContainerItems) {
                if (!ImagetextHologramTab.isHologramPart(subContainerStack))
                    return false;
            }
        }

        return true;
    }
}
