package fzmm.zailer.me.builders;

import com.mojang.authlib.GameProfile;
import fzmm.zailer.me.client.gui.converters.tabs.ConverterUuidToArrayTab;
import fzmm.zailer.me.client.logic.FzmmHistory;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.nbt.*;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class HeadBuilder {

    private String skinValue;
    @Nullable
    private String headName;
    @Nullable
    private String signature;
    private NbtIntArray id;
    private boolean addToHeadHistory;

    private HeadBuilder() {
        this.skinValue = "";
        this.headName = null;
        this.addToHeadHistory = true;

        Random random = Random.create();
        this.id = new NbtIntArray(new int[]{
                random.nextInt(Integer.MAX_VALUE),
                random.nextInt(Integer.MAX_VALUE),
                random.nextInt(Integer.MAX_VALUE),
                random.nextInt(Integer.MAX_VALUE)}
        );
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


        // this is required since 1.20.2 to avoid errors in the logs,
        // although when included it still gives error,
        // and it seems that this must be used for the skin reporting system.
//        if (this.signature != null)
//            properties.putString(TagsConstant.HEAD__PROPERTIES_SIGNATURE, this.signature);

        skullOwner.put(TagsConstant.HEAD_PROPERTIES, properties);
        skullOwner.put("Id", this.id);

        if (this.headName != null)
            skullOwner.putString("Name", this.headName);


        tag.put(PlayerHeadItem.SKULL_OWNER_KEY, skullOwner);

        ItemStack stack = Items.PLAYER_HEAD.getDefaultStack();
        stack.setNbt(tag);
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
        this.id = new NbtIntArray(ConverterUuidToArrayTab.UUIDtoArray(id));
        return this;
    }

    public HeadBuilder notAddToHistory() {
        this.addToHeadHistory = false;
        return this;
    }

    public static ItemStack of(String username) {
        ItemStack head = Items.PLAYER_HEAD.getDefaultStack();
        head.setSubNbt(PlayerHeadItem.SKULL_OWNER_KEY, NbtString.of(username));

        FzmmHistory.addGeneratedHeads(head);
        return head;
    }

    public static ItemStack of(GameProfile profile) {
        ItemStack head = Items.PLAYER_HEAD.getDefaultStack();
        NbtCompound skullOwner = new NbtCompound();

        NbtHelper.writeGameProfile(skullOwner, profile);
        head.setSubNbt(PlayerHeadItem.SKULL_OWNER_KEY, skullOwner);

        FzmmHistory.addGeneratedHeads(head);
        return head;
    }
}
