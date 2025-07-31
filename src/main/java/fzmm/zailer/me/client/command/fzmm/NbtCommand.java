package fzmm.zailer.me.client.command.fzmm;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.command.ISubCommand;
import fzmm.zailer.me.utils.ItemUtils;
import fzmm.zailer.me.utils.TagsConstant;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.component.ComponentChanges;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

        ComponentChanges modifiedComponent = stack.getComponentChanges(); // use only to check if it has modified nbt
        Optional<NbtElement> stackNbtOptional = ItemUtils.encodeToNbt(stack).result();
        if (modifiedComponent.isEmpty() || stackNbtOptional.isEmpty() || !(stackNbtOptional.get() instanceof NbtCompound nbt) ||
                !nbt.contains(TagsConstant.ENCODE_STACK_COMPONENTS)) {

            ctx.getSource().sendError(Text.translatable("commands.fzmm.item.withoutNbt"));
            return;
        }

        // vanilla chat lines = 100
        final int MAX_CHAT_LINES = 90;
        final int MAX_HOVER_LENGTH = 15000;
        String nbtString = toFormatedComponent(nbt.getCompoundOrEmpty(TagsConstant.ENCODE_STACK_COMPONENTS), false).getString();
        String nbtStringHover = nbtString;
        int nbtLength = nbtString.length();
        MutableText nbtMessage;

        // check if the message length fits within 90% of the maximum chat lines in vanilla
        // in order to avoid writing a message too long which could cause crash with mods
        // that increase the limit beyond vanilla (and lag spike in vanilla)
        //
        // note: the final result could be more than 90% due to formatting adding spaces.
        if (client.textRenderer.getWidth(nbtString) > ChatHud.getWidth(client.options.getChatWidth().getValue()) * MAX_CHAT_LINES) {
            String message = String.format("[%s]", Text.translatable("commands.fzmm.nbt.tooLong").getString());
            nbtMessage = Text.literal(message).setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));
        } else {
            nbtMessage = toFormatedComponent(nbt.getCompoundOrEmpty(TagsConstant.ENCODE_STACK_COMPONENTS), true);
        }

        // if the hover text is too long it gives a lot of lag with cursor over it (and doesn't fit on the screen)
        if (nbtLength > MAX_HOVER_LENGTH) {
            nbtStringHover = "..." + nbtStringHover.substring(nbtLength - MAX_HOVER_LENGTH, nbtLength);
        }

        Text clickToCopyMessage = Text.literal(" (").append(Text.translatable("commands.fzmm.nbt.click")).append(")")
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));

        MutableText message = Text.empty()
                .append(Text.literal(stack.getItem().toString())
                        .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR))
                ).append(nbtMessage.copy().setStyle(nbtMessage.getStyle()
                                .withClickEvent(new ClickEvent.CopyToClipboard(nbtString))
                                .withHoverEvent(new HoverEvent.ShowText(Text.literal(nbtStringHover))))
                        .append(clickToCopyMessage)
                );

        Text length = Text.literal(String.valueOf(nbtLength))
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_WHITE_COLOR));
        MutableText lengthMessage = Text.translatable("commands.fzmm.nbt.length", length)
                .setStyle(Style.EMPTY.withColor(FzmmClient.CHAT_BASE_COLOR));

        client.inGameHud.getChatHud().addMessage(message.append("\n").append(lengthMessage));
    }

    public static MutableText toFormatedComponent(NbtCompound nbt, boolean prettyPrint) {
        MutableText result = Text.literal("[");
        List<Text> componentsText = new ArrayList<>();

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
