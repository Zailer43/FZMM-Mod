package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.client.command.argument_type.ComponentArgumentType;
import fzmm.zailer.me.client.command.argument_type.VersionArgumentType;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.IdentifierArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class OldGiveCommand implements ISubCommand {
    @Override
    public String alias() {
        return "old_give";
    }

    @Override
    public String syntax() {
        return "old_give <item> <damage (optional)> <nbt> <version_code>";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandBuildContext registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        var itemNode = ClientCommands.argument("item", IdentifierArgument.id()).executes((ctx) -> {
            ctx.getSource().sendError(Component.translatable("commands.fzmm.old_give.nbt_required").withStyle(ChatFormatting.RED));
            return 1;
        });
        var damageNode = ClientCommands.argument("damage", IntegerArgumentType.integer()).executes((ctx) -> {
            ctx.getSource().sendError(Component.translatable("commands.fzmm.old_give.nbt_required").withStyle(ChatFormatting.RED));
            return 1;
        });
        var nbtNode = ClientCommands.argument("nbt", ComponentArgumentType.component()).executes(ctx -> {
            Identifier item = ctx.getArgument("item", Identifier.class);
            int damage;
            try {
                damage = ctx.getArgument("damage", int.class);
            } catch (IllegalArgumentException ignored) {
                damage = 0;// damage no specified
            }
            CompoundTag nbt = ComponentArgumentType.getNbtCompound(ctx, "nbt");

            oldGiveItem(item, damage, nbt, VersionArgumentType.VERSIONS.getFirst());
            return 1;
        });
        var versionNode = ClientCommands.argument("item_version", VersionArgumentType.version()).executes(ctx -> {
            Identifier item = ctx.getArgument("item", Identifier.class);
            int damage;
            try {
                damage = ctx.getArgument("damage", int.class);
            } catch (IllegalArgumentException ignored) {
                damage = 0;// damage no specified
            }
            CompoundTag nbt = ComponentArgumentType.getNbtCompound(ctx, "nbt");
            Pair<String, Integer> version = VersionArgumentType.getVersion(ctx, "item_version");

            oldGiveItem(item, damage, nbt, version);
            return 1;
        });
        builder.then(itemNode.then(nbtNode.then(versionNode)).build());
        builder.then(itemNode.then(damageNode.then(nbtNode.then(versionNode))));
        return builder.build();
    }

    private static void oldGiveItem(Identifier item, int damage, CompoundTag nbtCompound, Pair<String, Integer> oldVersion) {
        CompletableFuture.runAsync(() -> {
            MutableComponent errorMessage = Component.translatable("commands.fzmm.old_give.error", item.toString(), oldVersion.getFirst()).withStyle(ChatFormatting.RED);
            ChatComponent chatHud = Minecraft.getInstance().gui.hud.getChat();

            try {
               Optional<ItemStack> stackOptional = updateStack(item, damage, nbtCompound, oldVersion.getSecond());

               if (stackOptional.isEmpty() || stackOptional.get().isEmpty()) {
                   chatHud.addClientSystemMessage(errorMessage);
               } else {
                   ItemUtils.give(ItemUtils.process(stackOptional.get()));
                   chatHud.addClientSystemMessage(Component.translatable("commands.fzmm.old_give.success", item.toString(), oldVersion.getFirst())
                           .withColor(FzmmClient.CHAT_BASE_COLOR)
                   );
               }
           } catch (Exception e) {
                chatHud.addClientSystemMessage(errorMessage);
                FzmmClient.LOGGER.warn("[OldGiveCommand] Failed to update stack with exception:", e);
           }
        });
    }

    public static Optional<ItemStack> updateStack(Identifier item, int damage, CompoundTag nbtCompound, int itemVersion) throws Exception {
        try {
            CompoundTag itemNbt = writeNbt(item, damage, nbtCompound, itemVersion);
            return updateStack(itemNbt, itemVersion)
                    .flatMap(nbtElement -> ItemUtils.decodeFromNbt(nbtElement).result());
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[OldGiveCommand] Failed to update item with '/fzmm old_give': {}:{} (damage: {})", item.toString(), nbtCompound.toString(), damage);
            throw e;
        }
    }

    @SuppressWarnings("RedundantThrows")
    public static Optional<Tag> updateStack(CompoundTag nbtCompound, int itemVersion) throws Exception {
        // use data fixers to update nbt
        return Optional.of(DataFixers.getDataFixer().update(References.ITEM_STACK,
                new Dynamic<>(NbtOps.INSTANCE, nbtCompound),
                itemVersion,
                SharedConstants.getCurrentVersion().dataVersion().version()
        ).getValue());
    }

    public static CompoundTag writeNbt(Identifier item, int damage, CompoundTag nbtCompound, int itemVersion) {
        boolean isCompound = itemVersion > VersionArgumentType.LATEST_VERSION_WITH_NBT;

        CompoundTag result = new CompoundTag();
        result.putByte(isCompound ? "count" : "Count", (byte) 1);
        result.putString("id", item.toString());
        result.put(isCompound ? "components" : "tag", nbtCompound);
        if (itemVersion <= VersionArgumentType.LATEST_VERSION_WITH_DAMAGE) {
            result.putInt("Damage", damage);
        }

        return result;
    }
}
