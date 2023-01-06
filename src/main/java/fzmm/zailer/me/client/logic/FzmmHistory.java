package fzmm.zailer.me.client.logic;

import com.google.common.collect.ImmutableList;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.config.FzmmConfig;
import net.minecraft.item.ItemStack;

import java.util.ArrayDeque;
import java.util.List;

public class FzmmHistory {
    private static final ArrayDeque<ItemStack> GENERATED_ITEMS = new ArrayDeque<>();
    private static final ArrayDeque<ItemStack> GENERATED_HEADS = new ArrayDeque<>();

    public static void update() {
        FzmmConfig.History config = FzmmClient.CONFIG.history;
        removeExcess(GENERATED_ITEMS, config.maxHeadHistory());
        removeExcess(GENERATED_HEADS, config.maxHeadHistory());
    }

    public static List<ItemStack> getGeneratedItems() {
        return ImmutableList.copyOf(GENERATED_ITEMS);
    }

    public static List<ItemStack> getGeneratedHeads() {
        return ImmutableList.copyOf(GENERATED_HEADS);
    }

    public static void addGeneratedItems(ItemStack stack) {
        add(stack, GENERATED_ITEMS, FzmmClient.CONFIG.history.maxItemHistory());
    }

    public static void addGeneratedHeads(ItemStack stack) {
        add(stack, GENERATED_HEADS, FzmmClient.CONFIG.history.maxHeadHistory());
    }

    public static void add(ItemStack stack, ArrayDeque<ItemStack> stacks, int max) {
        for (var stackFromHistory : stacks) {
            if (ItemStack.areEqual(stackFromHistory, stack))
                return;
        }
        stacks.addFirst(stack);
        removeExcess(stacks, max);
    }

    public static void removeExcess(ArrayDeque<ItemStack> stacks, int max){
        if (max < 1) {
            stacks.clear();
            return;
        }

        while (max < stacks.size())
            stacks.removeLast();
    }
}
