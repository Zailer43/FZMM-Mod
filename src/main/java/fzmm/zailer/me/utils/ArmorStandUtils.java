package fzmm.zailer.me.utils;

import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;

public class ArmorStandUtils {

    private final NbtCompound entityTag;

    public ArmorStandUtils() {
        this.entityTag = new NbtCompound();
    }

    public NbtCompound getItemNbt(@Nullable String itemName) {
        NbtCompound tag = new NbtCompound();

        if (itemName != null) {
            NbtCompound display = new NbtCompound();

            display.putString(ItemStack.NAME_KEY, itemName);
            tag.put(ItemStack.DISPLAY_KEY, display);
        }
        tag.put(EntityType.ENTITY_TAG_KEY, entityTag);
        return tag;
    }

    public ArmorStandUtils setAsHologram(String name) {
        this.setImmutableAndInvisible();
        this.entityTag.putString("CustomName", name);
        this.entityTag.putBoolean("CustomNameVisible", true);
        return this;
    }

    public ArmorStandUtils setImmutableAndInvisible() {
        this.entityTag.putInt("DisabledSlots", 4144959);
        this.entityTag.putBoolean("NoGravity", true);
        this.entityTag.putBoolean("Invisible", true);
        return this;
    }

    public ArmorStandUtils setPos(double x, double y, double z) {
        NbtList coordinates = new NbtList();
        coordinates.add(NbtDouble.of(x));
        coordinates.add(NbtDouble.of(y));
        coordinates.add(NbtDouble.of(z));
        this.setPos(coordinates);
        return this;
    }

    public ArmorStandUtils setPos(NbtList coordinates) {
        entityTag.put("Pos", coordinates);
        return this;
    }

    public ArmorStandUtils setRightHandItem(ItemStack stack) {
        return setRightHandItem(stack.getItem().toString(), (byte) stack.getCount(), stack.getNbt());
    }

    public ArmorStandUtils setRightHandItem(String id, byte count, @Nullable NbtCompound tag) {
        NbtList handItem = new NbtList();
        NbtCompound itemTag = new NbtCompound();

        itemTag.putString("id", id);
        itemTag.putByte("Count", count);
        if (tag != null && !tag.isEmpty()) {
            itemTag.put("tag", tag);
        }
        handItem.add(itemTag);

        entityTag.put("HandItems", handItem);
        return this;
    }

    public ArmorStandUtils setTags(String... tagsList) {
        NbtList tags = new NbtList();

        for (String tag : tagsList) {
            tags.add(NbtString.of(tag));
        }

        entityTag.put("Tags", tags);
        return this;
    }
}
