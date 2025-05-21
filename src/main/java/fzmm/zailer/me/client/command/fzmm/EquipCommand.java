package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class EquipCommand implements ISubCommand {
    @Override
    public String alias() {
        return "equip";
    }

    @Override
    public String syntax() {
        return "equip <armor>";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandRegistryAccess registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.build();
    }

    @Override
    public List<LiteralCommandNode<FabricClientCommandSource>> getSubCommands(CommandRegistryAccess registryAccess) {
        List<LiteralCommandNode<FabricClientCommandSource>> result = new ArrayList<>();

        result.add(ClientCommandManager.literal("head").executes(ctx -> {
            this.swapItemWithHand(EquipmentSlot.HEAD);
            return 1;
        }).build());

        result.add(ClientCommandManager.literal("chest").executes(ctx -> {
            this.swapItemWithHand(EquipmentSlot.CHEST);
            return 1;
        }).build());

        result.add(ClientCommandManager.literal("legs").executes(ctx -> {
            this.swapItemWithHand(EquipmentSlot.LEGS);
            return 1;
        }).build());

        result.add(ClientCommandManager.literal("feet").executes(ctx -> {
            this.swapItemWithHand(EquipmentSlot.FEET);
            return 1;
        }).build());

        return result;
    }

    private void swapItemWithHand(EquipmentSlot slot) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        assert client.interactionManager != null;
        ClientPlayerEntity player = client.player;

        if (ItemUtils.isNotAllowedToGive()) {
            FzmmClient.LOGGER.warn("[FzmmCommand] Creative mode is necessary to swap items");
            client.inGameHud.getChatHud().addMessage(Text.translatable("fzmm.item.error.actionNotAllowed").setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR)));
            return;
        }

        PlayerInventory inventory = player.getInventory();
        ItemStack handStack = player.getMainHandStack();
        ItemStack armorStack = player.getEquippedStack(slot);
        // I don't know why but at least with ClientPlayerInteractionManager#clickCreativeStack
        // they are placed in the reverse order
        // 5 = crafting slot + crafting result slot
        int armorSlotId = Math.abs(slot.getOffsetEntitySlotId(-3)) + 5;

        client.interactionManager.clickCreativeStack(handStack, armorSlotId);
        inventory.setStack(slot.getOffsetEntitySlotId(PlayerInventory.MAIN_SIZE), handStack);

        client.interactionManager.clickCreativeStack(armorStack, PlayerInventory.MAIN_SIZE + inventory.getSelectedSlot());
        inventory.setStack(inventory.getSelectedSlot(), armorStack);
    }
}
