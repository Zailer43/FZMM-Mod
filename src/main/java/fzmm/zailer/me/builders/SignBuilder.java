package fzmm.zailer.me.builders;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.HangingSignItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntityTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SignBuilder {

    public static final int MAX_ROWS = 4;
    private ItemStack stack;
    private final List<Component> frontTextList;
    private final CompoundTag frontCompound;
    private final List<Component> backTextList;
    private final CompoundTag backCompound;
    private boolean isWaxed;

    private SignBuilder() {
        this.stack = Items.OAK_SIGN.getDefaultInstance();
        this.frontTextList = new ArrayList<>();
        this.frontCompound = new CompoundTag();
        this.backTextList = new ArrayList<>();
        this.backCompound = new CompoundTag();
        this.isWaxed = false;
    }

    public static SignBuilder builder() {
        return new SignBuilder();
    }


    public SignBuilder item(Item item) {
        this.stack = item.getDefaultInstance();
        return this;
    }

    public SignBuilder addFrontLine(Component text, int expectedWidth) {
        return this.addLine(this.frontTextList, text, expectedWidth);
    }

    public SignBuilder addBackLine(Component text, int expectedWidth) {
        return this.addLine(this.backTextList, text, expectedWidth);
    }

    private SignBuilder addLine(List<Component> list, Component text, int expectedWidth) {
        Font textRenderer = Minecraft.getInstance().font;

        assert Minecraft.getInstance().player != null;
        if (text == null) {
            return this;
        }

        MutableComponent textCopy = text.copy();

        int spaceCount = 0;
        while (textRenderer.width(textCopy) < expectedWidth) {
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

    private SignBuilder glowing(CompoundTag compound) {
        compound.putBoolean(TagsConstant.SIGN_GLOWING_TEXT, true);
        return this;
    }

    public SignBuilder colorFront(String color) {
        return this.color(this.frontCompound, color);
    }

    public SignBuilder colorBack(String color) {
        return this.color(this.backCompound, color);
    }

    private SignBuilder color(CompoundTag compound, String color) {
        compound.putString(TagsConstant.SIGN_COLOR, color);
        return this;
    }

    public SignBuilder wax() {
        this.isWaxed = true;
        return this;
    }

    public ItemStack get() {
        BlockEntityType<?> type = this.isHangingSign() ? BlockEntityTypes.HANGING_SIGN : BlockEntityTypes.SIGN;

        this.stack.update(DataComponents.BLOCK_ENTITY_DATA, TypedEntityData.of(type, new CompoundTag()), entityData -> {
            CompoundTag result = entityData.copyTagWithoutId();

            this.addSignMessage(this.frontTextList, this.frontCompound, result, TagsConstant.SIGN_FRONT_TEXT);
            this.addSignMessage(this.backTextList, this.backCompound, result, TagsConstant.SIGN_BACK_TEXT);

            result.putBoolean(TagsConstant.SIGN_IS_WAXED, this.isWaxed);

            return TypedEntityData.of(type, result);
        });
        return this.stack;
    }

    public boolean isHangingSign() {
        return this.stack.getItem() instanceof HangingSignItem;
    }

    private void addSignMessage(List<Component> list, CompoundTag compound, CompoundTag blockEntityTag, String key) {
        if (list.isEmpty()) {
            return;
        }

        while (list.size() < 4) {
            list.add(Component.empty());
        }

        ListTag listTag = new ListTag();
        listTag.addAll(
                list.stream()
                        .map(text -> ComponentSerialization.CODEC.encodeStart(FzmmUtils.getRegistryOps(NbtOps.INSTANCE), text).result())
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
