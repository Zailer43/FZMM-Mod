package fzmm.zailer.me.client.gui.headgenerator.components;

import fzmm.zailer.me.client.logic.headGenerator.texture.HeadTextureEntry;
import fzmm.zailer.me.client.logic.headGenerator.AbstractHeadEntry;
import fzmm.zailer.me.client.renderer.customHead.CustomHeadEntity;
import fzmm.zailer.me.utils.ImageUtils;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.HorizontalFlowLayout;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.Drawer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.image.BufferedImage;
import java.util.Objects;
import java.util.Optional;

import static net.minecraft.client.gui.screen.multiplayer.SocialInteractionsPlayerListEntry.WHITE_COLOR;

public abstract class AbstractHeadListEntry extends HorizontalFlowLayout {
    public static final int PLAYER_SKIN_SIZE = 24;
    protected final AbstractHeadEntry entry;
    private final EntityComponent<CustomHeadEntity> previewComponent;
    protected FlowLayout buttonsLayout;

    public AbstractHeadListEntry(AbstractHeadEntry entry) {
        super(Sizing.fill(100), Sizing.fixed(28));
        this.entry = entry;

        this.previewComponent = Components.entity(Sizing.fixed(PLAYER_SKIN_SIZE), new CustomHeadEntity(MinecraftClient.getInstance().world))
                .allowMouseRotation(true);
        this.previewComponent.margins(Insets.left(4));

        this.buttonsLayout = Containers.horizontalFlow(Sizing.content(), this.verticalSizing().get());
        this.buttonsLayout.alignment(HorizontalAlignment.RIGHT, VerticalAlignment.CENTER);
        this.buttonsLayout.positioning(Positioning.relative(100, 0));

        this.child(this.previewComponent);
        this.child(this.buttonsLayout);
        this.update(new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB), false);
    }

    @Override
    public void draw(MatrixStack matrices, int mouseX, int mouseY, float partialTicks, float delta) {
        if (this.hovered)
            Drawer.fill(matrices, this.x, this.y, this.x + this.width, this.y + this.height, 0x40000000);

        super.draw(matrices, mouseX, mouseY, partialTicks, delta);
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int xWithPadding = this.x + 4;
        int centerY = this.y + (this.height / 2);
        int xText = xWithPadding + 24 + 4;
        Objects.requireNonNull(textRenderer);
        int yText = centerY - textRenderer.fontHeight / 2;

        textRenderer.draw(matrices, this.getDisplayName(), (float) xText, (float) yText, WHITE_COLOR);
    }

    public String getDisplayName() {
        return this.entry.getDisplayName();
    }

    public Optional<BufferedImage> getHeadSkin() {
        if (this.entry instanceof HeadTextureEntry textureEntry)
            return Optional.of(textureEntry.getHeadSkin());
        return Optional.empty();
    }

    public void update(BufferedImage skinBase, boolean overlapHatLayer) {
        MinecraftClient client = MinecraftClient.getInstance();
        TextureManager textureManager = client.getTextureManager();

        client.execute(() -> {
            CustomHeadEntity customHeadEntity = this.previewComponent.entity();
            if (customHeadEntity.getCustomHeadTexture() != null)
                textureManager.destroyTexture(customHeadEntity.getCustomHeadTexture());

            BufferedImage previewSkin = this.entry.getHeadSkin(skinBase, overlapHatLayer);
            ImageUtils.toNativeImage(previewSkin).ifPresent(nativeImage -> {
                NativeImageBackedTexture preview = new NativeImageBackedTexture(nativeImage);
                customHeadEntity.setCustomHeadTexture(textureManager.registerDynamicTexture("fzmm_head", preview));
            });

            textureManager.bindTexture(customHeadEntity.getCustomHeadTexture());
        });
    }
}