package fzmm.zailer.me.client.gui.headgenerator.components;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.headgenerator.HeadGeneratorScreen;
import fzmm.zailer.me.client.logic.headGenerator.HeadData;
import fzmm.zailer.me.client.logic.headGenerator.HeadGenerator;
import fzmm.zailer.me.config.FzmmConfig;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

import java.awt.image.BufferedImage;
import java.util.Set;

public class HeadComponentEntry extends AbstractHeadListEntry {
    public static final Text GIVE_BUTTON_TEXT = Text.translatable("fzmm.gui.button.giveHead");
    private static final Text ADD_LAYER_BUTTON_TEXT = Text.translatable("fzmm.gui.button.add");
    private static final Text FAVORITE_ENABLED_TEXT = Text.translatable("fzmm.gui.button.favorite.enabled");
    private static final Text FAVORITE_ENABLED_EASTER_EGG_TEXT = Text.translatable("fzmm.gui.button.favorite.enabled_easter_egg");
    private static final Text FAVORITE_DISABLED_TEXT = Text.translatable("fzmm.gui.button.favorite.disabled");
    private final ButtonComponent giveButton;
    private final ButtonComponent favoriteButton;
    private boolean isFavorite;
    private boolean hide;
    private final Sizing originalVerticalSizing;
    private final HeadGeneratorScreen parentScreen;

    public HeadComponentEntry(HeadData headData, HeadGeneratorScreen parent) {
        super(headData);
        FzmmConfig.HeadGenerator config = FzmmClient.CONFIG.headGenerator;
        this.isFavorite = config.favoriteSkins().contains(this.headData.key());
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        int giveButtonWidth = textRenderer.getWidth(GIVE_BUTTON_TEXT) + BaseFzmmScreen.BUTTON_TEXT_PADDING;
        this.giveButton = Components.button(GIVE_BUTTON_TEXT, buttonComponent -> this.giveButtonExecute(parent.overlapHatLayerButton()));
        this.giveButton.sizing(Sizing.fixed(giveButtonWidth), Sizing.fixed(20))
                .margins(Insets.right(BaseFzmmScreen.COMPONENT_DISTANCE));

        int addLayerButtonWidth = textRenderer.getWidth(ADD_LAYER_BUTTON_TEXT) + BaseFzmmScreen.BUTTON_TEXT_PADDING;
        ButtonComponent addLayerButton = Components.button(ADD_LAYER_BUTTON_TEXT, this::addLayerButtonExecute);
        addLayerButton.sizing(Sizing.fixed(Math.max(20, addLayerButtonWidth)), Sizing.fixed(20))
                .margins(Insets.right(8));

        int favoriteButtonWidth = Math.max(textRenderer.getWidth(FAVORITE_ENABLED_TEXT), textRenderer.getWidth(FAVORITE_DISABLED_TEXT)) + BaseFzmmScreen.BUTTON_TEXT_PADDING;
        this.favoriteButton = Components.button(Text.empty(), this::favoriteButtonExecute);
        this.favoriteButton.sizing(Sizing.fixed(Math.max(20, favoriteButtonWidth)), Sizing.fixed(20))
                .margins(Insets.right(8));
        this.updateFavoriteText();

        FlowLayout buttonsLayout = Containers.horizontalFlow(this.horizontalSizing().get(), this.verticalSizing().get())
                .child(this.giveButton)
                .child(addLayerButton)
                .child(this.favoriteButton);
        buttonsLayout.alignment(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);

        this.child(buttonsLayout);

        this.hide = false;
        this.originalVerticalSizing = this.verticalSizing().get();
        this.parentScreen = parent;
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        if (!this.hide)
            super.draw(matrices, mouseX, mouseY, partialTicks, delta);
    }

    private void giveButtonExecute(boolean overlapHatLayer) {
        BufferedImage headTexture = this.getHeadSkin();

        BufferedImage image = new HeadGenerator(this.parentScreen.getBaseSkin(), overlapHatLayer)
                .addTexture(headTexture)
                .getHeadTexture();

        this.parentScreen.giveHead(image);
    }

    private void addLayerButtonExecute(ButtonComponent button) {
        this.parentScreen.addLayer(this.headData);
    }

    private void favoriteButtonExecute(ButtonComponent button) {
        FzmmConfig.HeadGenerator config = FzmmClient.CONFIG.headGenerator;
        Set<String> favorites = config.favoriteSkins();

        if (this.isFavorite)
            favorites.remove(this.headData.key());
        else
            favorites.add(this.headData.key());

        config.favoriteSkins(favorites);
        this.isFavorite = !this.isFavorite;
        this.updateFavoriteText(true);
    }

    private void updateFavoriteText() {
        this.updateFavoriteText(false);
    }

    private void updateFavoriteText(boolean easterEgg) {
        Text message;
        if (this.isFavorite) {
            int number = easterEgg ? Random.create().nextBetween(0, 40) : 0;
            message = number == 1 ? FAVORITE_ENABLED_EASTER_EGG_TEXT : FAVORITE_ENABLED_TEXT;
        } else {
            message = FAVORITE_DISABLED_TEXT;
        }

        this.favoriteButton.setMessage(message);
    }

    public void filter(String searchValue, boolean toggledFavorites) {
        if (!this.isFavorite && toggledFavorites) {
            this.setHide(true);
            return;
        }

        this.setHide(!searchValue.isBlank() && !this.getDisplayName().toLowerCase().contains(searchValue));
    }

    public void setHide(boolean value) {
        this.hide = value;
        this.verticalSizing(value ? Sizing.fixed(0) : this.originalVerticalSizing);
    }

    public void updateGiveButton(boolean active, Text text) {
        this.giveButton.active = active;
        this.giveButton.setMessage(text);
    }
}