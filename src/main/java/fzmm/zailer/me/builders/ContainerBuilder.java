package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ContainerBuilder {
    private final List<ItemStack> itemList;
    private Item containerItem;
    private int maxItemByContainer;

    private ContainerBuilder() {
        this.itemList = new ArrayList<>();
        this.containerItem = Items.WHITE_SHULKER_BOX;
        this.maxItemByContainer = ShulkerBoxBlockEntity.field_31356;
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
        List<NbtList> itemsTagList = this.getItemsTagList();
        List<ItemStack> containerList = new ArrayList<>();

        for (var itemTag : itemsTagList) {
            NbtCompound blockEntityTag = new NbtCompound();
            blockEntityTag.put(ShulkerBoxBlockEntity.ITEMS_KEY, itemTag);

            ItemStack stack = this.containerItem.getDefaultStack();
            stack.setSubNbt(TagsConstant.BLOCK_ENTITY, blockEntityTag);
            containerList.add(stack);
        }

        return containerList;
    }

    public List<NbtList> getItemsTagList() {
        List<NbtList> itemsTagList = new ArrayList<>();
        int containersAmount = (int) Math.ceil((float) this.itemList.size() / this.maxItemByContainer);

        for (int i = 0; i != containersAmount; i++) {
            int sublistEnd = Math.min((i + 1) * this.maxItemByContainer, this.itemList.size());
            List<ItemStack> stackSublist = this.itemList.subList(i * this.maxItemByContainer, sublistEnd);
            itemsTagList.add(this.getItemsTag(stackSublist));
        }

        return itemsTagList;
    }

    public NbtList getItemsTag(List<ItemStack> stackList) {
        NbtList itemsTag = new NbtList();
        for (int i = 0; i != stackList.size(); i++) {
            NbtCompound tag = stackList.get(i).writeNbt(new NbtCompound());
            tag.putByte(TagsConstant.INVENTORY_SLOT, (byte) i);
            itemsTag.add(tag);
        }
        return itemsTag;
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
                NbtCompound tag = DisplayBuilder.of(stack).addLore(lore, color).getNbt();
                stack.setNbt(tag);
            }
        }
        return this;
    }

    public ContainerBuilder setNameStyleToItems(Style style) {
        for (ItemStack stack : this.itemList) {
            String name = DisplayBuilder.of(stack).getName();
            if (name == null)
                continue;
            MutableText nameText;
            try {
                nameText = Text.Serializer.fromJson(name);
                if (nameText == null)
                    nameText = Text.literal(name);

                nameText.setStyle(style);
                stack.setCustomName(nameText);
            } catch (Exception ignored) {
            }
        }
        return this;
    }
}
