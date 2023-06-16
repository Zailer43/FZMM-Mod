package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

public class SignBuilder {

    public static final int MAX_ROWS = 4;
    private ItemStack stack;
    private final NbtList frontTextList;
    private final NbtCompound frontCompound;
    private final NbtList backTextList;
    private final NbtCompound backCompound;
    private boolean isWaxed;

    private SignBuilder() {
        this.stack = Items.OAK_SIGN.getDefaultStack();
        this.frontTextList = new NbtList();
        this.frontCompound = new NbtCompound();
        this.backTextList = new NbtList();
        this.backCompound = new NbtCompound();
        this.isWaxed = false;
    }

    public static SignBuilder builder() {
        return new SignBuilder();
    }


    public SignBuilder item(Item item) {
        this.stack = item.getDefaultStack();
        return this;
    }


    public SignBuilder addFrontLine(NbtString nbtString) {
        return this.addLine(this.frontTextList, nbtString);
    }

    public SignBuilder addBackLine(NbtString nbtString) {
        return this.addLine(this.backTextList, nbtString);
    }

    private SignBuilder addLine(NbtList list, NbtString nbtString) {
        list.add(nbtString);
        return this;
    }

    public SignBuilder addFrontLine(NbtString nbtString, int expectedWidth) {
        return this.addLine(this.frontTextList, nbtString, expectedWidth);
    }

    public SignBuilder addBackLine(NbtString nbtString, int expectedWidth) {
        return this.addLine(this.backTextList, nbtString, expectedWidth);
    }

    private SignBuilder addLine(NbtList list, NbtString nbtString, int expectedWidth) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        MutableText text = Text.Serializer.fromJson(nbtString.asString());
        if (text == null)
            return this;

        int spaceCount = 0;
        MutableText textCopy = text.copy();
        while (textRenderer.getWidth(textCopy) < expectedWidth) {
            textCopy.append(" ");
            spaceCount++;
        }
        text.append(" ".repeat(spaceCount));

        nbtString = FzmmUtils.toNbtString(text, false);
        return this.addLine(list, nbtString);
    }

    public SignBuilder glowingFront() {
        return this.glowing(this.frontCompound);
    }

    public SignBuilder glowingBack() {
        return this.glowing(this.backCompound);
    }

    private SignBuilder glowing(NbtCompound compound) {
        compound.putBoolean(TagsConstant.SIGN_GLOWING_TEXT, true);
        return this;
    }

    public SignBuilder colorFront(String color) {
        return this.color(this.frontCompound, color);
    }

    public SignBuilder colorBack(String color) {
        return this.color(this.backCompound, color);
    }

    private SignBuilder color(NbtCompound compound, String color) {
        compound.putString(TagsConstant.SIGN_COLOR, color);
        return this;
    }

    public SignBuilder wax() {
        this.isWaxed = true;
        return this;
    }

    public ItemStack get() {
        NbtCompound entityTag = new NbtCompound();

        this.frontCompound.put(TagsConstant.SIGN_MESSAGES, this.frontTextList);
        this.backCompound.put(TagsConstant.SIGN_MESSAGES, this.backTextList);
        entityTag.put(TagsConstant.SIGN_FRONT_TEXT, this.frontCompound);
        entityTag.put(TagsConstant.SIGN_BACK_TEXT, this.backCompound);
        entityTag.putBoolean(TagsConstant.SIGN_IS_WAXED, this.isWaxed);

        this.stack.setSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY, entityTag);
        return this.stack;
    }
}
