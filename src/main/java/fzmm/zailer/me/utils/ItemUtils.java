package fzmm.zailer.me.utils;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.HistoryScreen;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.SnackBarBuilder;
import fzmm.zailer.me.client.logic.history.FzmmHistory;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.wispforest.owo.config.ui.ConfigScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class ItemUtils {
    private static final Component GIVE_ITEM_ERROR = Component.translatable("fzmm.giveItem.error");

    /**
     * Process the hand item to be able to edit it
     *
     * @return Hand item copy and ready to be modified
     */
    public static ItemStack from(InteractionHand hand) {
        Minecraft client = Minecraft.getInstance();
        assert client.player != null;
        ItemStack stack = client.player.getItemInHand(hand);

        return process(stack);
    }

    public static Item from(String value) {
        return BuiltInRegistries.ITEM.getOptional(Identifier.parse(value)).orElse(Items.STONE);
    }

    /**
     * @return {@code true} if the item was successfully given
     */
    public static boolean give(ItemStack stack) {
        Optional<ISnackBarComponent> snackBar = canGive(stack);
        if (snackBar.isPresent()) {
            Minecraft.getInstance().execute(() ->
                    SnackBarManager.getInstance().remove(SnackBarManager.GIVE_ID).add(snackBar.get())
            );
            return false;
        }


        return uncheckedGive(stack);
    }

    private static boolean uncheckedGive(ItemStack stack) {
        Minecraft client = Minecraft.getInstance();
        assert client.player != null;

        if (FzmmClient.CONFIG.general.giveClientSide()) {
            updateHandClientSide(stack);
        } else {
            Inventory playerInventory = client.player.getInventory();

            int slot = playerInventory.findSlotMatchingItem(stack);
            if (Inventory.isHotbarSlot(slot)) {
                playerInventory.setSelectedSlot(slot);
            } else {
                playerInventory.addAndPickItem(stack);
            }

            updateHand(stack);
        }

        return true;
    }

    /**
     * @return Empty {@link Optional} if the item can be given
     */
    public static Optional<ISnackBarComponent> canGive(ItemStack stack) {
        Minecraft client = Minecraft.getInstance();
        assert client.player != null;

        SnackBarBuilder builder = BaseSnackBarComponent.builder(SnackBarManager.GIVE_ID)
                .title(GIVE_ITEM_ERROR)
                .backgroundColor(EStyles.ALERT_ERROR_COLOR)
                .keepOnLimit()
                .highTimer()
                .startTimer()
                .closeButton()
                .expandDetails();


        if (FzmmClient.CONFIG.general.giveItemSizeLimit()) {
            long stackSize = getLengthInBytes(stack);
            long inventorySize = InventoryUtils.getSizeInBytes(InventoryUtils.getCombinedInventory());
            if ((stackSize + inventorySize) > 8000000) {
                FzmmClient.LOGGER.warn("[ItemUtils] An attempt was made to give an item with size of {} bytes (with {} bytes already in inventory)",
                        stackSize, inventorySize);

                return Optional.of(builder.details(Component.translatable("fzmm.giveItem.exceedLimit",
                                getLengthInKB(stackSize + inventorySize),
                                getLengthInKB(8000000L)
                        ))
                        .button(snackBar -> UIComponents.button(Component.translatable("fzmm.gui.title.configs.icon"),
                                buttonComponent -> {
                                    client.setScreen(ConfigScreen.create(FzmmClient.CONFIG, client.screen));
                                    snackBar.close();
                                }))
                        .build());
            }
        }

        FzmmHistory.add(stack);

        if (FzmmClient.CONFIG.general.checkValidCodec() && !isCodecValid(stack)) {
            FzmmClient.LOGGER.warn("[ItemUtils] An item with an invalid codec was found: {}", stack.getComponents().toString());
            return Optional.of(builder.details(Component.translatable("fzmm.giveItem.codecError"))
                    .backgroundColor(EStyles.ALERT_WARNING_COLOR)
                    .button(snackBar -> UIComponents.button(Component.translatable("fzmm.gui.title.configs.icon"),
                            buttonComponent -> {
                                client.setScreen(ConfigScreen.create(FzmmClient.CONFIG, client.screen));
                                snackBar.close();
                            })
                    ).button(snackBar -> UIComponents.button(Component.translatable("fzmm.giveItem.codecError.ignore"),
                            buttonComponent -> {
                                uncheckedGive(stack);
                                snackBar.close();
                            })
                    ).sizing(Sizing.fixed(250), Sizing.content())
                    .build()
            );
        }

        if (isNotAllowedToGive()) {
            return Optional.of(builder.details(Component.translatable("fzmm.giveItem.notAllowed"))
                    .backgroundColor(EStyles.ALERT_ERROR_COLOR)
                    .button(snackBar -> UIComponents.button(Component.translatable("fzmm.gui.title.history"),
                            buttonComponent -> {
                                FzmmUtils.setScreen(new HistoryScreen(client.screen));
                                snackBar.close();
                            }))
                    .build()
            );
        }

        return Optional.empty();
    }

    /**
     * Process the item to be able to edit it
     *
     * @return The item ready to be modified
     */
    public static ItemStack process(ItemStack stack) {
        ItemStack stackCopy = stack.copy();

        if (FzmmClient.CONFIG.general.removeViaVersionTags()) {
            stackCopy.update(DataComponents.CUSTOM_DATA, null, nbtComponent -> {
                if (nbtComponent == null) {
                    return null;
                }

                CompoundTag customTag = nbtComponent.copyTag();

                // This affects multiplayer when the server is on a lower version and ViaVersion is used.
                //
                // When removing ViaVersion tags, the cached version for ViaVersion is deleted.
                // These cached versions are used for players on older versions, but these tags
                // are more important than those for the higher version. Consequently, if you
                // modify an item with these tags, it will later revert to the cached version, losing the changes.
                recursiveRemoveTags(customTag, s -> s.startsWith("VV|Protocol"));

                return customTag.keySet().isEmpty() ? null : CustomData.of(customTag);
            });
        }

        if (FzmmClient.CONFIG.general.minimizeHeadTexturesTag()) {
            stackCopy.update(DataComponents.PROFILE, null, profileComponent -> {
                if (profileComponent == null) {
                    return null;
                }

                return HeadUtils.minimizeTextures(profileComponent.partialProfile());
            });
        }

        return stackCopy;
    }

    public static void recursiveRemoveTags(CompoundTag tags, Predicate<String> keyPredicate) {
        List<String> keysToRemove = new ArrayList<>();
        for (String key : tags.keySet()) {
            if (keyPredicate.test(key)) {
                keysToRemove.add(key);
                continue;
            }

            Tag value = tags.get(key);
            if (value instanceof CompoundTag compound) {
                recursiveRemoveTags(compound, keyPredicate);
                continue;
            }

            if (value instanceof ListTag list) {
                for (var element : list) {
                    if (element instanceof CompoundTag compoundElement) {
                        recursiveRemoveTags(compoundElement, keyPredicate);
                    }
                }
            }
        }

        for (String key : keysToRemove) {
            tags.remove(key);
        }
    }

    public static boolean isCodecValid(ItemStack stack) {
        try {
            ServerboundSetCreativeModeSlotPacket packet = new ServerboundSetCreativeModeSlotPacket(0, stack);
            ByteBuf buf = Unpooled.buffer();
            RegistryFriendlyByteBuf registryByteBuf = new RegistryFriendlyByteBuf(buf, FzmmUtils.getRegistryManager());
            ServerboundSetCreativeModeSlotPacket.STREAM_CODEC.encode(registryByteBuf, packet);
            ServerboundSetCreativeModeSlotPacket.STREAM_CODEC.decode(registryByteBuf);
        } catch (Exception ignored) {
            return false;
        }
        return true;
    }

    public static void updateHand(ItemStack stack) {
        Minecraft client = Minecraft.getInstance();
        assert client.gameMode != null;
        assert client.player != null;

        Inventory playerInventory = client.player.getInventory();
        updateHandClientSide(stack); // required since 1.21.2

        // server-side sync
        client.gameMode.handleCreativeModeItemAdd(stack, Inventory.INVENTORY_SIZE + playerInventory.getSelectedSlot());
    }

    private static void updateHandClientSide(ItemStack stack) {
        assert Minecraft.getInstance().player != null;
        Inventory inventory = Minecraft.getInstance().player.getInventory();
        inventory.setItem(inventory.getSelectedSlot(), stack);
    }

    public static String getLengthInKB(long length) {
        return new DecimalFormat("#,##0.0").format(length / 1024f);
    }

    public static long getLengthInBytes(ItemStack stack) {
        ByteCountDataOutput byteCountDataOutput = ByteCountDataOutput.getInstance();

        try {
            NbtIo.writeUnnamedTagWithFallback(encodeToNbt(stack).getOrThrow(), byteCountDataOutput);
        } catch (Exception ignored) {
            return 0;
        }

        long count = byteCountDataOutput.getCount();
        byteCountDataOutput.reset();
        return count;
    }

    public static boolean isNotAllowedToGive() {
        Minecraft client = Minecraft.getInstance();
        if (client.gameMode == null) {
            return true;
        }

        return !(client.gameMode.getPlayerMode().isCreative()
                || FzmmClient.CONFIG.general.giveClientSide());
    }

    public static DataResult<Tag> encodeToNbt(ItemStack stack) {
        return ItemStack.CODEC.encodeStart(FzmmUtils.getRegistryOps(NbtOps.INSTANCE), stack);
    }

    public static DataResult<ItemStack> decodeFromNbt(Tag nbt) {
        return ItemStack.CODEC.decode(FzmmUtils.getRegistryOps(NbtOps.INSTANCE), nbt).map(Pair::getFirst);
    }
}
