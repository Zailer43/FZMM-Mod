package fzmm.zailer.me.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtils {

    public static List<ItemStack> getItemsFromContainer(ItemStack container) {
        ItemContainerContents result = container.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.fromItems(new ArrayList<>()));

        return result.stream().toList();
    }

    public static long getSizeInBytes(List<ItemStack> stacks) {
        long size = 0;
        for (ItemStack stack : stacks) {
            size += ItemUtils.getLengthInBytes(stack);
        }
        return size;
    }

    public static List<ItemStack> getEquipmentStacks() {
        List<ItemStack> result = new ArrayList<>(Inventory.EQUIPMENT_SLOT_MAPPING.size());

        assert Minecraft.getInstance().player != null;
        Inventory inventory = Minecraft.getInstance().player.getInventory();

        for (int i = 0; i != Inventory.EQUIPMENT_SLOT_MAPPING.size(); i++) {
            result.add(inventory.getItem(Inventory.INVENTORY_SIZE + i));
        }

        return result;
    }

    public static List<ItemStack> getCombinedInventory() {
        assert Minecraft.getInstance().player != null;
        Inventory inventory = Minecraft.getInstance().player.getInventory();

        List<ItemStack> stackList = new ArrayList<>(inventory.getNonEquipmentItems());
        stackList.addAll(InventoryUtils.getEquipmentStacks());

        return stackList;
    }
}
