package fzmm.zailer.me.utils;

import fi.dy.masa.malilib.util.Color4f;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import javax.annotation.Nullable;

public class DisplayUtils {
    private final NbtCompound nbt;
    private final Item item;
    private final int count;

    public DisplayUtils(ItemStack stack) {
        this.nbt = stack.hasNbt() ? stack.getNbt() : new NbtCompound();
        this.item = stack.getItem();
        this.count = stack.getCount();
    }

    public DisplayUtils(Item item) {
        this.nbt =  new NbtCompound();
        this.item = item;
        this.count = 1;
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

    public DisplayUtils setLore(NbtList lore) {
        NbtCompound display = this.getDisplay();

        display.put(ItemStack.LORE_KEY, lore);
        this.nbt.put(ItemStack.DISPLAY_KEY, display);
        return this;
    }

    public DisplayUtils setName(NbtString name) {
        NbtCompound display = this.getDisplay();

        display.put(ItemStack.NAME_KEY, name);
        this.nbt.put(ItemStack.DISPLAY_KEY, display);
        return this;
    }

    public DisplayUtils setName(String name) {
        return this.setName(Text.of(name));
    }

    public DisplayUtils setName(String name, Color4f color) {
        return this.setName(name, FzmmUtils.RGBAtoRGB(color).intValue);
    }

    public DisplayUtils setName(String name, int color) {
        return this.setName(Text.literal(name).setStyle(Style.EMPTY.withColor(color)));
    }

    public DisplayUtils setName(Text name) {
        return this.setName(FzmmUtils.toNbtString(name, true));
    }

    public DisplayUtils addLore(NbtList lore) {
        NbtList oldLore = this.getLore();

        oldLore.addAll(lore);
        this.setLore(oldLore);
        return this;
    }

    public DisplayUtils addLore(String[] loreArr) {
        NbtList nbtList = new NbtList();
        for (String loreLine : loreArr)
            nbtList.add(FzmmUtils.toNbtString(loreLine, true));

        return this.addLore(nbtList);
    }

    public DisplayUtils addLore(Text lore) {
        NbtList oldLore = this.getLore();

        oldLore.add(FzmmUtils.toNbtString(lore, true));
        this.setLore(oldLore);
        return this;
    }

    public DisplayUtils addLore(Text lore, int messageColor) {
        return this.addLore(lore.copy().setStyle(Style.EMPTY.withColor(messageColor)));
    }

    public DisplayUtils addLore(Text lore, Color4f color) {
        return this.addLore(lore.copy(), FzmmUtils.RGBAtoRGB(color).intValue);
    }

    public DisplayUtils addLore(String message, Color4f color) {
        this.addLore(message, FzmmUtils.RGBAtoRGB(color).intValue);
        return this;
    }

    public DisplayUtils addLore(String message, int messageColor) {
        return this.addLore(Text.literal(message).setStyle(Style.EMPTY.withColor(messageColor)));
    }


    public static void addLoreToHandItem(Text text) {
        MinecraftClient mc = MinecraftClient.getInstance();
        assert mc.player != null;

        ItemStack stack = new DisplayUtils(mc.player.getMainHandStack()).addLore(text).get();
        FzmmUtils.giveItem(stack);
    }
}
