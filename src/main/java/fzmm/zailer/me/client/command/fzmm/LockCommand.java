package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.core.component.DataComponentExactPredicate;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.LockCode;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class LockCommand implements ISubCommand {
    @Override
    public String alias() {
        return "lock";
    }

    @Override
    public String syntax() {
        return "lock <key>";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandBuildContext registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.then(ClientCommands.argument("key", ComponentArgument.textComponent(registryAccess)).executes(ctx -> {

            Component key = ctx.getArgument("key", Component.class);
            this.lockContainer(key);
            return 1;

        })).build();
    }

    private void lockContainer(Component key) {
        Minecraft client = Minecraft.getInstance();

        ItemStack containerStack = ItemUtils.from(InteractionHand.MAIN_HAND);
        ItemStack lockStack = ItemUtils.from(InteractionHand.OFF_HAND);

        containerStack.update(DataComponents.LOCK, LockCode.NO_LOCK, component -> {
            ItemPredicate predicate = ItemPredicate.Builder.item()
                    .withComponents(DataComponentMatchers.Builder.components()
                            .exact(DataComponentExactPredicate.expect(DataComponents.CUSTOM_NAME, key))
                            .build()
                    ).build();

            return new LockCode(predicate);
        });

        lockStack.update(DataComponents.CUSTOM_NAME, Component.empty(), component -> key.copy());

        ItemUtils.give(containerStack);
        assert client.gameMode != null;
        // PlayerInventory.OFF_HAND_SLOT is 40, but OFF_HAND_SLOT is 45 (PlayerInventory.MAIN_SIZE + PlayerInventory.HOTBAR_SIZE)
        client.gameMode.handleCreativeModeItemAdd(lockStack, Inventory.INVENTORY_SIZE + Inventory.getSelectionSize());
    }
}
