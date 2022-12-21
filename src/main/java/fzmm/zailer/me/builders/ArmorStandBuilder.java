package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ArmorStandBuilder {

    private final NbtCompound entityTag;

    private ArmorStandBuilder() {
        this.entityTag = new NbtCompound();
    }

    public static ArmorStandBuilder builder() {
        return new ArmorStandBuilder();
    }

    public NbtCompound getItemNbt(@Nullable Text itemName) {
        NbtCompound tag = new NbtCompound();

        if (itemName != null) {
            NbtCompound display = new NbtCompound();
            display.put(ItemStack.NAME_KEY, FzmmUtils.toNbtString(itemName.getString(), true));
            tag.put(ItemStack.DISPLAY_KEY, display);
        }

        tag.put(EntityType.ENTITY_TAG_KEY, entityTag);
        return tag;
    }

    public ItemStack getItem(@Nullable String itemName) {
        return this.getItem(itemName == null ? null : Text.of(itemName));
    }

    public ItemStack getItem(@Nullable Text itemName) {
        ItemStack armorStand = new ItemStack(Items.ARMOR_STAND);
        armorStand.setNbt(this.getItemNbt(itemName));
        return armorStand;
    }

    public ArmorStandBuilder setAsHologram(String name) {
        this.setImmutableAndInvisible();
        this.entityTag.putString("CustomName", name);
        this.entityTag.putBoolean("CustomNameVisible", true);
        return this;
    }

    public ArmorStandBuilder setImmutableAndInvisible() {
        this.entityTag.putInt("DisabledSlots", 4144959);
        this.entityTag.putBoolean("NoGravity", true);
        this.entityTag.putBoolean("Invisible", true);
        return this;
    }

    public ArmorStandBuilder setPos(double x, double y, double z) {
        NbtList coordinates = new NbtList();
        coordinates.add(NbtDouble.of(x));
        coordinates.add(NbtDouble.of(y));
        coordinates.add(NbtDouble.of(z));
        this.setPos(coordinates);
        return this;
    }

    public ArmorStandBuilder setPos(NbtList coordinates) {
        this.entityTag.put("Pos", coordinates);
        return this;
    }

    public ArmorStandBuilder setRightHandItem(ItemStack stack) {
        NbtList handItem = new NbtList();
        NbtCompound itemTag = stack.writeNbt(new NbtCompound());
        handItem.add(itemTag);

        this.entityTag.put("HandItems", handItem);
        return this;
    }

    public ArmorStandBuilder setRightArmPose(Vector3f pos) {
        NbtList armPose = new NbtList();
        NbtCompound pose = new NbtCompound();

        armPose.add(NbtFloat.of(pos.x()));
        armPose.add(NbtFloat.of(pos.y()));
        armPose.add(NbtFloat.of(pos.z()));

        pose.put("RightArm", armPose);
        this.entityTag.put("Pose", pose);
        return this;
    }

    public ArmorStandBuilder setTags(String... tagsList) {
        NbtList tags = new NbtList();

        for (String tag : tagsList) {
            tags.add(NbtString.of(tag));
        }

        this.entityTag.put("Tags", tags);
        return this;
    }

    public ArmorStandBuilder setShowArms() {
        this.entityTag.putBoolean("ShowArms", true);
        return this;
    }

    public ArmorStandBuilder setSmall() {
        this.entityTag.putBoolean("Small", true);
        return this;
    }
}
