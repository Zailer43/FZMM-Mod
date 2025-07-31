package fzmm.zailer.me.client.gui.head_generator.components;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.entity.custom_skin.CustomHeadEntity;
import fzmm.zailer.me.client.entity.custom_skin.CustomPlayerSkinEntity;
import fzmm.zailer.me.client.entity.custom_skin.ISkinMutable;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.head_generator.HeadGeneratorScreen;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import fzmm.zailer.me.utils.ImageUtils;
import fzmm.zailer.me.utils.SkinPart;
import fzmm.zailer.me.utils.list.IListEntry;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.EntityComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.util.UIErrorToast;
import io.wispforest.owo.ui.util.UISounds;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;

public abstract class AbstractHeadComponentEntry extends EFlowLayout implements IListEntry<AbstractHeadEntry> {
    public static final int HEAD_PREVIEW_SIZE = 24;
    public static final int BODY_PREVIEW_SIZE = 12;
    protected final HeadGeneratorScreen parentScreen;
    private final NativeImageBackedTexture previewTexture;
    @Nullable
    private Identifier textureId;
    protected AbstractHeadEntry entry;
    private EntityComponent<LivingEntity> previewComponent;
    protected OverlayContainer<FlowLayout> overlayContainer;
    private boolean isBodyPreview;

    public AbstractHeadComponentEntry(AbstractHeadEntry entry, Sizing horizontalSizing, Sizing verticalSizing, HeadGeneratorScreen parent) {
        super(horizontalSizing, verticalSizing, Algorithm.VERTICAL);
        this.entry = entry;
        this.textureId = this.getTextureId();
        this.setBodyPreview(entry.isEditingSkinBody());

        this.alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER);
        this.gap(BaseFzmmScreen.COMPONENT_DISTANCE);
        this.cursorStyle(CursorStyle.HAND);

        this.parentScreen = parent;

        this.mouseDown().subscribe((mouseX, mouseY, button) -> {
            try {
                this.addOverlay(parent);
            } catch (Exception e) {
                //noinspection UnstableApiUsage
                UIErrorToast.report(e);
                FzmmClient.LOGGER.error("[AbstractHeadComponentEntry] Failed to add overlay", e);
            }
            UISounds.playInteractionSound();
            return true;
        });

        this.hoveredSurface(EStyles.DEFAULT_HOVERED);

        BufferedImage defaultPreview = entry.getHeadSkin(new BufferedImage(SkinPart.MAX_WIDTH, SkinPart.MAX_HEIGHT, BufferedImage.TYPE_INT_ARGB), false);
        this.previewTexture = new NativeImageBackedTexture(this.textureId::toString, ImageUtils.toNativeImage(defaultPreview));
        MinecraftClient.getInstance().getTextureManager().registerTexture(this.textureId, this.previewTexture);
    }

    public boolean isBodyPreview() {
        return this.isBodyPreview;
    }

    public void setBodyPreview(boolean isBody) {
        if (this.textureId == null)
            return;
        this.isBodyPreview = isBody;
        LivingEntity previewEntity;
        float scale;
        if (isBody) {
            scale = 0.45f;
            previewEntity = new CustomPlayerSkinEntity(MinecraftClient.getInstance().world);
        } else {
            scale = 1f;
            previewEntity = new CustomHeadEntity(MinecraftClient.getInstance().world);
        }
        ((ISkinMutable) previewEntity).texture(this.textureId);

        this.removeChild(this.previewComponent);
        this.previewComponent = EComponents.entity(Sizing.fixed(HEAD_PREVIEW_SIZE), previewEntity);
        this.previewComponent.scale(scale)
                .cursorStyle(CursorStyle.HAND)
                .tooltip(this.entry.getDisplayName());
        this.child(this.previewComponent);
    }

    public String getFilterValue() {
        return this.entry.getFilterValue();
    }

    public String getCategoryId() {
        return this.entry.getCategoryId();
    }

    /**
     * Update preview with base skin
     */
    public void basePreview(BufferedImage baseSkin, boolean hasUnusedPixels) {
        this.updatePreview(this.entry.getHeadSkin(baseSkin, hasUnusedPixels));
    }

    /**
     * @param previewSkin Update preview with {@link BufferedImage}
     */
    public void updatePreview(BufferedImage previewSkin) {
        if (this.textureId == null)
            return;
        NativeImage nativeImage = ImageUtils.toNativeImage(previewSkin);
        this.previewTexture.setImage(nativeImage);
        this.previewTexture.upload();
    }

    public void updateModel(boolean isSlim) {
        if (this.previewComponent.entity() instanceof ISkinMutable previewEntity) {
            previewEntity.model(isSlim);
        }
    }

    protected EntityComponent<LivingEntity> copyCustomHeadEntity() {
        return EComponents.entity(this.previewComponent.horizontalSizing().get(), this.previewComponent.entity())
                .scale(this.previewComponent.scale());
    }

    public void close() {
        MinecraftClient.getInstance().getTextureManager().destroyTexture(this.textureId);
        this.textureId = null;
    }

    @Override
    public void remove() {
        super.remove();
        this.close();
    }

    public BufferedImage getPreview() {
        NativeImage nativeImage = this.previewTexture.getImage();
        if (nativeImage == null) {
            FzmmClient.LOGGER.warn("[AbstractHeadListEntry] Failed to get preview image for {}", this.entry.getDisplayName().getString());
            return new BufferedImage(SkinPart.MAX_WIDTH, SkinPart.MAX_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        }

        return ImageUtils.getBufferedImgFromNativeImg(nativeImage);
    }

    protected void addOverlay(HeadGeneratorScreen parent) {
        EntityComponent<LivingEntity> previewEntity = this.copyCustomHeadEntity().allowMouseRotation(true);
        FlowLayout overlayLayout = new HeadComponentOverlay(parent, previewEntity, this);

        this.overlayContainer = new OverlayContainer<>(overlayLayout) {
            @Override
            public void remove() {
                super.remove();
                onCloseOverlay();
            }
        };
        this.overlayContainer.zIndex(300);
        parent.addOverlay(this.overlayContainer);
    }

    protected void onCloseOverlay() {

    }

    protected abstract void addTopRightButtons(EFlowLayout panel, FlowLayout layout);


    protected abstract Identifier getTextureId();

    @Override
    public AbstractHeadEntry getValue() {
        return this.entry;
    }

    @Override
    public void setValue(AbstractHeadEntry value) {
        this.entry = value;
        this.previewComponent.tooltip(value.getDisplayName());
    }
}