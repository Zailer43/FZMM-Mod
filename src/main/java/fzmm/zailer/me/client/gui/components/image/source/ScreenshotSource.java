package fzmm.zailer.me.client.gui.components.image.source;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.Window;
import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.extend.EComponents;
import fzmm.zailer.me.client.gui.components.extend.EContainers;
import fzmm.zailer.me.client.gui.components.extend.EStyles;
import fzmm.zailer.me.client.gui.components.image.ScreenshotZoneComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.BaseSnackBarComponent;
import fzmm.zailer.me.client.gui.components.snack_bar.ISnackBarComponent;
import fzmm.zailer.me.utils.FzmmUtils;
import fzmm.zailer.me.utils.SnackBarManager;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import io.wispforest.owo.ui.hud.Hud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ScreenshotSource implements IInteractiveImageLoader {
    private static final Identifier HUD_CAPTURE_SCREENSHOT = Identifier.fromNamespaceAndPath(FzmmClient.MOD_ID, "screenshot_capture");
    private static ScreenshotSource instance;
    private BufferedImage image;
    private Consumer<BufferedImage> consumer;
    private BaseFzmmScreen previousScreen;

    public ScreenshotSource() {
        this.image = null;
    }

    public static ScreenshotSource getInstance() {
        return instance;
    }

    public static boolean hasInstance() {
        return instance != null;
    }

    @Override
    public void execute(Consumer<BufferedImage> consumer) {
        this.image = null;
        this.consumer = consumer;
        Minecraft client = Minecraft.getInstance();

        this.previousScreen = client.gui.screen() instanceof BaseFzmmScreen baseScreen ? baseScreen : null;
        SnackBarManager.getInstance().moveToHud(this.previousScreen);
        client.gui.setScreen(null);
        Hud.add(HUD_CAPTURE_SCREENSHOT, this::getHud);
        instance = this;
    }

    @Override
    public Optional<BufferedImage> getImage() {
        return Optional.ofNullable(this.image);
    }

    @Override
    public boolean hasTextField() {
        return false;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        this.consumer.accept(this.image);
    }

    private FlowLayout getHud() {
        FlowLayout hudLayout = (FlowLayout) EContainers.verticalFlow(Sizing.fill(100), Sizing.fill(100))
                .positioning(Positioning.absolute(0, 0));

        ScreenshotZoneComponent screenshotZoneComponent = new ScreenshotZoneComponent();
        screenshotZoneComponent.sizing(Sizing.fill(100), Sizing.fill(100))
                .positioning(Positioning.absolute(0, 0));

        FlowLayout labelLayout = (FlowLayout) EContainers.verticalFlow(Sizing.fill(100), Sizing.fixed(ScreenshotZoneComponent.PADDING))
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                .positioning(Positioning.absolute(0, 0));

        Component keyTranslation = FzmmClient.OPEN_MAIN_GUI_KEYBINDING.getTranslatedKeyMessage();
        LabelComponent labelComponent = EComponents.label(Component.translatable("fzmm.gui.option.image.screenshot.message", keyTranslation.getString()));

        labelLayout.child(labelComponent);
        hudLayout.child(screenshotZoneComponent);
        hudLayout.child(labelLayout);

        return hudLayout;
    }

    public void takeScreenshot() {
        RenderTarget framebuffer = Minecraft.getInstance().gameRenderer.mainRenderTarget();
        try {
            Screenshot.takeScreenshot(framebuffer, screenshot -> {
                screenshot.getPixels();
                this.processScreenshot(screenshot.getPixels());
            });
        } catch (Exception e) {
            this.complete(null, e);
        }
    }

    private void processScreenshot(int[] pixelArray) {
        CompletableFuture.supplyAsync(() -> {
            if (pixelArray == null) {
                return null;
            }

            Window window = Minecraft.getInstance().getWindow();
            int width = window.getScreenWidth();
            int height = window.getScreenHeight();
            BufferedImage screenshot = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            screenshot.getRaster().setDataElements(0, 0, width, height, pixelArray);
            int smallerSide = Math.min(width, height);
            int halfLongerSide = smallerSide / 2;

            BufferedImage scaled = screenshot.getSubimage(width / 2 - halfLongerSide, height / 2 - halfLongerSide, smallerSide, smallerSide);
            BufferedImage finalImage = this.removePadding(scaled);

            screenshot.flush();
            scaled.flush();

            return finalImage;
        }, Util.backgroundExecutor()).whenComplete(this::complete);
    }

    private void complete(BufferedImage image, Throwable throwable) {
        instance = null;
        ISnackBarComponent snackBar = null;

        if (throwable != null || image == null) {
            FzmmClient.LOGGER.error("[ScreenshotSource] Unexpected error while taking screenshot", throwable);
            snackBar = BaseSnackBarComponent.builder(SnackBarManager.IMAGE_ID)
                    .title(Component.translatable("fzmm.snack_bar.image.error.title"))
                    .details(Component.translatable("fzmm.snack_bar.image.error.details.unexpectedError"))
                    .backgroundColor(EStyles.ALERT_ERROR_COLOR)
                    .closeButton()
                    .build();
        }

        ISnackBarComponent finalSnackBar = snackBar;
        Minecraft.getInstance().execute(() -> {
            SnackBarManager manager = SnackBarManager.getInstance();
            Hud.remove(HUD_CAPTURE_SCREENSHOT);
            if (finalSnackBar != null) {
                manager.add(finalSnackBar);
            }

            FzmmUtils.setScreen(this.previousScreen);
            this.previousScreen = null;
            this.setImage(image);
        });
    }

    private BufferedImage removePadding(BufferedImage image) {
        // all minecraft rendering varies depending on the gui scale, so it is necessary to adjust the padding value
        int padding = ScreenshotZoneComponent.PADDING * Minecraft.getInstance().options.guiScale().get();

        BufferedImage paddedScreenshot = new BufferedImage(image.getWidth() - 2 * padding, image.getHeight() - 2 * padding, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = paddedScreenshot.createGraphics();
        g2d.drawImage(image, -padding, -padding, null);
        g2d.dispose();
        return paddedScreenshot;
    }
}
