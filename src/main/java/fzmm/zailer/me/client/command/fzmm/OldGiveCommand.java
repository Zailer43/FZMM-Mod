package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.client.command.argument_type.ComponentArgumentType;
import fzmm.zailer.me.client.command.argument_type.VersionArgumentType;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.ItemUtils;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.option.HotbarStorageEntry;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.datafixer.Schemas;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtOps;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

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
        return builder.then(ClientCommandManager.argument("item", IdentifierArgumentType.identifier()).executes((ctx) -> {
                    ctx.getSource().sendError(Text.translatable("commands.fzmm.old_give.nbt_required").formatted(Formatting.RED));
                    return 1;
                }).then(ClientCommandManager.argument("nbt", ComponentArgumentType.component()).executes((ctx) -> {

                    Identifier item = ctx.getArgument("item", Identifier.class);
                    NbtCompound nbt = ComponentArgumentType.getNbtCompound(ctx, "nbt");

                    oldGiveItem(item, nbt, VersionArgumentType.VERSIONS.get(0));
                    return 1;
                }).then(ClientCommandManager.argument("item_version", VersionArgumentType.version()).executes(ctx -> {
                    Identifier item = ctx.getArgument("item", Identifier.class);
                    NbtCompound nbt = ComponentArgumentType.getNbtCompound(ctx, "nbt");
                    Pair<String, Integer> version = VersionArgumentType.getVersion(ctx, "item_version");

                    oldGiveItem(item, nbt, version);
                    return 1;
                }))).then(ClientCommandManager.argument("damage", IntegerArgumentType.integer()).executes((ctx) -> {
                    ctx.getSource().sendError(Text.translatable("commands.fzmm.old_give.nbt_required").formatted(Formatting.RED));

                    return 1;
                }).then(ClientCommandManager.argument("nbt", ComponentArgumentType.component()).executes((ctx) -> {

                    Identifier item = ctx.getArgument("item", Identifier.class);
                    int damage = IntegerArgumentType.getInteger(ctx, "damage");
                    NbtCompound nbt = ComponentArgumentType.getNbtCompound(ctx, "nbt");

                    oldGiveItem(item, damage, nbt, VersionArgumentType.VERSIONS.get(0));
                    return 1;
                }).then(ClientCommandManager.argument("item_version", VersionArgumentType.version()).executes(ctx -> {
                    Identifier item = ctx.getArgument("item", Identifier.class);
                    int damage = IntegerArgumentType.getInteger(ctx, "damage");
                    NbtCompound nbt = ComponentArgumentType.getNbtCompound(ctx, "nbt");
                    Pair<String, Integer> version = VersionArgumentType.getVersion(ctx, "item_version");

                    oldGiveItem(item, damage, nbt, version);
                    return 1;
                }))))

        ).build();
    }

    public static void oldGiveItem(Identifier item, NbtCompound nbtCompound, Pair<String, Integer> oldVersion) {
        oldGiveItem(item, 0, nbtCompound, oldVersion);
    }

    public static void oldGiveItem(Identifier item, int damage, NbtCompound nbtCompound, Pair<String, Integer> oldVersion) {
        CompletableFuture.runAsync(() -> {
            AtomicReference<String> errorStr = new AtomicReference<>(null);
            int oldVersionData = oldVersion.getRight();
            NbtCompound fakeHotbarStorageCompound = getFakeHotbarStorageCompound(item.toString(), damage, nbtCompound, oldVersionData);
            // use data fixers to update nbt from a fake HotbarStorageEntry
            fakeHotbarStorageCompound = DataFixTypes.HOTBAR.update(Schemas.getFixer(), fakeHotbarStorageCompound, oldVersionData);

            // get stack from fake hotbar
            HotbarStorageEntry parsedHotbarStorage = HotbarStorageEntry.CODEC
                    .parse(NbtOps.INSTANCE, fakeHotbarStorageCompound.get(String.valueOf(0)))
                    .resultOrPartial(error -> {
                        errorStr.set(error);
                        FzmmClient.LOGGER.error("[FzmmCommand] Failed to parse update item with '/fzmm old_give': {}", error);
                    })
                    .orElseGet(HotbarStorageEntry::new);

            DynamicRegistryManager registryManager = FzmmUtils.getRegistryManager();
            List<ItemStack> result = parsedHotbarStorage.deserialize(registryManager);

            ChatHud chatHud = MinecraftClient.getInstance().inGameHud.getChatHud();
            MutableText errorMessage = Text.translatable("commands.fzmm.old_give.error", item.toString(), oldVersion.getLeft()).formatted(Formatting.RED);
            if (errorStr.get() != null) {
                chatHud.addMessage(errorMessage.append("\n").append(Text.literal(errorStr.get())));
            } else if (result.isEmpty() || result.get(0).isEmpty()) {
                chatHud.addMessage(errorMessage);
            } else {
                ItemUtils.give(ItemUtils.process(result.get(0)));
                chatHud.addMessage(Text.translatable("commands.fzmm.old_give.success", item.toString(), oldVersion.getLeft())
                        .withColor(FzmmClient.CHAT_BASE_COLOR)
                );
            }
        });
    }

    @NotNull
    private static NbtCompound getFakeHotbarStorageCompound(String item, int damage, NbtCompound nbtCompound, int oldVersion) {
        NbtCompound fakeHotbarStorageCompound = new NbtCompound();

        try {
            int version1204 = VersionArgumentType.parse("1.20.4").getRight();
            String countKey = oldVersion > version1204 ? "count" : "Count";
            String tagKey = oldVersion > version1204 ? "components" : "tag";

            for (int i = 0; i < 9; i++) {
                NbtList entry = new NbtList();
                if (i == 0) {
                    NbtCompound itemCompound = new NbtCompound();
                    itemCompound.putByte(countKey, (byte) 1);
                    itemCompound.putString("id", item);
                    itemCompound.put(tagKey, nbtCompound);
                    if (oldVersion <= VersionArgumentType.parse("1.12.2").getRight()) {
                        itemCompound.putInt("Damage", damage);
                    }

                    entry.add(itemCompound);
                }

                for (int j = entry.size(); j != 9; j++) {
                    NbtCompound emptyCompound = new NbtCompound();
                    emptyCompound.putByte(countKey, (byte) 1);
                    emptyCompound.putString("id", "minecraft:air");

                    entry.add(emptyCompound);
                }

                fakeHotbarStorageCompound.put(String.valueOf(i), entry);
            }
        } catch (CommandSyntaxException e) {
            FzmmClient.LOGGER.error("[FzmmCommand] Failed to get fake hotbar storage compound", e);
            return new NbtCompound();
        }

        return fakeHotbarStorageCompound;
    }
}
