package fzmm.zailer.me.client.gui.head_gallery.components;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.component.ELabelComponent;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.head_gallery.controller.AbstractGalleryContent;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchHead;
import fzmm.zailer.me.client.logic.minecraft_heads.model.MchTier;
import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.TextUtils;
import io.wispforest.owo.ui.component.CheckboxComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;
import java.util.function.Consumer;

public class GalleryItemDisplayOverlay extends OverlayContainer<EFlowLayout> {
    private final Consumer<AbstractGalleryContent.Display> callback;
    private CheckboxComponent showDisplay;
    private CheckboxComponent showTags;
    private CheckboxComponent showPublishedAt;
    private CheckboxComponent showId;
    private ELabelComponent preview;

    public GalleryItemDisplayOverlay(AbstractGalleryContent.Display values, Consumer<AbstractGalleryContent.Display> callback) {
        super(EContainers.verticalFlow(Sizing.fixed(250), Sizing.content()));

        this.callback = callback;
        this.buildComponents(values);
    }

    private void buildComponents(AbstractGalleryContent.Display values) {
        this.child.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        this.child.gap(12);
        this.child.padding(Insets.of(6));
        this.child.surface(this.child.styledPanel());

        EFlowLayout optionsWrapper = this.buildOptions(values);
        optionsWrapper.child(this.buildPreview());

        this.child.child(EComponents.label(Component.translatable("fzmm.gui.headGallery.option.display")));
        this.child.child(optionsWrapper);
        this.child.child(this.buildBottomButtons());
    }

    private EFlowLayout buildOptions(AbstractGalleryContent.Display values) {
        EFlowLayout result = EContainers.horizontalFlow(Sizing.content(), Sizing.content());
        result.gap(4);
        EFlowLayout optionsLayout = EContainers.verticalFlow(Sizing.expand(100), Sizing.content());
        optionsLayout.gap(2);

        this.showDisplay = this.getOption("Display", MchTier.HEADS_BASIC_DATA, values.enabled());
        optionsLayout.child(this.showDisplay);
        this.showTags = this.getOption("Tags", MchTier.HEADS_ADD_DATA_TAGS, values.tags());
        optionsLayout.child(this.showTags);
        this.showPublishedAt = this.getOption("PublishedAt", MchTier.HEADS_ADD_DATA_FREE, values.publishedAt());
        optionsLayout.child(this.showPublishedAt);
        this.showId = this.getOption("Id", MchTier.HEADS_ADD_DATA_FREE, values.id());
        optionsLayout.child(this.showId);

        result.child(optionsLayout);

        return result;
    }

    private UIComponent buildPreview() {
        EFlowLayout result = EContainers.verticalFlow(Sizing.content(), Sizing.content());
        result.surface(Surface.TOOLTIP).padding(Insets.of(4));
        this.preview = EComponents.label(Component.empty());
        this.updatePreview();
        result.child(this.preview);

        return result;
    }

    private UIComponent buildBottomButtons() {
        FzmmConfig.HeadGallery.Display config = FzmmClient.CONFIG.headGallery.display;
        EFlowLayout result = EContainers.horizontalFlow(Sizing.expand(100), Sizing.fixed(16));
        result.child(
                EComponents.button(Component.translatable("fzmm.gui.button.saveConfig"))
                        .onPress(button -> {
                            config.enabled(this.showDisplay.selected());
                            config.tags(this.showTags.selected());
                            config.publishedAt(this.showPublishedAt.selected());
                            config.id(this.showId.selected());
                            FzmmClient.CONFIG.save();
                            this.remove();
                        }).verticalSizing(Sizing.fixed(16))
                        .positioning(Positioning.relative(0, 100))
        );

        return result;
    }

    private CheckboxComponent getOption(String translationKey, int permission, boolean enabled) {
        Component text = Component.translatable("fzmm.gui.headGallery.option.display.overlay.show" + translationKey);
        if (!FzmmClient.MCH_RESOURCES.licenseDetected().hasPermission(permission)) {
            MchTier minLicense = MchTier.minTierRequired(permission);
            text = Component.translatable("fzmm.gui.headGallery.tier.missingPermission", text, minLicense.message());
        }
        CheckboxComponent checkbox = UIComponents.checkbox(text);
        checkbox.checked(enabled);
        checkbox.onChanged(aBoolean -> this.updatePreview());

        return checkbox;
    }

    public void updatePreview() {
        MchHead head = FzmmClient.MCH_RESOURCES.heads().stream().toList().get(0);
        ItemStack previewStack = AbstractGalleryContent.toStack(head, this.toDisplay());
        Minecraft client = Minecraft.getInstance();
        List<Component> tooltip = previewStack.getTooltipLines(Item.TooltipContext.of(client.level), client.player, TooltipFlag.NORMAL);
        this.preview.text(TextUtils.mergeText(tooltip));
    }

    public AbstractGalleryContent.Display toDisplay() {
        return new AbstractGalleryContent.Display(
                this.showDisplay.selected(),
                this.showTags.selected(),
                this.showPublishedAt.selected(),
                this.showId.selected(),
                FzmmClient.CONFIG.colors.headGalleryName().rgb(),
                FzmmClient.CONFIG.colors.headGalleryLore().rgb()
        );
    }

    @Override
    public void dismount(DismountReason reason) {
        super.dismount(reason);
        this.callback.accept(this.toDisplay());
    }
}
