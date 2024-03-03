package fzmm.zailer.me.utils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtils {

    private final ItemStack container;
    private final List<ItemStack> items;

    public InventoryUtils(ItemStack container) {
        this.container = container;
        this.items = getItemsFromContainer(this.container, false);
    }

    public ItemStack get() {
        NbtCompound blockEntityTag = new NbtCompound();
        NbtList items = new NbtList();

        int itemsLength = this.items.size();
        for (int i = 0; i != itemsLength; i++) {
            NbtCompound slotTag = getSlotTag(this.items.get(i), i);
            items.add(slotTag);
        }

        blockEntityTag.put(ShulkerBoxBlockEntity.ITEMS_KEY, items);
        this.container.setSubNbt(TagsConstant.BLOCK_ENTITY, blockEntityTag);
        return this.container;
    }

    public static void addSlot(NbtList slotList, ItemStack itemToAdd, int slot) {
        slotList.add(getSlotTag(itemToAdd, slot));
    }

    public static NbtCompound getSlotTag(ItemStack stack, int slot) {
        NbtCompound slotTag = stack.writeNbt(new NbtCompound());
        slotTag.putByte(TagsConstant.INVENTORY_SLOT, (byte) slot);
        return slotTag;
    }


    public static List<ItemStack> getItemsFromContainer(ItemStack container, boolean addEmptySlots) {
        NbtCompound blockEntityTag = container.getOrCreateSubNbt(TagsConstant.BLOCK_ENTITY);
        List<ItemStack> items = new ArrayList<>();
        if (addEmptySlots) {
            int size = getContainerSize(container.getItem());
            for (int i = 0; i < size; i++) {
                items.add(ItemStack.EMPTY);
            }
        }

        if (!blockEntityTag.contains(ShulkerBoxBlockEntity.ITEMS_KEY, NbtElement.LIST_TYPE))
            return items;

        NbtList itemsTag = blockEntityTag.getList(ShulkerBoxBlockEntity.ITEMS_KEY, NbtElement.COMPOUND_TYPE);

        for (NbtElement itemTag : itemsTag) {
            if (itemTag instanceof NbtCompound nbtCompound) {
                addItem(items, addEmptySlots, nbtCompound);
            }
        }

        return items;
    }

    private static void addItem(List<ItemStack> items, boolean addEmptySlot, NbtCompound nbtCompound) {
        ItemStack stack = ItemStack.fromNbt(nbtCompound);
        if (addEmptySlot && nbtCompound.contains(TagsConstant.INVENTORY_SLOT, NbtElement.BYTE_TYPE)) {
            int slot = nbtCompound.getByte(TagsConstant.INVENTORY_SLOT);
            if (slot < 0)
                return;

            if (slot >= items.size()) {
                for (int i = items.size(); i <= slot; i++)
                    items.add(ItemStack.EMPTY);
            }

            items.set(slot, stack);
        } else {
            items.add(stack);
        }
    }


    public static int getContainerSize(Item containerItem) {
        if (containerItem instanceof BlockItem blockItem) {
            Block block = blockItem.getBlock();
            if (block instanceof BlockEntityProvider blockEntityProvider) {
                BlockEntity blockEntity = blockEntityProvider.createBlockEntity(new BlockPos(0, 0, 0), block.getDefaultState());

                if (blockEntity instanceof Inventory inventory)
                    return inventory.size();
            }
        }

        return 0;
    }
}
