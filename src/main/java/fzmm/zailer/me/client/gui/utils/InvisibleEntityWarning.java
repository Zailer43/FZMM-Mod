package fzmm.zailer.me.client.gui.utils;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.extend.container.EFlowLayout;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.OverlayContainer;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Insets;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Style;

public class InvisibleEntityWarning {

    public static void add(boolean isArmorStand, boolean isInvisible, net.minecraft.network.chat.Component customEntity, String tag) {
        Minecraft.getInstance().execute(() ->
                SnackBarManager.getInstance().add(BaseSnackBarComponent.builder(SnackBarManager.INVISIBLE_ENTITY_ID)
                        .backgroundColor(EStyles.ALERT_WARNING_COLOR)
                        .title(net.minecraft.network.chat.Component.translatable("fzmm.snack_bar.entityDifficultToRemove.title"))
                        .button(iSnackBarComponent -> UIComponents.button(net.minecraft.network.chat.Component.translatable("fzmm.snack_bar.entityDifficultToRemove.button"), buttonComponent -> {
                            addOverlay(isArmorStand, isInvisible, customEntity, tag);
                            iSnackBarComponent.close();
                        }))
                        .closeButton()
                        .mediumTimer()
                        .startTimer()
                        .build()
                ));
    }

    public static void addOverlay(boolean isArmorStand, boolean isInvisible, net.minecraft.network.chat.Component customEntity, String tag) {
        if (!(Minecraft.getInstance().gui.screen() instanceof BaseFzmmScreen baseFzmmScreen)) {
            FzmmClient.LOGGER.warn("[InvisibleEntityWarning] Failed to add overlay, root is not a FlowLayout");
            return;
        }
        EFlowLayout overlayLayout = EContainers.verticalFlow(Sizing.fill(80), Sizing.fill(80));
        FlowLayout overlayTextLayout = EContainers.verticalFlow(Sizing.fill(100), Sizing.content());
        FlowLayout overlayButtonLayout = EContainers.verticalFlow(Sizing.fill(100), Sizing.content());
        OverlayContainer<FlowLayout> overlayContainer = UIContainers.overlay(overlayLayout);

        overlayLayout.padding(Insets.of(8));
        overlayTextLayout.gap(4);
        overlayTextLayout.margins(Insets.bottom(32));
        overlayTextLayout.padding(Insets.horizontal(2));

        overlayTextLayout.child(EComponents.label(net.minecraft.network.chat.Component.translatable("fzmm.snack_bar.entityDifficultToRemove.introduction")).horizontalSizing(Sizing.expand(100)));

        overlayTextLayout.child(getLine());
        overlayTextLayout.child(getTitle(net.minecraft.network.chat.Component.translatable("fzmm.snack_bar.entityDifficultToRemove.operator.title")));
        overlayTextLayout.child(getCopyButton("/kill @e[tag=" + tag + "] ", net.minecraft.network.chat.Component.translatable("fzmm.snack_bar.entityDifficultToRemove.operator.kill", customEntity)));
        overlayTextLayout.child(getCopyButton("/kill @e[tag=" + tag + ",distance=..3] ", net.minecraft.network.chat.Component.translatable("fzmm.snack_bar.entityDifficultToRemove.operator.killDistance", customEntity)));
        if (isInvisible) {
            overlayTextLayout.child(getCopyButton("/execute as @e[tag=" + tag + ",distance=..3] run data merge entity @s {Invisible:0b}", net.minecraft.network.chat.Component.translatable("fzmm.snack_bar.entityDifficultToRemove.operator.removeInvisibility", customEntity)));
        }
        overlayTextLayout.child(getLine());

        overlayTextLayout.child(getTitle(net.minecraft.network.chat.Component.translatable("fzmm.snack_bar.entityDifficultToRemove.worldEdit")));
        overlayTextLayout.child(getCopyButton("//cut -e", net.minecraft.network.chat.Component.translatable("fzmm.snack_bar.entityDifficultToRemove.worldEdit.cut")));
        overlayTextLayout.child(getLine());

        if (isArmorStand) {
            overlayTextLayout.child(getTitle(net.minecraft.network.chat.Component.translatable("fzmm.snack_bar.entityDifficultToRemove.armorStand")));
            overlayTextLayout.child(getLabel(net.minecraft.network.chat.Component.translatable("fzmm.snack_bar.entityDifficultToRemove.armorStand.instruction")).horizontalSizing(Sizing.expand(100)));
            overlayTextLayout.child(getLine());
        }

        overlayTextLayout.child(getTitle(net.minecraft.network.chat.Component.translatable("fzmm.snack_bar.entityDifficultToRemove.multiplayerWithoutOperator")));
        overlayTextLayout.child(getLabel(net.minecraft.network.chat.Component.translatable("fzmm.snack_bar.entityDifficultToRemove.multiplayerWithoutOperator.help")).horizontalSizing(Sizing.expand(100)));
        overlayTextLayout.child(getLine());

        overlayTextLayout.child(getTitle(net.minecraft.network.chat.Component.translatable("fzmm.snack_bar.entityDifficultToRemove.plotsquared")));
        overlayTextLayout.child(getLabel(net.minecraft.network.chat.Component.translatable("fzmm.snack_bar.entityDifficultToRemove.plotsquared.clear")).horizontalSizing(Sizing.expand(100)));

        overlayButtonLayout.child(UIComponents.button(net.minecraft.network.chat.Component.translatable("fzmm.snack_bar.entityDifficultToRemove.done"), buttonComponent ->
                overlayContainer.remove()));

        overlayLayout.child(EContainers.verticalScroll(Sizing.expand(100), Sizing.expand(100), overlayTextLayout))
                .child(overlayButtonLayout);

        overlayLayout.surface(overlayLayout.styledPanel());

        baseFzmmScreen.addOverlay(overlayContainer);
    }

    private static UIComponent getCopyButton(String text, net.minecraft.network.chat.Component translation) {
        return EContainers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(UIComponents.button(net.minecraft.network.chat.Component.translatable("commands.fzmm.nbt.click"), buttonComponent ->
                                SnackBarManager.copyToClipboard(text))
                        .margins(Insets.vertical(3))
                        .tooltip(net.minecraft.network.chat.Component.literal(text))
                ).child(EComponents.label(translation)
                        .horizontalSizing(Sizing.expand(100))
                        .margins(Insets.top(3))
                ).gap(8)
                .horizontalSizing(Sizing.expand(100));
    }

    private static UIComponent getTitle(net.minecraft.network.chat.Component text) {
        return EComponents.label(text.copy().setStyle(Style.EMPTY.withBold(true)))
                .horizontalSizing(Sizing.expand(100));
    }

    private static UIComponent getLabel(net.minecraft.network.chat.Component text) {
        return EComponents.label(text);
    }

    private static UIComponent getLine() {
        return UIComponents.box(Sizing.expand(100), Sizing.fixed(2)).color(Color.WHITE).fill(true);
    }
}
