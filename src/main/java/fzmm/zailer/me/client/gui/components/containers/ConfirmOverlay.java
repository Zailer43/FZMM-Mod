package fzmm.zailer.me.client.gui.components.containers;

import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.core.*;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class ConfirmOverlay extends OverlayContainer<EFlowLayout> {
    private static final int WIDTH = 250;

    public ConfirmOverlay(Text question, Consumer<Boolean> onConfirm) {
        super(EContainers.verticalFlow(Sizing.fixed(WIDTH), Sizing.content()));

        this.addComponents(question, onConfirm);
        this.child.alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER);
        this.child.gap(12);
        this.child.padding(Insets.of(6));
        this.child.surface(this.child.styledPanel());
    }

    protected void addComponents(Text question, Consumer<Boolean> onConfirm) {
        LabelComponent label = EComponents.label(question);
        label.horizontalSizing(Sizing.expand(100));

        FlowLayout buttonLayout = EContainers.horizontalFlow(Sizing.expand(100), Sizing.fixed(20));

        buttonLayout.child(Components.button(Text.translatable("fzmm.gui.confirmDialog.confirm"), buttonComponent -> {
                    onConfirm.accept(true);
                    this.remove();
                }).positioning(Positioning.relative(0, 0))
                .horizontalSizing(Sizing.fixed(100)));

        buttonLayout.child(Components.button(Text.translatable("fzmm.gui.confirmDialog.cancel"), buttonComponent -> {
                    onConfirm.accept(false);
                    this.remove();
                }).positioning(Positioning.relative(100, 0))
                .horizontalSizing(Sizing.fixed(100)));

        this.child.child(label);
        this.child.child(buttonLayout);
    }

}
