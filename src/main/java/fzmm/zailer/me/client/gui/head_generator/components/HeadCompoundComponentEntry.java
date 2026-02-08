package fzmm.zailer.me.client.gui.head_generator.components;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.head_generator.HeadGeneratorScreen;
import fzmm.zailer.me.client.gui.head_generator.category.IHeadCategory;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.awt.image.BufferedImage;

public class HeadCompoundComponentEntry extends AbstractHeadComponentEntry {
    private static final Component REMOVE_LAYER_BUTTON_TEXT = Component.translatable("fzmm.gui.button.remove");
    private static long COMPOUND_INDEX = 0;

    public HeadCompoundComponentEntry(AbstractHeadEntry entry, FlowLayout parentLayout, HeadGeneratorScreen parentScreen, BufferedImage initialPreview) {
        super(entry, Sizing.fixed(50), Sizing.fixed(45), parentScreen);
        this.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

        FlowLayout moveButtons = EContainers.horizontalFlow(Sizing.content(), Sizing.content());
        moveButtons.positioning(Positioning.relative(50, 100));
        moveButtons.gap(15);

        ButtonComponent moveUpButton = UIComponents.button(Component.translatable("fzmm.gui.button.arrow.up"),
                buttonComponent -> parentScreen.upCompoundEntry(this));
        moveUpButton.verticalSizing(Sizing.fixed(14));
        moveUpButton.renderer(EStyles.DEFAULT_FLAT_BUTTON);
        
        ButtonComponent moveDownButton = UIComponents.button(Component.translatable("fzmm.gui.button.arrow.down"),
                buttonComponent -> parentScreen.downCompoundEntry(this));
        moveDownButton.verticalSizing(Sizing.fixed(14));
        moveDownButton.renderer(EStyles.DEFAULT_FLAT_BUTTON);

        moveButtons.child(moveUpButton);
        moveButtons.child(moveDownButton);

        this.child(moveButtons);

        for (var button : moveButtons.children()) {
            button.mouseEnter().subscribe(() -> this.mouseEnterEvents.sink().onMouseEnter());
            button.mouseLeave().subscribe(() -> this.mouseLeaveEvents.sink().onMouseLeave());
        }
        this.parent = parentLayout;
        this.updatePreview(initialPreview);
    }

    @Override
    protected void onCloseOverlay() {
        this.parentScreen.updateCompoundPreviews(this, 1);
        this.parentScreen.updateContentPreviews();
    }

    private void removeCompoundEntry(ButtonComponent button) {
        this.overlayContainer.remove();
        this.parentScreen.removeCompound(this);
    }

    @Override
    protected void addTopRightButtons(EFlowLayout panel, FlowLayout layout) {
        Font textRenderer = Minecraft.getInstance().font;

        int addLayerButtonWidth = textRenderer.width(REMOVE_LAYER_BUTTON_TEXT) + BaseFzmmScreen.BUTTON_TEXT_PADDING;
        ButtonComponent removeButton = UIComponents.button(REMOVE_LAYER_BUTTON_TEXT, this::removeCompoundEntry);
        removeButton.horizontalSizing(Sizing.fixed(Math.max(20, addLayerButtonWidth)));

        layout.child(removeButton);

        LabelComponent categoryLabel = panel.childByIdOrThrow(LabelComponent.class, "category-label");
        categoryLabel.text(Component.translatable(IHeadCategory.COMPOUND_CATEGORY.getTranslationKey() + ".label", categoryLabel.text(), IHeadCategory.COMPOUND_CATEGORY.getText()));
    }

    @Override
    protected ClientAsset.Texture getTexture() {
        return new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "head_generator/compound/" + COMPOUND_INDEX++));
    }

}