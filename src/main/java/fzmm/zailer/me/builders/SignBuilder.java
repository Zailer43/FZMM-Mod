package fzmm.zailer.me.builders;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.item.HangingSignItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import net.minecraft.util.ErrorReporter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SignBuilder {

    public static final int MAX_ROWS = 4;
    private ItemStack stack;
    private final List<Text> frontTextList;
    private final NbtCompound frontCompound;
    private final List<Text> backTextList;
    private final NbtCompound backCompound;
    private boolean isWaxed;

    private SignBuilder() {
        this.stack = Items.OAK_SIGN.getDefaultStack();
        this.frontTextList = new ArrayList<>();
        this.frontCompound = new NbtCompound();
        this.backTextList = new ArrayList<>();
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

    public SignBuilder addFrontLine(Text text, int expectedWidth) {
        return this.addLine(this.frontTextList, text, expectedWidth);
    }

    public SignBuilder addBackLine(Text text, int expectedWidth) {
        return this.addLine(this.backTextList, text, expectedWidth);
    }

    private SignBuilder addLine(List<Text> list, Text text, int expectedWidth) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        assert MinecraftClient.getInstance().player != null;
        if (text == null) {
            return this;
        }

        MutableText textCopy = text.copy();

        int spaceCount = 0;
        while (textRenderer.getWidth(textCopy) < expectedWidth) {
            textCopy.append(" ");
            spaceCount++;
        }

        list.add(text.copy().append(" ".repeat(spaceCount)));

        return this;
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
        this.stack.apply(DataComponentTypes.BLOCK_ENTITY_DATA, NbtComponent.DEFAULT, component -> {
            NbtCompound result = component.copyNbt();

            try (var logging = new ErrorReporter.Logging(FzmmClient.LOGGER)) {
                NbtWriteView nbtWriteView = NbtWriteView.create(logging, FzmmUtils.getRegistryManager());
                BlockEntity.writeId(nbtWriteView, this.isHangingSign() ? BlockEntityType.HANGING_SIGN : BlockEntityType.SIGN); // 1.21.4+

                result.copyFrom(nbtWriteView.getNbt());
            }

            this.addSignMessage(this.frontTextList, this.frontCompound, result, TagsConstant.SIGN_FRONT_TEXT);
            this.addSignMessage(this.backTextList, this.backCompound, result, TagsConstant.SIGN_BACK_TEXT);

            result.putBoolean(TagsConstant.SIGN_IS_WAXED, this.isWaxed);

            return NbtComponent.of(result);
        });
        return this.stack;
    }

    public boolean isHangingSign() {
        return this.stack.getItem() instanceof HangingSignItem;
    }

    private void addSignMessage(List<Text> list, NbtCompound compound, NbtCompound blockEntityTag, String key) {
        if (list.isEmpty()) {
            return;
        }

        while (list.size() < 4) {
            list.add(Text.empty());
        }

        NbtList listTag = new NbtList();
        listTag.addAll(
                list.stream()
                        .map(text -> TextCodecs.CODEC.encodeStart(NbtOps.INSTANCE, text).result())
                        .filter(nbtOptional -> {
                            if (nbtOptional.isEmpty()) {
                                FzmmClient.LOGGER.warn("[SignBuilder] Failed to encode text");
                                return false;
                            } else {
                                return true;
                            }
                        })
                        .map(Optional::get)
                        .toList()
        );

        compound.put(TagsConstant.SIGN_MESSAGES, listTag);
        blockEntityTag.put(key, compound);
    }
}
