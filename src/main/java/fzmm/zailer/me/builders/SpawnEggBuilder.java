package fzmm.zailer.me.builders;

import fzmm.zailer.me.utils.TagsConstant;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.TypedEntityData;

public class SpawnEggBuilder {

    private ItemStack stack;
    private EntityType<?> entityType;
    private CompoundTag entityTag;

    private SpawnEggBuilder() {
        this.stack = Items.BAT_SPAWN_EGG.getDefaultInstance();
        this.entityType = EntityTypes.BAT;
        this.entityTag = new CompoundTag();
    }

    public static SpawnEggBuilder builder() {
        return new SpawnEggBuilder();
    }


    public SpawnEggBuilder item(Item item) {
        this.stack = item.getDefaultInstance();
        return this;
    }

    public SpawnEggBuilder entityType(EntityType<?> entityType) {
        this.entityType = entityType;
        return this;
    }

    public SpawnEggBuilder entityTag(CompoundTag entityTag) {
        this.entityTag = entityTag;
        return this;
    }

    public ItemStack get() {
        this.entityTag.putString(TagsConstant.ENTITY_TAG_ID, BuiltInRegistries.ENTITY_TYPE.getKey(this.entityType).getPath());
        this.stack.update(DataComponents.ENTITY_DATA, null,
                entityData -> TypedEntityData.of(this.entityType, this.entityTag)
        );

        return this.stack;
    }
}
