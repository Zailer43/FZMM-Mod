package fzmm.zailer.me.client.gui.head_generator.components;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.head_generator.HeadGeneratorScreen;
import fzmm.zailer.me.client.gui.head_generator.category.IHeadCategory;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmUtils;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.Set;

public class HeadComponentEntry extends AbstractHeadComponentEntry {
    private static final Component ADD_LAYER_BUTTON_TEXT = Component.translatable("fzmm.gui.button.add");
    public static final Component FAVORITE_ENABLED_TEXT = Component.translatable("fzmm.gui.button.favorite.enabled").setStyle(Style.EMPTY.withColor(0xECC709));
    private static final Component FAVORITE_ENABLED_EASTER_EGG_TEXT = Component.translatable("fzmm.gui.button.favorite.enabled_easter_egg").setStyle(Style.EMPTY.withColor(0xF4300B));
    public static final Component FAVORITE_DISABLED_TEXT = Component.translatable("fzmm.gui.button.favorite.disabled").setStyle(Style.EMPTY.withColor(0xECC709));
    private static final int FAVORITE_BUTTON_WIDTH = getFavoriteButtonWidth();
    private final ButtonComponent favoriteButton;
    private boolean isFavorite;
    private boolean hide;

    public HeadComponentEntry(AbstractHeadEntry headData, HeadGeneratorScreen parent) {
        super(headData, Sizing.fixed(40), Sizing.fixed(30), parent);
        FzmmConfig.HeadGenerator config = FzmmClient.CONFIG.headGenerator;
        this.isFavorite = config.favoriteSkins().contains(this.entry.getKey());

        this.favoriteButton = UIComponents.button(Component.empty(), this::favoriteButtonExecute);
        this.setupFavoriteButton(this.favoriteButton);
        this.favoriteButton.positioning(Positioning.relative(100, 0));
        this.favoriteButton.verticalSizing(Sizing.fixed(16));
        this.updateFavoriteText(this.favoriteButton, false);
        this.favoriteButton.visible = false;

        this.mouseEnter().subscribe(() -> this.favoriteButton.visible = true);
        this.mouseLeave().subscribe(() -> {
            if (!this.hovered)
                this.favoriteButton.visible = false;
        });

        this.child(this.favoriteButton);

        for (var entry : this.children()) {
            entry.mouseEnter().subscribe(() -> this.mouseEnterEvents.sink().onMouseEnter());
            entry.mouseLeave().subscribe(() -> this.mouseLeaveEvents.sink().onMouseLeave());
        }

        this.hide = false;
    }

    private void setupFavoriteButton(ButtonComponent favoriteButton) {
        favoriteButton.onPress(this::favoriteButtonExecute);
        favoriteButton.renderer(ButtonComponent.Renderer.flat(0x00000000, 0x00000000, 0x00000000));
        favoriteButton.sizing(Sizing.fixed(FAVORITE_BUTTON_WIDTH));
    }

    private static int getFavoriteButtonWidth() {
        return Math.max(15,
                FzmmUtils.getMaxWidth(List.of(FAVORITE_ENABLED_TEXT, FAVORITE_DISABLED_TEXT)) + BaseFzmmScreen.BUTTON_TEXT_PADDING
        );
    }

    private void favoriteButtonExecute(ButtonComponent button) {
        FzmmConfig.HeadGenerator config = FzmmClient.CONFIG.headGenerator;
        Set<String> favorites = config.favoriteSkins();

        if (this.isFavorite)
            favorites.remove(this.entry.getKey());
        else
            favorites.add(this.entry.getKey());

        config.favoriteSkins(favorites);
        this.isFavorite = !this.isFavorite;
        this.updateFavoriteText(button, true);
    }

    private void updateFavoriteText(ButtonComponent favoriteButton, boolean easterEgg) {
        Component message;
        if (this.isFavorite) {
            int number = easterEgg ? RandomSource.create().nextIntBetweenInclusive(0, 40) : 0;
            message = number == 1 ? FAVORITE_ENABLED_EASTER_EGG_TEXT : FAVORITE_ENABLED_TEXT;
        } else {
            message = FAVORITE_DISABLED_TEXT;
        }

        favoriteButton.setMessage(message);
    }

    public void filter(String searchValue, boolean toggledFavorites, IHeadCategory headCategory) {
        if (!this.isFavorite && toggledFavorites || !headCategory.isCategory(this.entry, this.entry.getCategoryId())) {
            this.hide = true;
            return;
        }

        if (searchValue.isEmpty()) {
            this.hide = false;
            return;
        }

        this.hide = (!searchValue.isBlank() && !this.getFilterValue().contains(searchValue));
    }

    public boolean isHide() {
        return this.hide;
    }

    @Override
    protected void addTopRightButtons(EFlowLayout panel, FlowLayout layout) {
        Font textRenderer = Minecraft.getInstance().font;

        ButtonComponent overlayFavoriteButton = UIComponents.button(Component.empty(), buttonComponent -> {});
        overlayFavoriteButton.setMessage(this.favoriteButton.getMessage());
        this.setupFavoriteButton(overlayFavoriteButton);

        int addLayerButtonWidth = textRenderer.width(ADD_LAYER_BUTTON_TEXT) + BaseFzmmScreen.BUTTON_TEXT_PADDING;
        ButtonComponent addCompoundButton = UIComponents.button(ADD_LAYER_BUTTON_TEXT, this::addCompoundButtonExecute);
        addCompoundButton.horizontalSizing(Sizing.fixed(Math.max(20, addLayerButtonWidth)));

        layout.children(List.of(overlayFavoriteButton, addCompoundButton));
    }

    @Override
    protected ClientAsset.Texture getTexture() {
        return new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "head_generator/preview/" + this.entry.getKey()));
    }

    private void addCompoundButtonExecute(ButtonComponent button) {
        this.parentScreen.addCompound(this.entry, this.getPreview());
    }
}