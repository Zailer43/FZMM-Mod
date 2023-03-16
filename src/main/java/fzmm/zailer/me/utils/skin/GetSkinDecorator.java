package fzmm.zailer.me.utils.skin;

import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;
import java.util.Optional;

public class GetSkinDecorator {
    private final GetSkinDecorator next;

    protected GetSkinDecorator(@Nullable GetSkinDecorator next) {
        this.next = Objects.requireNonNullElseGet(next, () -> new GetSkinDecorator() {
            @Override
            public Optional<BufferedImage> getSkin(String playerName) {
                return Optional.empty();
            }

            @Override
            public Optional<ItemStack> getHead(String playerName) {
                return Optional.empty();
            }
        });
    }

    private GetSkinDecorator() {
        this.next = null;
    }

    public Optional<BufferedImage> getSkin(String value) throws IOException {
        Optional<BufferedImage> skin = this.next.getSkin(value);

        return skin.isEmpty() ? this.next.getSkin(value) : skin;
    }

    public Optional<ItemStack> getHead(String value) {
        Optional<ItemStack> stack = this.next.getHead(value);

        return stack.isEmpty() ? this.next.getHead(value): stack;
    }
}
