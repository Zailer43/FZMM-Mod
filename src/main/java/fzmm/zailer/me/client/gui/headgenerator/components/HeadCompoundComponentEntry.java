package fzmm.zailer.me.client.gui.headgenerator.components;

import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.headgenerator.HeadGeneratorScreen;
import fzmm.zailer.me.client.gui.headgenerator.category.IHeadCategory;
import fzmm.zailer.me.client.logic.headGenerator.AbstractHeadEntry;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public class HeadCompoundComponentEntry extends AbstractHeadListEntry{
    private static final Text REMOVE_LAYER_BUTTON_TEXT = Text.translatable("fzmm.gui.button.remove");

    public HeadCompoundComponentEntry(AbstractHeadEntry headData, FlowLayout parentLayout, HeadGeneratorScreen parentScreen) {
        super(headData, Sizing.fixed(50), Sizing.fixed(45), parentScreen);
        this.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);

        FlowLayout moveButtons = Containers.horizontalFlow(Sizing.content(), Sizing.content());
        moveButtons.positioning(Positioning.relative(50, 100));
        moveButtons.gap(15);

        ButtonComponent moveUpButton = Components.button(Text.translatable("fzmm.gui.button.arrow.up"),
                buttonComponent -> parentScreen.upCompoundEntry(this));
        moveUpButton.verticalSizing(Sizing.fixed(14));
        moveUpButton.renderer(ButtonComponent.Renderer.flat(0x00000000, 0x40000000, 0x00000000));
        
        ButtonComponent moveDownButton = Components.button(Text.translatable("fzmm.gui.button.arrow.down"),
                buttonComponent -> parentScreen.downCompoundEntry(this));
        moveDownButton.verticalSizing(Sizing.fixed(14));
        moveDownButton.renderer(ButtonComponent.Renderer.flat(0x00000000, 0x40000000, 0x00000000));

        moveButtons.child(moveUpButton);
        moveButtons.child(moveDownButton);

        this.child(moveButtons);

        this.parent = parentLayout;
    }

    private void removeCompoundEntry(ButtonComponent button) {
        assert this.parent != null;

        this.close();
        if (this.parent.children().isEmpty()) {
            Animation<Sizing> layoutAnimation = this.parent.horizontalSizing().animation();
            if (layoutAnimation != null)
                layoutAnimation.backwards();
        }
        this.parentScreen.removeCompound(this);
        this.overlayContainer.remove();
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

}