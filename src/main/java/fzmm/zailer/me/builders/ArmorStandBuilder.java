package fzmm.zailer.me.builders;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.utils.ItemUtils;
import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public class ArmorStandBuilder {

    private final CompoundTag entityTag;

    private ArmorStandBuilder() {
        this.entityTag = new CompoundTag();
        this.entityTag.putString(TagsConstant.ENTITY_TAG_ID, BuiltInRegistries.ENTITY_TYPE.getKey(EntityType.ARMOR_STAND).getPath());
    }

    public static ArmorStandBuilder builder() {
        return new ArmorStandBuilder();
    }

    public ItemStack getItem(@Nullable String itemName) {
        return this.getItem(itemName == null ? null : Component.nullToEmpty(itemName));
    }

    public ItemStack getItem(@Nullable Component itemName) {
        ItemStack armorStand = new ItemStack(Items.ARMOR_STAND);

        armorStand.update(DataComponents.CUSTOM_NAME, null, component -> itemName);
        armorStand.update(DataComponents.ENTITY_DATA, null, entityData ->
                TypedEntityData.of(EntityType.ARMOR_STAND, this.entityTag)
        );
        return armorStand;
    }

    public ArmorStandBuilder setAsHologram(Component name) {
        this.setImmutableAndInvisible();
        this.entityTag.store("CustomName", ComponentSerialization.CODEC, name);
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
        ListTag coordinates = new ListTag();
        coordinates.add(DoubleTag.valueOf(x));
        coordinates.add(DoubleTag.valueOf(y));
        coordinates.add(DoubleTag.valueOf(z));
        this.setPos(coordinates);
        return this;
    }

    public ArmorStandBuilder setPos(ListTag coordinates) {
        this.entityTag.put("Pos", coordinates);
        return this;
    }

    public ArmorStandBuilder setRightHandItem(ItemStack stack) {
        CompoundTag equipmentTag = new CompoundTag();
        ItemUtils.encodeToNbt(stack).result().ifPresentOrElse(
                nbtElement -> equipmentTag.put(EquipmentSlot.MAINHAND.getName(), nbtElement),
                () -> FzmmClient.LOGGER.warn("[ArmorStandBuilder] Failed to encode item for armor stand")
        );

        this.entityTag.put("equipment", equipmentTag);
        return this;
    }

    public ArmorStandBuilder setRightArmPose(Vector3f pos) {
        ListTag armPose = new ListTag();
        CompoundTag pose = new CompoundTag();

        armPose.add(FloatTag.valueOf(pos.x()));
        armPose.add(FloatTag.valueOf(pos.y()));
        armPose.add(FloatTag.valueOf(pos.z()));

        pose.put("RightArm", armPose);
        this.entityTag.put("Pose", pose);
        return this;
    }

    public ArmorStandBuilder setTags(String... tagsList) {
        ListTag tags = new ListTag();

        for (String tag : tagsList) {
            tags.add(StringTag.valueOf(tag));
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
