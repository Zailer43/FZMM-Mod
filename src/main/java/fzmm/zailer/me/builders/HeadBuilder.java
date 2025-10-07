package fzmm.zailer.me.builders;

import com.mojang.authlib.GameProfile;
import fzmm.zailer.me.client.logic.history.FzmmHistory;
import fzmm.zailer.me.utils.ItemUtils;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class HeadBuilder {

    private String skinValue;
    @Nullable
    private String headName;
    @Nullable
    private String signature;
    private UUID uuid;
    private boolean addToHeadHistory;

    private HeadBuilder() {
        this.skinValue = "";
        this.headName = null;
        this.addToHeadHistory = true;
        this.uuid = UUID.randomUUID();
    }

    public static HeadBuilder builder() {
        return new HeadBuilder();
    }

    public ItemStack get() {
        NbtList textures = new NbtList();
        NbtCompound value = new NbtCompound();
        NbtCompound properties = new NbtCompound();
        NbtCompound skullOwner = new NbtCompound();
        NbtCompound tag = new NbtCompound();

        value.putString("Value", this.skinValue);
        textures.add(value);
        properties.put(TagsConstant.HEAD_PROPERTIES_TEXTURES, textures);

        skullOwner.put(TagsConstant.HEAD_PROPERTIES, properties);
        skullOwner.putUuid("Id", this.uuid);

        if (this.headName != null)
            skullOwner.putString("Name", this.headName);

        tag.put(PlayerHeadItem.SKULL_OWNER_KEY, skullOwner);

        ItemStack stack = Items.PLAYER_HEAD.getDefaultStack();
        stack.setNbt(tag);
        stack = ItemUtils.process(stack);

        if (this.addToHeadHistory)
            FzmmHistory.addGeneratedHeads(stack);
        return stack;
    }

    public HeadBuilder skinValue(String skinValue) {
        this.skinValue = skinValue;
        return this;
    }

    public HeadBuilder headName(@Nullable String headName) {
        this.headName = headName;
        return this;
    }

    public HeadBuilder signature(@Nullable String signature) {
        this.signature = signature;
        return this;
    }

    public HeadBuilder id(UUID id) {
        this.uuid = id;
        return this;
    }

    public HeadBuilder notAddToHistory() {
        this.addToHeadHistory = false;
        return this;
    }

    public static ItemStack of(String username) {
        ItemStack head = Items.PLAYER_HEAD.getDefaultStack();
        head.setSubNbt(PlayerHeadItem.SKULL_OWNER_KEY, NbtString.of(username));
        head = ItemUtils.process(head);

        FzmmHistory.addGeneratedHeads(head);
        return head;
    }

    public static ItemStack of(GameProfile profile) {
        ItemStack head = Items.PLAYER_HEAD.getDefaultStack();
        NbtCompound skullOwner = new NbtCompound();

        NbtHelper.writeGameProfile(skullOwner, profile);
        head.setSubNbt(PlayerHeadItem.SKULL_OWNER_KEY, skullOwner);
        head = ItemUtils.process(head);

        FzmmHistory.addGeneratedHeads(head);
        return head;
    }
}
