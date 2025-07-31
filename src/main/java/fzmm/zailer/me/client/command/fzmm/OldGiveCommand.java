package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.serialization.Dynamic;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.client.command.argument_type.ComponentArgumentType;
import fzmm.zailer.me.client.command.argument_type.VersionArgumentType;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.datafixer.Schemas;
import net.minecraft.datafixer.TypeReferences;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

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
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandRegistryAccess registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        var itemNode = ClientCommandManager.argument("item", IdentifierArgumentType.identifier()).executes((ctx) -> {
            ctx.getSource().sendError(Text.translatable("commands.fzmm.old_give.nbt_required").formatted(Formatting.RED));
            return 1;
        });
        var damageNode = ClientCommandManager.argument("damage", IntegerArgumentType.integer()).executes((ctx) -> {
            ctx.getSource().sendError(Text.translatable("commands.fzmm.old_give.nbt_required").formatted(Formatting.RED));
            return 1;
        });
        var nbtNode = ClientCommandManager.argument("nbt", ComponentArgumentType.component()).executes(ctx -> {
            Identifier item = ctx.getArgument("item", Identifier.class);
            // 4 = fzmm -> old_give -> item -> nbt, 5 if damage is specified
            int damage = ctx.getNodes().size() == 4 ? 0 : IntegerArgumentType.getInteger(ctx, "damage");
            NbtCompound nbt = ComponentArgumentType.getNbtCompound(ctx, "nbt");

            oldGiveItem(item, damage, nbt, VersionArgumentType.VERSIONS.get(0));
            return 1;
        });
        var versionNode = ClientCommandManager.argument("item_version", VersionArgumentType.version()).executes(ctx -> {
            Identifier item = ctx.getArgument("item", Identifier.class);
            // 5 = fzmm -> old_give ->  item -> nbt -> item_version, 6 if damage is specified
            int damage = ctx.getNodes().size() == 5 ? 0 : IntegerArgumentType.getInteger(ctx, "damage");
            NbtCompound nbt = ComponentArgumentType.getNbtCompound(ctx, "nbt");
            Pair<String, Integer> version = VersionArgumentType.getVersion(ctx, "item_version");

            oldGiveItem(item, damage, nbt, version);
            return 1;
        });
        builder.then(itemNode.then(nbtNode.then(versionNode)).build());
        builder.then(itemNode.then(damageNode.then(nbtNode.then(versionNode))));
        return builder.build();
    }

    private static void oldGiveItem(Identifier item, int damage, NbtCompound nbtCompound, Pair<String, Integer> oldVersion) {
        CompletableFuture.runAsync(() -> {
            MutableText errorMessage = Text.translatable("commands.fzmm.old_give.error", item.toString(), oldVersion.getLeft()).formatted(Formatting.RED);
            ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();

            try {
               Optional<ItemStack> stackOptional = updateStack(item, damage, nbtCompound, oldVersion.getRight());

               if (stackOptional.isEmpty() || stackOptional.get().isEmpty()) {
                   chatHud.addMessage(errorMessage);
               } else {
                   ItemUtils.give(ItemUtils.process(stackOptional.get()));
                   chatHud.addMessage(Text.translatable("commands.fzmm.old_give.success", item.toString(), oldVersion.getLeft())
                           .withColor(FzmmClient.CHAT_BASE_COLOR)
                   );
               }
           } catch (Exception e) {
                chatHud.addMessage(errorMessage);
                FzmmClient.LOGGER.warn("[OldGiveCommand] Failed to update stack with exception:", e);
           }
        });
    }

    public static Optional<ItemStack> updateStack(Identifier item, int damage, NbtCompound nbtCompound, int itemVersion) throws Exception {
        try {
            NbtCompound itemNbt = writeNbt(item, damage, nbtCompound, itemVersion);
            return updateStack(itemNbt, itemVersion)
                    .flatMap(nbtElement -> ItemUtils.decodeFromNbt(nbtElement).result());
        } catch (Exception e) {
            FzmmClient.LOGGER.error("[OldGiveCommand] Failed to update item with '/fzmm old_give': {}:{} (damage: {})", item.toString(), nbtCompound.toString(), damage);
            throw e;
        }
    }

    @SuppressWarnings("RedundantThrows")
    public static Optional<NbtElement> updateStack(NbtCompound nbtCompound, int itemVersion) throws Exception {
        // use data fixers to update nbt
        return Optional.of(Schemas.getFixer().update(TypeReferences.ITEM_STACK,
                new Dynamic<>(NbtOps.INSTANCE, nbtCompound),
                itemVersion,
                SharedConstants.getGameVersion().dataVersion().id()
        ).getValue());
    }

    public static NbtCompound writeNbt(Identifier item, int damage, NbtCompound nbtCompound, int itemVersion) {
        boolean isCompound = itemVersion > VersionArgumentType.LATEST_VERSION_WITH_NBT;

        NbtCompound result = new NbtCompound();
        result.putByte(isCompound ? "count" : "Count", (byte) 1);
        result.putString("id", item.toString());
        result.put(isCompound ? "components" : "tag", nbtCompound);
        if (itemVersion <= VersionArgumentType.LATEST_VERSION_WITH_DAMAGE) {
            result.putInt("Damage", damage);
        }

        return result;
    }
}
