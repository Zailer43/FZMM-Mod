package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SignBuilder {

    public static final int MAX_ROWS = 4;
    private ItemStack stack;
    private String color;
    private boolean glowing;
    private final NbtList textList;

    private SignBuilder() {
        this.stack = Items.OAK_SIGN.getDefaultStack();
        this.color = Formatting.BLACK.getName();
        this.glowing = false;
        this.textList = new NbtList();
    }

    public static SignBuilder builder() {
        return new SignBuilder();
    }


    public SignBuilder item(Item item) {
        this.stack = item.getDefaultStack();
        return this;
    }

    public SignBuilder color(String color) {
        this.color = color;
        return this;
    }

    public SignBuilder glowing(boolean value) {
        this.glowing = value;
        return this;
    }

    public SignBuilder addLine(NbtString nbtString) {
        this.textList.add(nbtString);
        return this;
    }

    public SignBuilder addLine(NbtString nbtString, int expectedWidth) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        MutableText text = Text.Serializer.fromJson(nbtString.asString());
        if (text == null)
            return this;

        while (textRenderer.getWidth(text) < expectedWidth)
            text.append(" ");

        nbtString = FzmmUtils.toNbtString(text, false);
        return this.addLine(nbtString);
    }

    public ItemStack get() {
        NbtCompound entityTag = new NbtCompound();

        entityTag.putString(TagsConstant.SIGN_COLOR, this.color);
        entityTag.putBoolean(TagsConstant.SIGN_GLOWING_TEXT, this.glowing);

        int rowCount = Math.min(MAX_ROWS, this.textList.size());
        for (int i = 0; i != rowCount; i++)
            entityTag.put("Text" + (i + 1), this.textList.get(i));

        this.stack.setSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY, entityTag);
        return this.stack;
    }
}
