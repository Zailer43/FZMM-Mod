package fzmm.zailer.me.client;

import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.LoreUtils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.state.property.Property;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.stream.Collectors;

public class PickItem {

    public static void doItemPick(MinecraftClient client) {
        if (client.crosshairTarget == null) {
            return;
        }
        assert client.world != null;
        assert client.player != null;
        assert client.interactionManager != null;
        boolean isCreative = client.player.getAbilities().creativeMode;
        HitResult.Type type = client.crosshairTarget.getType();
        ItemStack stack;

        switch (type) {
            case BLOCK -> {
                BlockPos blockPos = ((BlockHitResult) client.crosshairTarget).getBlockPos();
                BlockState blockState = client.world.getBlockState(blockPos);
                if (blockState.isAir()) {
                    return;
                }

                Block block = blockState.getBlock();
                stack = block.getPickStack(client.world, blockPos, blockState);
                PlayerInventory playerInventory = client.player.getInventory();

                int i = playerInventory.getSlotWithStack(stack);
                if (!isCreative && i != -1) {
                    if (PlayerInventory.isValidHotbarIndex(i)) {
                        playerInventory.selectedSlot = i;
                    } else {
                        client.interactionManager.pickFromInventory(i);
                    }
                    return;
                }

                if (Screen.hasAltDown() && blockState.getProperties().size() > 0)
                    stack.setSubNbt(BlockItem.BLOCK_STATE_TAG_KEY, getBlockStateTag(blockState));

                if (Screen.hasControlDown() && blockState.hasBlockEntity() && !(stack.getItem() instanceof BannerItem)) {
                    NbtCompound blockEntityTag = getBlockEntityTag(client.world, blockPos);
                    if (stack.getItem() instanceof SkullItem)
                        stack.setSubNbt(SkullItem.SKULL_OWNER_KEY, blockEntityTag.get(SkullItem.SKULL_OWNER_KEY));
                    else
                        stack.setSubNbt(BlockItem.BLOCK_ENTITY_TAG_KEY, blockEntityTag);
                }

                formatItem(stack);
            }
            case ENTITY -> {
                Entity entity = ((EntityHitResult) client.crosshairTarget).getEntity();
                stack = entity.getPickBlockStack();
            }
            case MISS -> {
                BlockState blockState = client.world.getBlockState(new BlockPos(client.crosshairTarget.getPos()));
                if (blockState.isAir() && !client.options.getPerspective().isFirstPerson()) {
                    stack = client.player.getPickBlockStack();
                } else {
                    return;
                }
            }
            default -> stack = new ItemStack(Items.BARRIER);
        }
        if (!isCreative) {
            return;
        }

        if (stack == null || stack.isEmpty()) {
            String string = switch (type) {
                case BLOCK -> Registry.BLOCK.getId(client.world.getBlockState(((BlockHitResult) client.crosshairTarget).getBlockPos()).getBlock()).toString();
                case ENTITY -> Registry.ENTITY_TYPE.getId(((EntityHitResult) client.crosshairTarget).getEntity().getType()).toString();
                case MISS -> "Pick block is null, crosshairTarget: MISS";
            };
            Logger logger = LogManager.getLogger("FZMM PickBlock");
            logger.warn("Picking on: [{}] {} gave null item", type, string);
            return;
        }

        FzmmUtils.giveItem(stack);

    }

    private static NbtCompound getBlockEntityTag(World world, BlockPos blockPos) {
        BlockEntity blockEntity = world.getBlockEntity(blockPos);
        assert blockEntity != null;
        return blockEntity.writeNbt(new NbtCompound());
    }

    private static void formatItem(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null)
            return;
        List<String> keys = nbt.getKeys().stream().sorted().collect(Collectors.toList());
        NbtCompound display = LoreUtils.generateLoreMessage("(" + String.join(" + ", keys) + ")");

        if (nbt.contains(BlockItem.BLOCK_ENTITY_TAG_KEY, NbtElement.COMPOUND_TYPE)) {
            NbtCompound blockEntityTag = nbt.getCompound(BlockItem.BLOCK_ENTITY_TAG_KEY);
            if (blockEntityTag.contains("CustomName", NbtElement.STRING_TYPE)) {
                display.putString(ItemStack.NAME_KEY, blockEntityTag.getString("CustomName"));
            }
        }

        stack.setSubNbt(ItemStack.DISPLAY_KEY, display);
    }

    private static NbtCompound getBlockStateTag(BlockState state) {
        NbtCompound blockStateTag = new NbtCompound();

        for (Property<?> property : state.getProperties()) {
            blockStateTag.putString(property.getName(), state.get(property).toString().toLowerCase());
        }

        if (FzmmConfig.get().general.removeFacingState && blockStateTag.contains("facing")) {
            blockStateTag.remove("facing");
        }

        return blockStateTag;
    }
}
