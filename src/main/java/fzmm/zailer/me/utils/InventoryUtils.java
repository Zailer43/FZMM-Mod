package fzmm.zailer.me.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ContainerComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InventoryUtils {

    public static List<ItemStack> getItemsFromContainer(ItemStack container) {
        ContainerComponent result = container.getOrDefault(DataComponentTypes.CONTAINER, ContainerComponent.fromStacks(new ArrayList<>()));

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
        List<ItemStack> result = new ArrayList<>(PlayerInventory.EQUIPMENT_SLOTS.size());

        assert MinecraftClient.getInstance().player != null;
        PlayerInventory inventory = MinecraftClient.getInstance().player.getInventory();

        for (int i = 0; i != PlayerInventory.EQUIPMENT_SLOTS.size(); i++) {
            result.add(inventory.getStack(PlayerInventory.MAIN_SIZE + i));
        }

        return result;
    }

    public static List<ItemStack> getCombinedInventory() {
        assert MinecraftClient.getInstance().player != null;
        PlayerInventory inventory = MinecraftClient.getInstance().player.getInventory();

        List<ItemStack> stackList = new ArrayList<>(inventory.getMainStacks());
        stackList.addAll(InventoryUtils.getEquipmentStacks());

        return stackList;
    }
}
