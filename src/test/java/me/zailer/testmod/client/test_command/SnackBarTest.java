package me.zailer.testmod.client.test_command;


import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;

import java.util.Random;

public class SnackBarTest {

    public static void showTimer() {
        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("1")
                .lowTimer()
                .startTimer()
                .title(Component.literal("low timer (5s)"))
                .build()
        );

        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("2")
                .mediumTimer()
                .startTimer()
                .title(Component.literal("medium timer (10s)"))
                .build()
        );

        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("3")
                .highTimer()
                .startTimer()
                .title(Component.literal("high timer (20s)"))
                .build()
        );

        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("4")
                .lowTimer()
                .button(snackBar -> UIComponents.button(Component.literal("start timer"), buttonComponent -> snackBar.startTimer()))
                .title(Component.literal("low timer (5s)"))
                .build()
        );
    }

    public static void showColor() {
        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("1")
                .mediumTimer()
                .startTimer()
                .closeButton()
                .backgroundColor(EStyles.ALERT_SUCCESS_COLOR)
                .title(Component.literal("success"))
                .build()
        );

        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("2")
                .mediumTimer()
                .startTimer()
                .closeButton()
                .backgroundColor(EStyles.ALERT_WARNING_COLOR)
                .title(Component.literal("warning"))
                .build()
        );

        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("3")
                .mediumTimer()
                .startTimer()
                .closeButton()
                .backgroundColor(EStyles.ALERT_ERROR_COLOR)
                .title(Component.literal("error"))
                .build()
        );

        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("4")
                .mediumTimer()
                .startTimer()
                .closeButton()
                .backgroundColor(EStyles.ALERT_TIP_COLOR)
                .title(Component.literal("tip"))
                .build()
        );

        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("5")
                .mediumTimer()
                .startTimer()
                .closeButton()
                .backgroundColor(EStyles.ALERT_LOADING_COLOR)
                .title(Component.literal("loading"))
                .build()
        );
    }

    public static void showButton() {
        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("1")
                .backgroundColor(EStyles.ALERT_SUCCESS_COLOR)
                .title(Component.literal("buttons"))
                .sizing(Sizing.fixed(150), Sizing.content())
                .details(Component.literal("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam id vulputate purus. Cras fringilla urna sed nulla porttitor accumsan. Quisque id ex lorem. Donec cursus, leo vitae sollicitudin bibendum, mauris urna ullamcorper ipsum, eget pharetra felis arcu vitae tellus. Cras posuere, velit vitae congue malesuada, quam eros hendrerit mauris, sed aliquam purus justo et ipsum."))
                .closeButton()
                .button(snackBar -> UIComponents.button(Component.literal("random chat number"), buttonComponent -> {
                    int random = new Random(Util.getEpochMillis()).nextInt(100);
                    Minecraft.getInstance().gui.getChat().addMessage(Component.literal(String.valueOf(random)));
                })).button(snackBar -> UIComponents.button(Component.literal("random snackbar color"), buttonComponent -> {
                    int color = 0x60000000 + new Random(Util.getEpochMillis()).nextInt(0xFFFFFF);
                    snackBar.surface(Surface.flat(color));
                })).build()
        );
    }
}
