package fzmm.zailer.me.mixin;

import fzmm.zailer.me.client.keys.FzmmGuiKey;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BannerItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SkullItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {

	@Shadow
	@Final
	private static final Logger LOGGER = LogManager.getLogger();
	@Shadow
	@Nullable
	public ClientWorld world;
	@Shadow
	public ClientPlayerEntity player;
	@Shadow
	public HitResult crosshairTarget;
	@Shadow
	public ClientPlayerInteractionManager interactionManager;

	@Inject(method = "handleInputEvents", at = @At("HEAD"))
	public void handleInputEvents(CallbackInfo ci) {
		FzmmGuiKey.handleInputEvents();
	}

	@Redirect(method = "handleInputEvents", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;doItemPick()V"))
	private void doItemPick(MinecraftClient minecraftClient) {
		if (crosshairTarget == null) {
			return;
		}
		assert this.world != null;
		boolean isCreative = this.player.getAbilities().creativeMode;
		HitResult.Type type = this.crosshairTarget.getType();
		ItemStack stack = Items.STONE.getDefaultStack();

		switch (type) {
			case BLOCK -> {
				BlockPos blockPos = ((BlockHitResult) this.crosshairTarget).getBlockPos();
				BlockState blockState = this.world.getBlockState(blockPos);
				if (blockState.isAir()) {
					return;
				}

				Block block = blockState.getBlock();
				stack = block.getPickStack(this.world, blockPos, blockState);
				PlayerInventory playerInventory = this.player.getInventory();

				int i = playerInventory.getSlotWithStack(stack);
				if ((!isCreative || !Screen.hasControlDown()) && i != -1) {
					if (PlayerInventory.isValidHotbarIndex(i)) {
						playerInventory.selectedSlot = i;
					} else {
						this.interactionManager.pickFromInventory(i);
					}
					return;
				}
				if (Screen.hasControlDown()) {
					if (blockState.hasBlockEntity() && !(stack.getItem() instanceof BannerItem)) {
						BlockEntity blockEntity = this.world.getBlockEntity(blockPos);
						assert blockEntity != null;
						this.addBlockEntityNbt(stack, blockEntity);
					} else {
						this.normalBlockSetState(stack, blockState);
					}
				}
			}
			case ENTITY -> {
				Entity entity = ((EntityHitResult) this.crosshairTarget).getEntity();
				stack = entity.getPickBlockStack();
			}
			case MISS -> {
				return;
			}
		}
		if (!isCreative) {
			return;
		}

		if (stack == null || stack.isEmpty()) {
			String string;

			string = switch (type) {
				case BLOCK -> Registry.BLOCK.getId(this.world.getBlockState(((BlockHitResult) this.crosshairTarget).getBlockPos()).getBlock()).toString();
				case ENTITY -> Registry.ENTITY_TYPE.getId(((EntityHitResult) this.crosshairTarget).getEntity().getType()).toString();
				default -> throw new IllegalStateException("Unexpected value: " + type);
			};

			LOGGER.warn("Picking on: [{}] {} gave null item", type, string);
		} else {
			PlayerInventory playerInventory = this.player.getInventory();

			playerInventory.addPickBlock(stack);
			this.interactionManager.clickCreativeStack(this.player.getStackInHand(Hand.MAIN_HAND), 36 + playerInventory.selectedSlot);
		}
	}

	/**
	 * @reason Get the BlockEntity with BlockEntityTag and BlockStateTag if Alt is pressed
	 * @author Zailer43
	 */
	@Overwrite
	private ItemStack addBlockEntityNbt(ItemStack stack, BlockEntity blockEntity) {
		NbtCompound blockEntityTags = blockEntity.writeNbt(new NbtCompound());
		NbtCompound display = new NbtCompound();
		NbtList lore = new NbtList();

		if (stack.getItem() instanceof SkullItem && blockEntityTags.contains("SkullOwner")) {
			stack.getOrCreateTag().put("SkullOwner", blockEntityTags.getCompound("SkullOwner"));
		} else {
			stack.putSubTag("BlockEntityTag", blockEntityTags);
			lore.add(FzmmUtils.generateLoreMessage("(BlockEntityTag)"));
			display.put("Lore", lore);
			if (!blockEntityTags.getString("CustomName").matches("")) {
				display.putString("Name", blockEntityTags.getString("CustomName"));
			}
			stack.putSubTag("display", display);
		}

		if (Screen.hasAltDown()) {
			short loreSize = (short) display.getList("Lore", 8).size();
			lore = new NbtList();
			lore.add(FzmmUtils.generateLoreMessage("(" + (loreSize == 0 ? "" : "BlockEntityTag + ") + "BlockStateTag)"));
			display.put("Lore", lore);
			stack.putSubTag("display", display);
			stack.putSubTag("BlockStateTag", this.getBlockStateTag(blockEntity.getCachedState()));
		}
		return stack;
	}

	public void normalBlockSetState(ItemStack stack, BlockState blockState) {
		if (blockState.getProperties().size() > 0) {

			NbtCompound display = new NbtCompound();
			NbtList lore = new NbtList();

			lore.add(FzmmUtils.generateLoreMessage("(BlockStateTag)"));
			display.put("Lore", lore);
			stack.putSubTag("display", display);
			stack.putSubTag("BlockStateTag", this.getBlockStateTag(blockState));
		}
	}

	public NbtCompound getBlockStateTag(BlockState state) {
		NbtCompound blockStateTag = new NbtCompound();

		for (Property property : state.getProperties()) {
			blockStateTag.putString(property.getName(), property.name(state.get(property)));
		}

		return blockStateTag;
	}

}
