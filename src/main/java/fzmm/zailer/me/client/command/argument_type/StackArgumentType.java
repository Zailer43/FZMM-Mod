package fzmm.zailer.me.client.command.argument_type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.commands.arguments.item.ItemInput;

public class StackArgumentType extends ItemArgument {

    public StackArgumentType(CommandBuildContext commandRegistryAccess) {
        super(commandRegistryAccess);
    }

    public static StackArgumentType item(CommandBuildContext commandRegistryAccess) {
        return new StackArgumentType(commandRegistryAccess);
    }

    @Override
    public ItemInput parse(StringReader stringReader) throws CommandSyntaxException {
        if (!ComponentArgumentType.maxDepthCheck(stringReader)) {
            throw NbtPathArgument.ERROR_DATA_TOO_DEEP.createWithContext(stringReader);
        }
        return super.parse(stringReader);
    }

    @Override
    public <S> ItemInput parse(StringReader stringReader, S source) throws CommandSyntaxException {
        if (!ComponentArgumentType.maxDepthCheck(stringReader)) {
            throw NbtPathArgument.ERROR_DATA_TOO_DEEP.createWithContext(stringReader);
        }
        return super.parse(stringReader, source);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        if (!ComponentArgumentType.maxDepthCheck(new StringReader(context.getInput()))) {
            return builder.buildFuture();
        }
        return super.listSuggestions(context, builder);
    }
}
