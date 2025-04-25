package fzmm.zailer.me.client.command.argument_type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.CommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class VersionArgumentType implements ArgumentType<Pair<String, Integer>> {
    private static final List<String> EXAMPLES = List.of("1.20.4", "1.12.2");
    public static final List<Pair<String, Integer>> VERSIONS;

    public Pair<String, Integer> parse(StringReader stringReader) throws CommandSyntaxException {
        return parse(stringReader.readUnquotedString());
    }

    public static Pair<String, Integer> parse(String value) throws CommandSyntaxException {
        for (var pair : VERSIONS) {
            if (pair.getLeft().equals(value)) {
                return pair;
            }
        }
        Text errorText = Text.translatable("commands.fzmm.version_argument.message", VERSIONS.get(0).getLeft(), VERSIONS.get(VERSIONS.size() - 1).getLeft());
        throw new CommandSyntaxException(new SimpleCommandExceptionType(errorText), errorText);
    }

    public static VersionArgumentType version() {
        return new VersionArgumentType();
    }

    @SuppressWarnings("unchecked")
    public static Pair<String, Integer> getVersion(CommandContext<FabricClientCommandSource> context, String name) {
        return context.getArgument(name, Pair.class);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        List<String> stringVersions = VERSIONS.stream().map(Pair::getLeft).toList();

        return CommandSource.suggestMatching(stringVersions, builder);
    }

    // https://minecraft.wiki/w/Data_version
    private static List<Pair<String, Integer>> initVersions() {
        List<Pair<String, Integer>> result = new ArrayList<>();

        //result.add(new Pair<>("1.21.4", 4325));
        result.add(new Pair<>("1.21.4", 4189));
        result.add(new Pair<>("1.21.3", 4082));
        result.add(new Pair<>("1.21.2", 4080));
        result.add(new Pair<>("1.21.1", 3955));
        result.add(new Pair<>("1.21", 3953));

        result.add(new Pair<>("1.20.6", 3839));
        result.add(new Pair<>("1.20.5", 3837));
        result.add(new Pair<>("1.20.4", 3700));
        result.add(new Pair<>("1.20.3", 3698));
        result.add(new Pair<>("1.20.2", 3578));
        result.add(new Pair<>("1.20.1", 3465));
        result.add(new Pair<>("1.20", 3463));

        result.add(new Pair<>("1.19.4", 3337));
        result.add(new Pair<>("1.19.3", 3218));
        result.add(new Pair<>("1.19.2", 3120));
        result.add(new Pair<>("1.19.1", 3117));
        result.add(new Pair<>("1.19", 3105));

        result.add(new Pair<>("1.18.2", 2975));
        result.add(new Pair<>("1.18.1", 2865));
        result.add(new Pair<>("1.18", 2860));

        result.add(new Pair<>("1.17.1", 2730));
        result.add(new Pair<>("1.17", 2724));

        result.add(new Pair<>("1.16.5", 2586));
        result.add(new Pair<>("1.16.4", 2584));
        result.add(new Pair<>("1.16.3", 2580));
        result.add(new Pair<>("1.16.2", 2578));
        result.add(new Pair<>("1.16.1", 2567));
        result.add(new Pair<>("1.16", 2566));

        result.add(new Pair<>("1.15.2", 2230));
        result.add(new Pair<>("1.15.1", 2227));
        result.add(new Pair<>("1.15", 2225));

        result.add(new Pair<>("1.14.4", 1976));
        result.add(new Pair<>("1.14.3", 1968));
        result.add(new Pair<>("1.14.2", 1963));
        result.add(new Pair<>("1.14.1", 1957));
        result.add(new Pair<>("1.14", 1952));

        result.add(new Pair<>("1.13.2", 1630));
        result.add(new Pair<>("1.13.1", 1628));
        result.add(new Pair<>("1.13", 1519));

        result.add(new Pair<>("1.12.2", 1343));
        result.add(new Pair<>("1.12.1", 1241));
        result.add(new Pair<>("1.12", 1139));

        return List.copyOf(result);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    static {
        VERSIONS = initVersions();
    }
}
