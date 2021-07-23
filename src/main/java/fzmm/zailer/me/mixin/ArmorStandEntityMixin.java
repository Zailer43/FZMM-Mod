package fzmm.zailer.me.mixin;

import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ArmorStandEntity.class)
public abstract class ArmorStandEntityMixin {

	@Shadow
	public abstract boolean isSmall();

	@Shadow
	public abstract boolean shouldShowArms();

	@Shadow
	public abstract boolean shouldHideBasePlate();

	@Shadow
	private NbtCompound poseToNbt() {
		return null;
	}

	@Shadow
	@Final
	private DefaultedList<ItemStack> armorItems;
	@Shadow
	@Final
	private DefaultedList<ItemStack> heldItems;

	/**
	 * @reason Get the armor stand item with NBT if Ctrl is pressed
	 * @author Zailer43
	 */
	@Overwrite
	public ItemStack getPickBlockStack() {
		ItemStack stack = Items.ARMOR_STAND.getDefaultStack();
		if (Screen.hasControlDown()) {
			NbtCompound entityTag = new NbtCompound();
			NbtList armorNbt = new NbtList(),
				handNbt = new NbtList();
			DefaultedList<ItemStack> armor = this.armorItems,
				hand = this.heldItems;
			NbtCompound display = stack.getOrCreateSubTag("display");
			NbtList lore = display.getList("Lore", 8);

			lore.add(FzmmUtils.generateLoreMessage("(EntityTag)"));
			display.put("Lore", lore);

			FzmmUtils.defaultListToNbtList(armor, armorNbt);
			FzmmUtils.defaultListToNbtList(hand, handNbt);

			entityTag.put("Pose", this.poseToNbt());
			entityTag.put("ArmorItems", armorNbt);
			entityTag.put("HandItems", handNbt);
			entityTag.putBoolean("Small", this.isSmall());
			entityTag.putBoolean("ShowArms", this.shouldShowArms());
			//entityTag.putBoolean("Invisible", this.isInvisible()); //TODO: Problemas de compatibilidad con otros mods
			//entityTag.putBoolean("NoGravity", this.hasNoGravity());
			entityTag.putBoolean("NoBasePlate", this.shouldHideBasePlate());

			stack.putSubTag("EntityTag", entityTag);
			stack.putSubTag("display", display);
		}
		return stack;
	}
}
