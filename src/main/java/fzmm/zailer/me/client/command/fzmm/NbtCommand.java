package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.TagsConstant;
import fzmm.zailer.me.utils.TextUtils;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class NbtCommand implements ISubCommand {
    @Override
    public String alias() {
        return "nbt";
    }

    @Override
    public String syntax() {
        return "nbt";
    }

    @Override
    public LiteralCommandNode<FabricClientCommandSource> getBaseCommand(CommandRegistryAccess registryAccess, LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.executes(ctx -> {
            this.showNbt(ctx);
            return 1;
        }).build();
    }

    private void showNbt(CommandContext<FabricClientCommandSource> ctx) {
        MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;
        ItemStack stack = client.player.getInventory().getSelectedStack();
        DynamicRegistryManager registryManager = FzmmUtils.getRegistryManager();

        // is modified or has NBT
        if (stack.getComponentChanges().isEmpty() ||
                !(stack.toNbt(registryManager) instanceof NbtCompound nbt) ||
                !nbt.contains(TagsConstant.ENCODE_STACK_COMPONENTS)) {

            ctx.getSource().sendError(Text.translatable("commands.fzmm.item.withoutNbt"));
            return;
        }

        final int MAX_HOVER_LENGTH = 15000;
        String nbtString = toFormatedComponent(nbt.getCompoundOrEmpty(TagsConstant.ENCODE_STACK_COMPONENTS), false).getString();
        String nbtStringHover = nbtString;
        nbtString = TextUtils.removeUnpairedMultibyte(nbtString);
        int nbtLength = nbtString.length();

        // if the hover text is too long it gives a lot of lag with cursor over it (and doesn't fit on the screen)
        if (nbtLength > MAX_HOVER_LENGTH) {
            nbtStringHover = "..." + nbtStringHover.substring(nbtLength - MAX_HOVER_LENGTH, nbtLength);
        }

        Text length = Text.literal(String.valueOf(nbtLength))
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));
        MutableText lengthMessage = Text.translatable("commands.fzmm.nbt.length", length)
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR));

        MutableText nbtText = this.nbtToText(nbt, client, nbtString);
        MutableText message = this.nbtChatMessageOf(stack, nbtText, nbtString, nbtStringHover);
        client.inGameHud.getChatHud().addMessage(message.append("\n").append(lengthMessage));
    }

    private MutableText nbtToText(NbtCompound nbt, MinecraftClient client, String nbtString) {
        final int MAX_CHAT_LINES = 90; // vanilla chat lines = 100
        // check if the message length fits within 90% of the maximum chat lines in vanilla
        // in order to avoid writing a message too long which could cause crash with mods
        // that increase the limit beyond vanilla (and lag spike in vanilla)
        //
        // note: the final result could be more than 90% due to formatting adding spaces.
        if (client.textRenderer.getWidth(nbtString) > ChatHud.getWidth(client.options.getChatWidth().getValue()) * MAX_CHAT_LINES) {
            String message = String.format("[%s]", Text.translatable("commands.fzmm.nbt.tooLong").getString());
            return Text.literal(message).setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));
        } else {
            return toFormatedComponent(nbt.getCompoundOrEmpty(TagsConstant.ENCODE_STACK_COMPONENTS), true);
        }
    }

    private MutableText nbtChatMessageOf(ItemStack stack, MutableText nbtMessage, String nbtString, String nbtStringHover) {
        Text clickToCopyMessage = Text.literal(" (").append(Text.translatable("commands.fzmm.nbt.click")).append(")")
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));

        return Text.literal(stack.getItem().toString()).setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR))
                .append(nbtMessage.copy().append(clickToCopyMessage)
                        .setStyle(nbtMessage.getStyle()
                                .withClickEvent(new ClickEvent.CopyToClipboard(nbtString))
                                .withHoverEvent(new HoverEvent.ShowText(Text.literal(nbtStringHover)))
                        )
                );
    }

    public static MutableText toFormatedComponent(NbtCompound nbt, boolean prettyPrint) {
        MutableText result = Text.literal("[");
        List<Text> componentsText = new ArrayList<>(nbt.getKeys().size());

        for (var key : nbt.getKeys()) {
            MutableText text = Text.empty();
            NbtElement tag = nbt.get(key);

            if (tag == null) {
                tag = new NbtCompound();
            }

            text.append(Text.literal(key).setStyle(Style.EMPTY.withFormatting(Formatting.DARK_AQUA)));
            text.append(Text.literal("="));
            if (prettyPrint) {
                text.append(NbtHelper.toPrettyPrintedText(tag));
            } else {
                text.append(tag.toString());
            }

            componentsText.add(text);
        }

        for (int i = 0; i != componentsText.size(); i++) {
            result.append(componentsText.get(i));

            if (i != componentsText.size() - 1) {
                result.append(", ");
            }
        }

        return result.append("]");
    }
}
