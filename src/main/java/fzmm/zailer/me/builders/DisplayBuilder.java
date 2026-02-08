package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.ItemUtils;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DisplayBuilder {
    private ItemStack stack;
    private List<Component> lore = new ArrayList<>();
    @Nullable
    private Component customName = null;

    public DisplayBuilder() {
        this.stack = Items.STONE.getDefaultInstance();
    }

    public static DisplayBuilder builder() {
        return new DisplayBuilder();
    }

    public static DisplayBuilder of(ItemStack stack) {
        return builder().stack(stack.copy());
    }

    public static void addLoreToHandItem(MutableComponent text) {
        ItemStack stack = ItemUtils.from(InteractionHand.MAIN_HAND);

        stack.update(DataComponents.LORE, ItemLore.EMPTY, loreComponent -> {
            List<Component> loreList = new ArrayList<>(loreComponent.lines());
            loreList.add(FzmmUtils.disableItalicConfig(text));
            return new ItemLore(loreList);
        });

        ItemUtils.give(stack);
    }

    public static void renameHandItem(MutableComponent text) {
        ItemStack stack = ItemUtils.from(InteractionHand.MAIN_HAND);

        stack.update(DataComponents.CUSTOM_NAME, null, component -> FzmmUtils.disableItalicConfig(text));
        ItemUtils.give(stack);
    }

    public DisplayBuilder item(Item item) {
        return this.stack(item.getDefaultInstance());
    }

    public DisplayBuilder stack(ItemStack stack) {
        this.stack = stack.copy();
        this.lore = new ArrayList<>(stack.getComponents().getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines());
        this.customName = stack.getComponents().getOrDefault(DataComponents.CUSTOM_NAME, null);

        if (this.customName != null) {
            this.customName = this.customName.copy();
        }

        return this;
    }

    public Component getName() {
        return this.customName == null ? Component.empty() : this.customName;
    }

    public List<Component> getLoreText() {
        List<Component> result = this.stack.getComponents().getOrDefault(DataComponents.LORE, ItemLore.EMPTY).lines();

        return new ArrayList<>(result);
    }

    public ItemStack get() {
        if (!this.lore.isEmpty()) {
            if (this.lore.size() > ItemLore.MAX_LINES) {
                this.lore = this.lore.subList(0, ItemLore.MAX_LINES);
            }

            this.stack.update(DataComponents.LORE, null, component -> new ItemLore(List.copyOf(this.lore)));
        }

        if (this.customName != null) {
            this.stack.update(DataComponents.CUSTOM_NAME, null, component -> this.customName.copy());
        }

        return this.stack;
    }

    public DisplayBuilder setLore(List<Component> lore) {
        this.lore = lore;
        return this;
    }

    public DisplayBuilder setName(MutableComponent name) {
        this.customName = FzmmUtils.disableItalicConfig(name, true);
        return this;
    }

    public DisplayBuilder setName(String name) {
        return this.setName(name, false);
    }

    public DisplayBuilder setName(String name, boolean useDisableItalicConfig) {
        return this.setName(FzmmUtils.disableItalicConfig(name, useDisableItalicConfig));
    }

    public DisplayBuilder setName(Component name, int color) {
        return this.setName(name.getString(), color);
    }

    public DisplayBuilder setName(String name, int color) {
        return this.setName(Component.literal(name).setStyle(Style.EMPTY.withColor(color)));
    }

    public DisplayBuilder addLore(List<Component> lore) {
        this.lore.addAll(lore);
        return this;
    }

    public DisplayBuilder addLore(String[] loreArr) {
        List<Component> loreList = Arrays.stream(loreArr)
                .map(loreLine -> (Component) FzmmUtils.disableItalicConfig(loreLine, true))
                .toList();

        return this.addLore(loreList);
    }

    public DisplayBuilder addLore(String lore) {
        return this.addLore(Component.literal(lore));
    }

    public DisplayBuilder addLore(Component lore) {
        this.lore.add(lore);
        return this;
    }

    public DisplayBuilder addLore(Component lore, int messageColor) {
        return this.addLore(lore.copy().setStyle(Style.EMPTY.withColor(messageColor)));
    }

    public DisplayBuilder addLore(String message, int messageColor) {
        return this.addLore(Component.literal(message).setStyle(Style.EMPTY.withColor(messageColor)));
    }
}
