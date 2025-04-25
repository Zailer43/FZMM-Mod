package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.NbtComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.text.TextCodecs;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ArmorStandBuilder {

    private final NbtCompound entityTag;

    private ArmorStandBuilder() {
        this.entityTag = new NbtCompound();
        this.entityTag.putString(TagsConstant.ENTITY_TAG_ID, Registries.ENTITY_TYPE.getId(EntityType.ARMOR_STAND).getPath());
    }

    public static ArmorStandBuilder builder() {
        return new ArmorStandBuilder();
    }

    public ItemStack getItem(@Nullable String itemName) {
        return this.getItem(itemName == null ? null : Text.of(itemName));
    }

    public ItemStack getItem(@Nullable Text itemName) {
        ItemStack armorStand = new ItemStack(Items.ARMOR_STAND);

        armorStand.apply(DataComponentTypes.CUSTOM_NAME, null, component -> itemName);
        armorStand.apply(DataComponentTypes.ENTITY_DATA, null, component -> NbtComponent.of(this.entityTag));
        return armorStand;
    }

    public ArmorStandBuilder setAsHologram(Text name) {
        this.setImmutableAndInvisible();
        this.entityTag.put("CustomName", TextCodecs.CODEC, name);
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
        NbtCompound equipmentTag = new NbtCompound();
        equipmentTag.put(EquipmentSlot.MAINHAND.getName(), stack.toNbt(FzmmUtils.getRegistryManager()));

        this.entityTag.put("equipment", equipmentTag);
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

        this.entityTag.put(TagsConstant.ENTITY_TAG_TAGS_ID, tags);
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
