package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import javax.annotation.Nullable;

public class DisplayBuilder {
    private NbtCompound nbt;
    private Item item;
    private int count;

    public DisplayBuilder() {
        this.nbt = new NbtCompound();
        this.item = Items.STONE;
        this.count = 1;
    }

    public static DisplayBuilder builder() {
        return new DisplayBuilder();
    }

    public static DisplayBuilder of(ItemStack stack) {
        return builder()
                .nbt(stack.hasNbt() ? stack.getNbt() : new NbtCompound())
                .item(stack.getItem())
                .count(stack.getCount());
    }

    public static void addLoreToHandItem(Text text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;

        ItemStack stack = of(mc.player.getMainHandStack()).addLore(text).get();
        FzmmUtils.giveItem(stack);
    }

    public static void renameHandItem(Text text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;

        ItemStack stack = mc.player.getInventory().getMainHandStack();
        stack.setCustomName(FzmmUtils.disableItalicConfig(text));
        FzmmUtils.giveItem(stack);
    }

    public DisplayBuilder nbt(NbtCompound nbt) {
        this.nbt = nbt;
        return this;
    }

    public DisplayBuilder item(Item item) {
        this.item = item;
        return this;
    }

    public DisplayBuilder count(int count) {
        this.count = count;
        return this;
    }

    public NbtCompound getDisplay() {
        return this.nbt.contains(ItemStack.DISPLAY_KEY, NbtElement.COMPOUND_TYPE) ? this.nbt.getCompound(ItemStack.DISPLAY_KEY) : new NbtCompound();
    }

    public NbtList getLore() {
        NbtCompound display = this.getDisplay();
        return display.contains(ItemStack.LORE_KEY, NbtElement.LIST_TYPE) ? display.getList(ItemStack.LORE_KEY, NbtElement.STRING_TYPE) : new NbtList();
    }

    @Nullable
    public String getName() {
        NbtCompound display = this.getDisplay();
        return display.contains(ItemStack.NAME_KEY, NbtElement.STRING_TYPE) ? display.getString(ItemStack.NAME_KEY) : null;
    }

    public ItemStack get() {
        ItemStack stack = new ItemStack(this.item);
        stack.setCount(this.count);
        stack.setNbt(this.nbt);

        return stack;
    }

    public NbtCompound getNbt() {
        return this.nbt;
    }

    public DisplayBuilder setLore(NbtList lore) {
        NbtCompound display = this.getDisplay();

        display.put(ItemStack.LORE_KEY, lore);
        this.nbt.put(ItemStack.DISPLAY_KEY, display);
        return this;
    }

    public DisplayBuilder setName(NbtString name) {
        NbtCompound display = this.getDisplay();

        display.put(ItemStack.NAME_KEY, name);
        this.nbt.put(ItemStack.DISPLAY_KEY, display);
        return this;
    }

    public DisplayBuilder setName(String name) {
        return this.setName(Text.of(name));
    }

    public DisplayBuilder setName(Text name, int color) {
        return this.setName(name.getString(), color);
    }

    public DisplayBuilder setName(String name, int color) {
        return this.setName(Text.literal(name).setStyle(Style.EMPTY.withColor(color)));
    }

    public DisplayBuilder setName(Text name) {
        return this.setName(FzmmUtils.toNbtString(name, true));
    }

    public DisplayBuilder addLore(NbtList lore) {
        NbtList oldLore = this.getLore();

        oldLore.addAll(lore);
        this.setLore(oldLore);
        return this;
    }

    public DisplayBuilder addLore(String[] loreArr) {
        NbtList nbtList = new NbtList();
        for (String loreLine : loreArr)
            nbtList.add(FzmmUtils.toNbtString(loreLine, true));

        return this.addLore(nbtList);
    }

    public DisplayBuilder addLore(Text lore) {
        NbtList oldLore = this.getLore();

        oldLore.add(FzmmUtils.toNbtString(lore, true));
        this.setLore(oldLore);
        return this;
    }

    public DisplayBuilder addLore(Text lore, int messageColor) {
        return this.addLore(lore.copy().setStyle(Style.EMPTY.withColor(messageColor)));
    }

    public DisplayBuilder addLore(String message, int messageColor) {
        return this.addLore(Text.literal(message).setStyle(Style.EMPTY.withColor(messageColor)));
    }
}
