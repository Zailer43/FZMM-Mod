package fzmm.zailer.me.client.gui.head_generator.components;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.style.FzmmStyles;
import fzmm.zailer.me.client.gui.components.style.StyledContainers;
import fzmm.zailer.me.client.gui.head_generator.HeadGeneratorScreen;
import fzmm.zailer.me.client.gui.head_generator.category.IHeadCategory;
import fzmm.zailer.me.client.logic.head_generator.AbstractHeadEntry;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.awt.image.BufferedImage;

public class HeadCompoundComponentEntry extends AbstractHeadComponentEntry {
    private static final Text REMOVE_LAYER_BUTTON_TEXT = Text.translatable("fzmm.gui.button.remove");
    private static long COMPOUND_INDEX = 0;

    public HeadCompoundComponentEntry(AbstractHeadEntry entry, FlowLayout parentLayout, HeadGeneratorScreen parentScreen, BufferedImage initialPreview) {
        super(entry, Sizing.fixed(50), Sizing.fixed(45), parentScreen);
        this.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

        FlowLayout moveButtons = StyledContainers.horizontalFlow(Sizing.content(), Sizing.content());
        moveButtons.positioning(Positioning.relative(50, 100));
        moveButtons.gap(15);

        ButtonComponent moveUpButton = Components.button(Text.translatable("fzmm.gui.button.arrow.up"),
                buttonComponent -> parentScreen.upCompoundEntry(this));
        moveUpButton.verticalSizing(Sizing.fixed(14));
        moveUpButton.renderer(FzmmStyles.DEFAULT_FLAT_BUTTON);
        
        ButtonComponent moveDownButton = Components.button(Text.translatable("fzmm.gui.button.arrow.down"),
                buttonComponent -> parentScreen.downCompoundEntry(this));
        moveDownButton.verticalSizing(Sizing.fixed(14));
        moveDownButton.renderer(FzmmStyles.DEFAULT_FLAT_BUTTON);

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
    protected void addTopRightButtons(FlowLayout panel, FlowLayout layout) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        int addLayerButtonWidth = textRenderer.getWidth(REMOVE_LAYER_BUTTON_TEXT) + BaseFzmmScreen.BUTTON_TEXT_PADDING;
        ButtonComponent removeButton = Components.button(REMOVE_LAYER_BUTTON_TEXT, this::removeCompoundEntry);
        removeButton.horizontalSizing(Sizing.fixed(Math.max(20, addLayerButtonWidth)));

        layout.child(removeButton);

        LabelComponent categoryLabel = panel.childById(LabelComponent.class, "category-label");
        BaseFzmmScreen.checkNull(categoryLabel, "label", "category-label");
        categoryLabel.text(Text.translatable(IHeadCategory.COMPOUND_CATEGORY.getTranslationKey() + ".label", categoryLabel.text(), IHeadCategory.COMPOUND_CATEGORY.getText()));
    }

    @Override
    protected Identifier getTextureId() {
        return Identifier.of(FzmmClient.MOD_ID, "head_generator/compound/" + COMPOUND_INDEX++);
    }

}