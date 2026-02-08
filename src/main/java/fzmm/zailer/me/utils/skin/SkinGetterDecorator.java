package fzmm.zailer.me.utils.skin;

import com.mojang.authlib.GameProfile;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;

public abstract class SkinGetterDecorator {
    private final SkinGetterDecorator next;

    protected SkinGetterDecorator(@Nullable SkinGetterDecorator next) {
        this.next = next;
    }

    protected SkinGetterDecorator() {
        this(null);
    }

    /**
     * Get the player's skin as a {@link BufferedImage}. If not found, delegates to the next decorator
     *
     * @param playerName the name of the player to get the skin of
     */
    public Optional<BufferedImage> getSkin(String playerName) {
        if (this.next == null) {
            return Optional.empty();
        }

        return this.next.getSkin(playerName);
    }

    /**
     * Retrieves the player's head as an {@link ItemStack}. If not found, delegates to the next decorator
     *
     * @param playerName the name of the player to get the head of
     */
    public Optional<ItemStack> getHead(String playerName) {
        if (this.next == null) {
            return Optional.empty();
        }

        return this.next.getHead(playerName);
    }

    /**
     * @param playerName the name of the player to get the profile of
     */
    protected abstract Optional<GameProfile> getProfile(String playerName);
}
