package fzmm.zailer.me.client.logic.history;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.DynamicRegistryManager;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

public class FzmmHistory {
    private static final ArrayDeque<NbtCompound> GENERATED_ITEMS = new ArrayDeque<>();
    private static final ArrayDeque<NbtCompound> GENERATED_HEADS = new ArrayDeque<>();
    private static final UniqueMementoCaretaker SCREEN_CARETAKER = new UniqueMementoCaretaker();

    public static void onUpdateConfig() {
        FzmmConfig.History config = FzmmClient.CONFIG.history;
        removeExcess(GENERATED_ITEMS, config.maxItemHistory());
        removeExcess(GENERATED_HEADS, config.maxHeadHistory());
    }

    public static List<ItemStack> getGeneratedItems() {
        return GENERATED_ITEMS.stream().map(FzmmHistory::parseNbt).toList();
    }

    public static List<ItemStack> getGeneratedHeads() {
        return GENERATED_HEADS.stream().map(FzmmHistory::parseNbt).toList();
    }

    public static void add(ItemStack stack) {
        if (Items.PLAYER_HEAD == stack.getItem()) {
            FzmmHistory.addGeneratedHeads(stack);
        } else {
            FzmmHistory.addGeneratedItems(stack);
        }
    }

    public static void addGeneratedItems(ItemStack stack) {
        add(stack, GENERATED_ITEMS, FzmmClient.CONFIG.history.maxItemHistory());
    }

    public static void addGeneratedHeads(ItemStack stack) {
        add(stack, GENERATED_HEADS, FzmmClient.CONFIG.history.maxHeadHistory());
    }

    public static void add(ItemStack stack, ArrayDeque<NbtCompound> compounds, int max) {
        NbtCompound stackCompound;
        try {
            // May throw an exception if the codec is invalid
            stackCompound = (NbtCompound) stack.toNbtAllowEmpty(FzmmUtils.getRegistryManager());
        } catch (Exception ignored) {
            return;
        }
        for (var compoundsFromHistory : compounds) {
            if (compoundsFromHistory.equals(stackCompound)) {
                compounds.remove(compoundsFromHistory);
                break;
            }
        }
        compounds.addFirst(stackCompound);
        removeExcess(compounds, max);
    }

    public static void removeExcess(ArrayDeque<NbtCompound> compounds, int max) {
        if (max < 1) {
            compounds.clear();
            return;
        }

        while (max < compounds.size()) {
            compounds.removeLast();
        }
    }

    public static List<ItemStack> getAllItems() {
        List<ItemStack> result = new ArrayList<>();
        result.addAll(getGeneratedItems());
        result.addAll(getGeneratedHeads());
        return result;
    }

    public static void saveScreen(IMemento screen) {
        try {
            SCREEN_CARETAKER.backup(screen);
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[FzmmHistory] Failed to save memento", e);
        }
    }

    public static void restoreScreen(IMemento screen) {
        try {
            SCREEN_CARETAKER.restore(screen);
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[FzmmHistory] Failed to restore memento", e);
        }
    }

    /**
     * This is necessary because of how registries work.
     * If an item was created in one world with one registry,
     * and then you go to another world with a different registry,
     * it will cause a codec error
     */
    private static ItemStack parseNbt(NbtCompound nbt) {
        DynamicRegistryManager registryManager = FzmmUtils.getRegistryManager();
        return ItemStack.fromNbtOrEmpty(registryManager, nbt);
    }
}
