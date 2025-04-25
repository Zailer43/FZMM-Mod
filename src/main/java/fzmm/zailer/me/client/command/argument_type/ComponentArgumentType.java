package fzmm.zailer.me.client.command.argument_type;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.command.argument.NbtPathArgumentType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringNbtReader;

import java.util.Arrays;
import java.util.Collection;

public class ComponentArgumentType implements ArgumentType<NbtCompound> {
    private static final int MAX_DEPTH = NbtCompound.MAX_DEPTH - 2;
    private static final Collection<String> EXAMPLES = Arrays.asList("{}", "{foo:'bar'}", "[foo='bar']");

    public static ComponentArgumentType component() {
        return new ComponentArgumentType();
    }

    public static <S> NbtCompound getNbtCompound(CommandContext<S> context, String name) {
        return context.getArgument(name, NbtCompound.class);
    }

    @Override
    public NbtCompound parse(StringReader stringReader) throws CommandSyntaxException {
        if (!maxDepthCheck(stringReader)) {
            throw NbtPathArgumentType.TOO_DEEP_EXCEPTION.createWithContext(stringReader);
        }

        StringNbtReader<NbtElement> nbtReader = StringNbtReader.fromOps(NbtOps.INSTANCE);
        if (stringReader.peek() == '[') {
            return this.parseComponent(stringReader, nbtReader);
        } else {
            return StringNbtReader.readCompoundAsArgument(stringReader);
        }
    }

    private NbtCompound parseComponent(StringReader stringReader, StringNbtReader<NbtElement> nbtReader) throws CommandSyntaxException {
        NbtCompound compound = new NbtCompound();
        StringBuilder keyBuilder = new StringBuilder();

        stringReader.expect('[');
        stringReader.skipWhitespace();
        int oldCursor;

        while (stringReader.canRead()) {
            char c = stringReader.peek();
            oldCursor = stringReader.getCursor();
            stringReader.skip();
            stringReader.skipWhitespace();

            if (c == '=') {
                String key = keyBuilder.toString();
                keyBuilder = new StringBuilder();

                compound.put(key, nbtReader.readAsArgument(stringReader));
            } else if (c == ']') {
                stringReader.setCursor(oldCursor);
                stringReader.skip();
                break;
            } else if (c != ',') {
                keyBuilder.append(c);
            }
        }

        return compound;
    }

    /**
     * This is necessary because FZMM increases the character limit of the '/fzmm' command,
     * and vanilla doesn't check for nbt depth.
     * @return {@code true} if depth is valid (less than {@link #MAX_DEPTH})
     */
    public static boolean maxDepthCheck(StringReader stringReader) {
        int originalCursor = stringReader.getCursor();
        int curlyDepth = 0;
        int squareDepth = 0;
        boolean inString = false;
        int inStringEscapeDepth = 0;
        char stringDelimiter = '\0';
        boolean result = true;

        while (stringReader.getRemainingLength() > 0) {
            char current = stringReader.peek();
            int cursor = stringReader.getCursor();
            stringReader.skip();

            // Handle string context
            if (inString) {
                if (current == stringDelimiter  && inStringEscapeDepth == countRepeatedPrevious(stringReader, cursor, '\\')) {
                    // Exit string if delimiter matches and escape depth matches
                    // Example \\' only closes \\' and ' only closes '
                    inString = false;
                }
                continue;
            }

            // Enter string context
            if (current == '"' || current == '\'') {
                inString = true;
                stringDelimiter = current;
                inStringEscapeDepth = countRepeatedPrevious(stringReader, cursor, '\\');
                continue;
            }

            // Track depth of brackets
            if (current == '{') {
                curlyDepth++;
            } else if (current == '}') {
                curlyDepth--;
                if (curlyDepth < 0) {
                    break; // Unmatched }
                }
            } else if (current == '[') {
                squareDepth++;
            } else if (current == ']') {
                squareDepth--;
                if (squareDepth < 0) {
                    break; // Unmatched ]
                }
            }

            // Check depth
            if ((curlyDepth + squareDepth) > MAX_DEPTH) {
                result = false;
                break;
            }
        }

        stringReader.setCursor(originalCursor);
        return result;
    }

    public static int countRepeatedPrevious(StringReader stringReader, int cursor, char c) {
        int result = 0;
        int originalCursor = stringReader.getCursor();
        stringReader.setCursor(cursor);
        while (stringReader.peek(-result) == c) {
            result++;
        }

        stringReader.setCursor(originalCursor);
        return result;
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }
}
