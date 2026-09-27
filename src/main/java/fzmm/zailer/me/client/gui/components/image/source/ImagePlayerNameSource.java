package fzmm.zailer.me.client.gui.components.image.source;

import com.mojang.authlib.GameProfile;
import fzmm.zailer.me.builders.HeadBuilder;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.SuggestionTextBox;
import fzmm.zailer.me.client.gui.components.image.ImageStatus;
import fzmm.zailer.me.client.gui.utils.context_menu.ICtxComponent;
import fzmm.zailer.me.utils.SuggestionUtils;
import fzmm.zailer.me.utils.skin.CacheSkinGetter;
import fzmm.zailer.me.utils.skin.SkinGetterDecorator;
import fzmm.zailer.me.utils.skin.VanillaSkinGetter;
import io.wispforest.owo.itemgroup.Icon;
import net.minecraft.client.Minecraft;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ImagePlayerNameSource implements IImageLoaderFromText, SuggestionTextBox.IProvider<GameProfile> {
    private static final String REGEX = "^[a-zA-Z0-9_]{2,16}$";
    private BufferedImage image;
    private final SkinGetterDecorator skinGetter = new CacheSkinGetter(new VanillaSkinGetter());

    public ImagePlayerNameSource() {
        this.image = null;
    }

    @Override
    public ImageStatus loadImage(String value) {
        if (this.image != null) {
            this.image.flush();
        }
        this.image = null;

        try {
            if (!this.predicate(value)) return ImageStatus.INVALID_USERNAME;

            var skinOptional = this.skinGetter.getSkin(value);
            if (skinOptional.isEmpty()) {
                FzmmClient.LOGGER.warn("[ImageUtils] skin of '{}' was not found", value);
                return this.predicateOnlinePlayer(value) ? ImageStatus.PLAYER_HAS_NO_SKIN : ImageStatus.PLAYER_NOT_FOUND;
            } else {
                this.image = skinOptional.get();
            }

            return ImageStatus.IMAGE_LOADED;
        } catch (Exception e) {
            FzmmClient.LOGGER.error("Unexpected error loading an image", e);
            return ImageStatus.UNEXPECTED_ERROR;
        }
    }

    @Override
    public Optional<BufferedImage> getImage() {
        return Optional.ofNullable(this.image);
    }

    @Override
    public boolean predicate(String value) {
        return this.isValidName(value) || this.predicateOnlinePlayer(value);
    }

    private boolean isValidName(String value) {
        return value.matches(REGEX);
    }

    private boolean predicateOnlinePlayer(String value) {
        // supports users that are not allowed by the regex, as long as that player is online
        assert Minecraft.getInstance().getConnection() != null;
        return Minecraft.getInstance().getConnection().getPlayerInfoIgnoreCase(value) != null;
    }

    @Override
    public boolean hasTextField() {
        return true;
    }

    @Override
    public List<GameProfile> from(String input) {
//        if (this.predicate(input)) {
//            this.skinGetter.getProfile(input).ifPresent(result::add);
//        }
        return new ArrayList<>(SuggestionUtils.filterPlayers(input));
    }

    @Override
    public ICtxComponent<String> toComponent(GameProfile value, String input) {
        return ICtxComponent.simple(
                SuggestionUtils.createComponent(this.toSuggestion(value), input, ICtxComponent.ACCENT_COLOR),
                Icon.of(() -> HeadBuilder.of(value))
        );
    }

    @Override
    public String toSuggestion(GameProfile value) {
        return value.name();
    }
}
