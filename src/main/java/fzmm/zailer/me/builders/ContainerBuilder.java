package fzmm.zailer.me.builders;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class ContainerBuilder {
    private final List<ItemStack> itemList;
    private Item containerItem;
    private int maxItemByContainer;

    private ContainerBuilder() {
        this.itemList = new ArrayList<>();
        this.containerItem = Items.WHITE_SHULKER_BOX;
        this.maxItemByContainer = ShulkerBoxBlockEntity.CONTAINER_SIZE;
    }

    public static ContainerBuilder builder() {
        return new ContainerBuilder();
    }

    public ContainerBuilder containerItem(Item item) {
        this.containerItem = item;
        return this;
    }

    public ContainerBuilder maxItemByContainer(int value) {
        this.maxItemByContainer = value;
        return this;
    }

    public List<ItemStack> getAsList() {
        List<List<ItemStack>> itemsTagList = this.getItemsTagList();
        List<ItemStack> containerList = new ArrayList<>();

        for (var itemTag : itemsTagList) {
            ItemStack stack = this.containerItem.getDefaultInstance();
            stack.update(DataComponents.CONTAINER, null, component -> ItemContainerContents.fromItems(itemTag));
            containerList.add(stack);
        }

        return containerList;
    }

    public List<List<ItemStack>> getItemsTagList() {
        List<List<ItemStack>> itemsTagList = new ArrayList<>();
        int containersAmount = (int) Math.ceil((float) this.itemList.size() / this.maxItemByContainer);

        for (int i = 0; i != containersAmount; i++) {
            int sublistEnd = Math.min((i + 1) * this.maxItemByContainer, this.itemList.size());
            List<ItemStack> stackSublist = this.itemList.subList(i * this.maxItemByContainer, sublistEnd);
            itemsTagList.add(stackSublist);
        }

        return itemsTagList;
    }
    public ContainerBuilder add(ItemStack stack) {
        return this.addAll(List.of(stack));
    }

    public ContainerBuilder addAll(List<ItemStack> stacks) {
        this.itemList.addAll(stacks);
        return this;
    }

    public ContainerBuilder addLoreToItems(Item itemToApply, String lore, int color) {
        for (ItemStack stack : this.itemList) {
            if (stack.getItem() == itemToApply) {
                stack.update(DataComponents.LORE, ItemLore.EMPTY, component -> {
                    List<Component> lines = new ArrayList<>(component.lines());
                    lines.add(Component.literal(lore).setStyle(Style.EMPTY.withColor(color)));

                    return new ItemLore(List.copyOf(lines));
                });
            }
        }
        return this;
    }

    public ContainerBuilder setNameStyleToItems(Style style) {
        for (ItemStack stack : this.itemList) {
            stack.update(DataComponents.CUSTOM_NAME, Component.empty(), component -> component.copy().setStyle(style));
        }
        return this;
    }
}
