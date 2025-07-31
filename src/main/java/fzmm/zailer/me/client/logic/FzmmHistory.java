package fzmm.zailer.me.client.logic;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.ItemUtils;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FzmmHistory {
    private static final ArrayDeque<NbtCompound> GENERATED_ITEMS = new ArrayDeque<>();
    private static final ArrayDeque<NbtCompound> GENERATED_HEADS = new ArrayDeque<>();

    public static void update() {
        FzmmConfig.History config = FzmmClient.CONFIG.history;
        removeExcess(GENERATED_ITEMS, config.maxItemHistory());
        removeExcess(GENERATED_HEADS, config.maxHeadHistory());
    }

    public static List<ItemStack> getGeneratedItems() {
        return parseNbt(GENERATED_ITEMS);
    }

    public static List<ItemStack> getGeneratedHeads() {
        return parseNbt(GENERATED_HEADS);
    }

    private static List<ItemStack> parseNbt(ArrayDeque<NbtCompound> compounds) {
        return compounds.stream().map(FzmmHistory::parseNbt)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
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
        if (stack.isEmpty()) {
            return;
        }

        Optional<NbtElement> stackNbtOptional = ItemUtils.encodeToNbt(stack).resultOrPartial();
        if (stackNbtOptional.isEmpty()) {
            return;
        }
        NbtCompound stackCompound = (NbtCompound) stackNbtOptional.get();

        for (var compoundsFromHistory : compounds) {
            if (compoundsFromHistory.equals(stackCompound)) {
                compounds.remove(compoundsFromHistory);
                break;
            }
        }
        compounds.addFirst(stackCompound);
        removeExcess(compounds, max);
    }

    public static void removeExcess(ArrayDeque<NbtCompound> compounds, int max){
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

    /**
     * This is necessary because of how registries work.
     * If an item was created in one world with one registry,
     * and then you go to another world with a different registry,
     * it will cause a codec error
     */
    private static Optional<ItemStack> parseNbt(NbtCompound nbt) {
        return ItemUtils.decodeFromNbt(nbt).result();
    }
}
