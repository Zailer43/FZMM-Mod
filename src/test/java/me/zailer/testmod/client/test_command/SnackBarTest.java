package me.zailer.testmod.client.test_command;


import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.Surface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Random;

public class SnackBarTest {

    public static void showTimer() {
        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("1")
                .lowTimer()
                .startTimer()
                .title(Text.literal("low timer (5s)"))
                .build()
        );

        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("2")
                .mediumTimer()
                .startTimer()
                .title(Text.literal("medium timer (10s)"))
                .build()
        );

        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("3")
                .highTimer()
                .startTimer()
                .title(Text.literal("high timer (20s)"))
                .build()
        );

        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("4")
                .lowTimer()
                .button(snackBar -> Components.button(Text.literal("start timer"), buttonComponent -> snackBar.startTimer()))
                .title(Text.literal("low timer (5s)"))
                .build()
        );
    }

    public static void showColor() {
        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("1")
                .mediumTimer()
                .startTimer()
                .closeButton()
                .backgroundColor(EStyles.ALERT_SUCCESS_COLOR)
                .title(Text.literal("success"))
                .build()
        );

        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("2")
                .mediumTimer()
                .startTimer()
                .closeButton()
                .backgroundColor(EStyles.ALERT_WARNING_COLOR)
                .title(Text.literal("warning"))
                .build()
        );

        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("3")
                .mediumTimer()
                .startTimer()
                .closeButton()
                .backgroundColor(EStyles.ALERT_ERROR_COLOR)
                .title(Text.literal("error"))
                .build()
        );

        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("4")
                .mediumTimer()
                .startTimer()
                .closeButton()
                .backgroundColor(EStyles.ALERT_TIP_COLOR)
                .title(Text.literal("tip"))
                .build()
        );

        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("5")
                .mediumTimer()
                .startTimer()
                .closeButton()
                .backgroundColor(EStyles.ALERT_LOADING_COLOR)
                .title(Text.literal("loading"))
                .build()
        );
    }

    public static void showButton() {
        SnackBarManager.getInstance().add(BaseSnackBarComponent.builder("1")
                .backgroundColor(EStyles.ALERT_SUCCESS_COLOR)
                .title(Text.literal("buttons"))
                .sizing(Sizing.fixed(150), Sizing.content())
                .details(Text.literal("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Etiam id vulputate purus. Cras fringilla urna sed nulla porttitor accumsan. Quisque id ex lorem. Donec cursus, leo vitae sollicitudin bibendum, mauris urna ullamcorper ipsum, eget pharetra felis arcu vitae tellus. Cras posuere, velit vitae congue malesuada, quam eros hendrerit mauris, sed aliquam purus justo et ipsum."))
                .closeButton()
                .button(snackBar -> Components.button(Text.literal("random chat number"), buttonComponent -> {
                    int random = new Random(System.currentTimeMillis()).nextInt(100);
                    MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal(String.valueOf(random)));
                }))
                .button(snackBar -> Components.button(Text.literal("random snackbar color"), buttonComponent -> {
                    int color = 0x60000000 + new Random(System.currentTimeMillis()).nextInt(0xFFFFFF);
                    snackBar.surface(Surface.flat(color));
                }))
                .build()
        );
    }
}
