package fzmm.zailer.me.client.gui.components.image.source;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.BaseFzmmScreen;
import fzmm.zailer.me.client.gui.components.image.ScreenshotZoneComponent;
import fzmm.zailer.me.client.gui.components.style.FzmmStyles;
import fzmm.zailer.me.client.gui.components.style.StyledComponents;
import fzmm.zailer.me.client.gui.components.style.StyledContainers;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.client.util.Window;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class ScreenshotSource implements IInteractiveImageLoader {
    private static final Identifier HUD_CAPTURE_SCREENSHOT = Identifier.of(FzmmClient.MOD_ID, "screenshot_capture");
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
        MinecraftClient client = MinecraftClient.getInstance();

        this.previousScreen = client.currentScreen instanceof BaseFzmmScreen baseScreen ? baseScreen : null;
        SnackBarManager.getInstance().moveToHud(this.previousScreen);
        client.setScreen(null);
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
        FlowLayout hudLayout = (FlowLayout) StyledContainers.verticalFlow(Sizing.fill(100), Sizing.fill(100))
                .positioning(Positioning.absolute(0, 0));

        ScreenshotZoneComponent screenshotZoneComponent = new ScreenshotZoneComponent();
        screenshotZoneComponent.sizing(Sizing.fill(100), Sizing.fill(100))
                .positioning(Positioning.absolute(0, 0));

        FlowLayout labelLayout = (FlowLayout) StyledContainers.verticalFlow(Sizing.fill(100), Sizing.fixed(ScreenshotZoneComponent.PADDING))
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                .positioning(Positioning.absolute(0, 0));

        Text keyTranslation = FzmmClient.OPEN_MAIN_GUI_KEYBINDING.getBoundKeyLocalizedText();
        LabelComponent labelComponent = StyledComponents.label(Text.translatable("fzmm.gui.option.image.screenshot.message", keyTranslation.getString()));

        labelLayout.child(labelComponent);
        hudLayout.child(screenshotZoneComponent);
        hudLayout.child(labelLayout);

        return hudLayout;
    }

    public void takeScreenshot() {
        int[] pixelArray = null;
        Exception exception = null;

        MinecraftClient client = MinecraftClient.getInstance();
        Framebuffer framebuffer = client.getFramebuffer();
        try (var screenshot = ScreenshotRecorder.takeScreenshot(framebuffer)) {
            pixelArray = screenshot.copyPixelsArgb();
        } catch (Exception e) {
            exception = e;
        }

        int[] finalByteArray = pixelArray;
        Exception finalException = exception;
        CompletableFuture.supplyAsync(() -> {
            if (finalByteArray == null) {
                return null;
            }

            if (finalException != null) {
                throw new RuntimeException(finalException);
            }

            Window window = MinecraftClient.getInstance().getWindow();
            int width = window.getWidth();
            int height = window.getHeight();
            BufferedImage screenshot = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            screenshot.getRaster().setDataElements(0, 0, width, height, finalByteArray);
            int smallerSide = Math.min(width, height);
            int halfLongerSide = smallerSide / 2;

            BufferedImage scaled = screenshot.getSubimage(width / 2 - halfLongerSide, height / 2 - halfLongerSide, smallerSide, smallerSide);
            BufferedImage finalImage = this.removePadding(scaled);

            screenshot.flush();
            scaled.flush();

            return finalImage;
        }, Util.getMainWorkerExecutor()).whenComplete((image, throwable) -> {
            instance = null;
            ISnackBarComponent snackBar = null;

            if (throwable != null || image == null) {
                FzmmClient.LOGGER.error("[ScreenshotSource] Unexpected error while taking screenshot", throwable);
                snackBar = BaseSnackBarComponent.builder(SnackBarManager.IMAGE_ID)
                        .title(Text.translatable("fzmm.snack_bar.image.error.title"))
                        .details(Text.translatable("fzmm.snack_bar.image.error.details.unexpectedError"))
                        .backgroundColor(FzmmStyles.ALERT_ERROR_COLOR)
                        .closeButton()
                        .build();

            }

            ISnackBarComponent finalSnackBar = snackBar;
            client.execute(() -> {
                SnackBarManager manager = SnackBarManager.getInstance();
                Hud.remove(HUD_CAPTURE_SCREENSHOT);
                if (finalSnackBar != null) {
                    manager.add(finalSnackBar);
                }

                FzmmUtils.setScreen(this.previousScreen);
                this.previousScreen = null;
                this.setImage(image);
            });
        });
    }

    private BufferedImage removePadding(BufferedImage image) {
        // all minecraft rendering varies depending on the gui scale, so it is necessary to adjust the padding value
        int padding = ScreenshotZoneComponent.PADDING * MinecraftClient.getInstance().options.getGuiScale().getValue();

        BufferedImage paddedScreenshot = new BufferedImage(image.getWidth() - 2 * padding, image.getHeight() - 2 * padding, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = paddedScreenshot.createGraphics();
        g2d.drawImage(image, -padding, -padding, null);
        g2d.dispose();
        return paddedScreenshot;
    }
}
