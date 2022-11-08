package fzmm.zailer.me.utils;

import fzmm.zailer.me.mixin.HandledScreenAccessor;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class InventoryUtils {

    private final ItemStack container;
    private final List<ItemStack> items;

    public InventoryUtils(ItemStack container) {
        this.container = container;
        this.items = getItemsFromContainer(this.container);
    }

    public InventoryUtils addItem(List<ItemStack> items) {
        this.items.addAll(items);
        return this;
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


    public static List<ItemStack> getItemsFromContainer(ItemStack container) {
        List<ItemStack> items = new ArrayList<>();

        NbtCompound blockEntityTag = container.getOrCreateSubNbt(TagsConstant.BLOCK_ENTITY);
        if (blockEntityTag.contains(ShulkerBoxBlockEntity.ITEMS_KEY, NbtElement.LIST_TYPE)) {
            NbtList itemsTag = blockEntityTag.getList(ShulkerBoxBlockEntity.ITEMS_KEY, NbtElement.COMPOUND_TYPE);

            for (NbtElement itemTag : itemsTag) {
                if (itemTag instanceof NbtCompound itemCompound) {
                    try {

                        if (!itemCompound.contains(TagsConstant.INVENTORY_ID, NbtElement.STRING_TYPE))
                            continue;
                        String idString = itemCompound.getString(TagsConstant.INVENTORY_ID);
                        Item item = Registry.ITEM.get(new Identifier(idString));

                        if (!itemCompound.contains(TagsConstant.INVENTORY_COUNT, NbtElement.BYTE_TYPE))
                            continue;
                        byte count = itemCompound.getByte(TagsConstant.INVENTORY_COUNT);

                        if (!itemCompound.contains(TagsConstant.INVENTORY_SLOT, NbtElement.BYTE_TYPE))
                            continue;

                        ItemStack stack = new ItemStack(item);
                        stack.setCount(count);

                        if (itemCompound.contains(TagsConstant.INVENTORY_TAG, NbtElement.COMPOUND_TYPE)) {
                            NbtCompound tag = itemCompound.getCompound(TagsConstant.INVENTORY_TAG);
                            stack.setNbt(tag);
                        }

                        items.add(stack);
                    } catch (Exception ignored) {
                    }
                }
            }
        }

        return items;
    }

    @Nullable
    public static ItemStack getFocusedItem() {
        Slot slot = getFocusedSlot();
        return slot == null ? null : slot.getStack();
    }

    @Nullable
    public static Slot getFocusedSlot() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (!(mc.currentScreen instanceof HandledScreen<?> screen))
            return null;

        return ((HandledScreenAccessor) screen).getFocusedSlot();
    }

    public static ItemStack getInItemFrame(ItemStack stack, boolean glowing) {
        ItemStack itemFrame = new ItemStack(glowing ? Items.GLOW_ITEM_FRAME : Items.ITEM_FRAME);
        NbtCompound itemTag = stack.writeNbt(new NbtCompound());
        NbtCompound entityTag = new NbtCompound();

        entityTag.put(TagsConstant.ITEM_FRAME_ITEM, itemTag);
        itemFrame.setSubNbt(EntityType.ENTITY_TAG_KEY, entityTag);

        return itemFrame;
    }
}
