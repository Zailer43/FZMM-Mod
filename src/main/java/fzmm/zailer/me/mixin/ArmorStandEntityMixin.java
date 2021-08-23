package fzmm.zailer.me.mixin;

import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin {

	@Shadow public abstract void writeCustomDataToNbt(NbtCompound nbt);

	/**
	 * @reason Get the armor stand item with NBT if Ctrl is pressed
	 * @author Zailer43
	 */
	@Overwrite
	public ItemStack getPickBlockStack() {
		ItemStack stack = Items.ARMOR_STAND.getDefaultStack();
		if (Screen.hasControlDown()) {
			NbtCompound entityTag = new NbtCompound();

			this.writeCustomDataToNbt(entityTag);
			// FIXME: DisabledSlots and NoGravity do not work

			stack.setSubNbt(EntityType.ENTITY_TAG_KEY, entityTag);
			stack.setSubNbt(ItemStack.DISPLAY_KEY, FzmmUtils.generateLoreMessage("(" + EntityType.ENTITY_TAG_KEY + ")"));
		}
		return stack;
	}
}
