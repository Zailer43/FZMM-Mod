package fzmm.zailer.me.builders;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import fzmm.zailer.me.client.gui.converters.tabs.ConverterBase64Tab;
import fzmm.zailer.me.client.logic.FzmmHistory;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PlayerHeadItem;
import net.minecraft.nbt.*;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class HeadBuilder {

    private ItemStack stack;
    private String skinValue;
    @Nullable
    private String headName;
    @Nullable
    private String signature;
    @Nullable
    private UUID id;
    @Nullable
    private Identifier noteBlockSound;
    private boolean addToHeadHistory;
    private boolean isUnloadedHead;

    private HeadBuilder() {
        this.stack = new ItemStack(Items.PLAYER_HEAD);
        this.skinValue = "";
        this.headName = null;
        this.id = null;
        this.noteBlockSound = null;
        this.addToHeadHistory = true;
        this.isUnloadedHead = true;
    }

    public static HeadBuilder builder() {
        return new HeadBuilder();
    }

    public ItemStack get() {
        if (this.isUnloadedHead)
            return this.stack;

        NbtList textures = new NbtList();
        NbtCompound value = new NbtCompound();
        NbtCompound properties = new NbtCompound();
        NbtCompound skullOwner = new NbtCompound();
        NbtCompound nbt = this.stack.getOrCreateNbt();

        if (!this.skinValue.isEmpty()) {
            value.putString(TagsConstant.HEAD_TEXTURE_VALUE, this.skinValue);
            textures.add(value);
            properties.put(TagsConstant.HEAD_PROPERTIES_TEXTURES, textures);
            skullOwner.put(TagsConstant.HEAD_SKULL_OWNER_PROPERTIES, properties);
        }

        UUID uuid = this.id == null ? UUID.randomUUID() : this.id;

        skullOwner.putUuid(TagsConstant.HEAD_SKULL_OWNER_ID, uuid);

        if (this.headName != null && !this.headName.isEmpty())
            skullOwner.putString(TagsConstant.HEAD_SKULL_OWNER_NAME, this.headName);
        else
            skullOwner.remove(TagsConstant.HEAD_SKULL_OWNER_NAME);

        if (skullOwner.getSize() > 1)
            nbt.put(PlayerHeadItem.SKULL_OWNER_KEY, skullOwner);
        else
            nbt.remove(PlayerHeadItem.SKULL_OWNER_KEY);

        NbtCompound blockEntityTag = nbt.getCompound(TagsConstant.BLOCK_ENTITY);
        if (this.noteBlockSound != null && !this.noteBlockSound.getPath().isEmpty()) {
            blockEntityTag.putString(SkullBlockEntity.NOTE_BLOCK_SOUND_KEY, this.noteBlockSound.toString());
            nbt.put(TagsConstant.BLOCK_ENTITY, blockEntityTag);
        }
        else if (blockEntityTag.isEmpty())
            nbt.remove(TagsConstant.BLOCK_ENTITY);

        this.stack.setNbt(nbt.isEmpty() ? null : nbt);
        if (this.addToHeadHistory)
            FzmmHistory.addGeneratedHeads(this.stack);

        return this.stack;
    }

    public HeadBuilder skinValue(String skinValue) {
        boolean isValid = false;

        try {
            String jsonStr = ConverterBase64Tab.decode(skinValue);
            Gson gson = new Gson();
            isValid = !gson.fromJson(jsonStr, JsonObject.class).isEmpty();
        } catch (Exception ignored) {
        }

        this.skinValue = isValid ? skinValue : "";

        this.updateUnloadedHead(this.skinValue.isEmpty() ? null : this.skinValue);
        return this;
    }

    public String skinValue() {
        return this.skinValue;
    }

    public HeadBuilder skinUrl(String skinUrl) {
        Gson gson = new Gson();
        JsonObject jsonObject = null;

        try {
            String jsonStr = ConverterBase64Tab.decode(this.skinValue);
            jsonObject = gson.fromJson(jsonStr, JsonObject.class);
        } catch (Exception ignored) {
        }

        if (jsonObject == null)
            jsonObject = new JsonObject();

        JsonObject texturesObject = jsonObject.has("textures") ?
                jsonObject.getAsJsonObject("textures") : new JsonObject();

        JsonObject skinObject = texturesObject.has("SKIN") ?
                texturesObject.getAsJsonObject("SKIN") : new JsonObject();

        skinObject.addProperty("url", skinUrl);
        texturesObject.add("SKIN", skinObject);
        jsonObject.add("textures", texturesObject);

        try {
            this.skinValue = ConverterBase64Tab.encode(jsonObject.toString());
        } catch (Exception ignored) {
        }

        this.updateUnloadedHead(this.skinValue);

        return this;
    }


    public String skinUrl() {
        String result = "";
        if (this.skinValue != null && !this.skinValue.isEmpty()) {
            try {
                String jsonStr = ConverterBase64Tab.decode(this.skinValue);
                Gson gson = new Gson();
                JsonObject jsonObject = gson.fromJson(jsonStr, JsonObject.class);
                if (jsonObject.has("textures")) {
                    JsonObject texturesObject = jsonObject.getAsJsonObject("textures");
                    if (texturesObject.has("SKIN")) {
                        JsonObject skinObject = texturesObject.getAsJsonObject("SKIN");
                        if (skinObject.has("url"))
                            result = skinObject.get("url").getAsString();
                    }
                }

            } catch (Exception ignored) {
            }
        }
        return result;
    }

    public HeadBuilder headName(String headName) {
        this.headName = headName.isEmpty() ? null : headName;

        this.updateUnloadedHead(this.headName);
        return this;
    }

    public String headName() {
        return this.headName;
    }

    public HeadBuilder signature(String signature) {
        this.signature = signature.isEmpty() ? null : signature;

        this.updateUnloadedHead(this.signature);
        return this;
    }

    public String signature() {
        return this.signature;
    }

    public HeadBuilder id(@Nullable UUID id) {
        this.id = id;

        this.updateUnloadedHead(this.id);
        return this;
    }

    public HeadBuilder id(NbtIntArray id) {
        try {
            this.id = NbtHelper.toUuid(id);
        } catch (IllegalArgumentException ignored) {
            this.id = null;
        }

        this.updateUnloadedHead(this.id);

        return this;
    }

    public Optional<NbtIntArray> idArray() {
        if (this.id == null)
            return Optional.empty();

        return Optional.of(NbtHelper.fromUuid(this.id));
    }

    public Optional<UUID> idUuid() {
        if (this.id == null)
            return Optional.empty();

        return Optional.of(this.id);
    }

    public HeadBuilder notAddToHistory() {
        this.addToHeadHistory = false;
        return this;
    }

    public HeadBuilder noteBlockSound(String noteBlockSound) {
        return this.noteBlockSound(noteBlockSound.isEmpty() ? null : Identifier.tryParse(noteBlockSound));
    }

    public HeadBuilder noteBlockSound(@Nullable Identifier noteBlockSound) {
        this.noteBlockSound = noteBlockSound;

        this.updateUnloadedHead(this.noteBlockSound);
        return this;
    }

    public Optional<Identifier> noteBlockSound() {
        return Optional.ofNullable(this.noteBlockSound);
    }

    public HeadBuilder setUnloadedHead(boolean value) {
        this.isUnloadedHead = value;
        return this;
    }

    private void updateUnloadedHead(@Nullable Object value) {
        if (this.isUnloadedHead)
            this.isUnloadedHead = value == null;
    }

    public HeadBuilder of(ItemStack stack) {
        this.stack = stack.copy();

        if (!this.stack.hasNbt())
            return this;

        NbtCompound nbt = this.stack.getNbt();

        assert nbt != null;
        if (nbt.contains(PlayerHeadItem.SKULL_OWNER_KEY, NbtElement.STRING_TYPE)) {
            this.headName(nbt.getString(PlayerHeadItem.SKULL_OWNER_KEY));
            this.isUnloadedHead = true;
            return this;
        }

        NbtCompound skullOwner = nbt.getCompound(PlayerHeadItem.SKULL_OWNER_KEY);

        this.noteBlockSound(nbt.getString(SkullBlockEntity.NOTE_BLOCK_SOUND_KEY))
            .headName(skullOwner.getString(TagsConstant.HEAD_SKULL_OWNER_NAME));

        if (skullOwner.containsUuid(TagsConstant.HEAD_SKULL_OWNER_ID))
            this.id(skullOwner.getUuid(TagsConstant.HEAD_SKULL_OWNER_ID));

        NbtCompound properties = skullOwner.getCompound(TagsConstant.HEAD_SKULL_OWNER_PROPERTIES);
        NbtList textures = properties.getList(TagsConstant.HEAD_PROPERTIES_TEXTURES, NbtElement.COMPOUND_TYPE);
        if (!textures.isEmpty() && textures.get(0) instanceof NbtCompound texture)
            this.skinValue(texture.getString(TagsConstant.HEAD_TEXTURE_VALUE));

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
