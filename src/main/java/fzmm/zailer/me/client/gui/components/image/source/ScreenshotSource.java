package fzmm.zailer.me.client.gui.components.image.source;

import fzmm.zailer.me.client.FzmmClient;
import fzmm.zailer.me.client.gui.components.image.ScreenshotZoneComponent;
import fzmm.zailer.me.client.toast.LoadingImageToast;
import fzmm.zailer.me.client.toast.status.ImageStatus;
import io.wispforest.owo.ui.component.Components;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.Containers;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.HorizontalAlignment;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.VerticalAlignment;
import io.wispforest.owo.ui.hud.Hud;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public class ScreenshotSource implements IInteractiveImageLoader {
    private static final Identifier HUD_CAPTURE_SCREENSHOT = new Identifier(FzmmClient.MOD_ID, "screenshot_capture");
    private static ScreenshotSource instance;
    private BufferedImage image;
    private Consumer<BufferedImage> consumer;
    private Screen previousScreen;

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

        this.previousScreen = client.currentScreen;
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

    @Override
    public void setImage(BufferedImage image) {
        this.image = image;
        this.consumer.accept(this.image);
    }

    private FlowLayout getHud() {
        FlowLayout hudLayout = (FlowLayout) Containers.verticalFlow(Sizing.fill(100), Sizing.fill(100))
                .positioning(Positioning.absolute(0, 0));

        ScreenshotZoneComponent screenshotZoneComponent = new ScreenshotZoneComponent();
        screenshotZoneComponent.sizing(Sizing.fill(100), Sizing.fill(100))
                .positioning(Positioning.absolute(0, 0));

        FlowLayout labelLayout = (FlowLayout) Containers.verticalFlow(Sizing.fill(100), Sizing.fixed(ScreenshotZoneComponent.PADDING))
                .alignment(HorizontalAlignment.CENTER, VerticalAlignment.CENTER)
                .positioning(Positioning.absolute(0, 0));

        Text keyTranslation = FzmmClient.OPEN_MAIN_GUI_KEYBINDING.getBoundKeyLocalizedText();
        LabelComponent labelComponent = Components.label(Text.translatable("fzmm.gui.option.imageMode.screenshot.message", keyTranslation));

        labelLayout.child(labelComponent);
        hudLayout.child(screenshotZoneComponent);
        hudLayout.child(labelLayout);

        return hudLayout;
    }

    public void takeScreenshot() {
        MinecraftClient client = MinecraftClient.getInstance();
        try {
            Framebuffer framebuffer = client.getFramebuffer();
            byte[] byteArray;
            try (var screenshot = ScreenshotRecorder.takeScreenshot(framebuffer)) {
                byteArray = screenshot.getBytes();
            }
            BufferedImage screenshot = ImageIO.read(new ByteArrayInputStream(byteArray));
            int width = screenshot.getWidth();
            int height = screenshot.getHeight();
            int smallerSide = Math.min(width, height);
            int halfLongerSide = smallerSide / 2;

            BufferedImage scaled = screenshot.getSubimage(width / 2 - halfLongerSide, height / 2 - halfLongerSide, smallerSide, smallerSide);
            BufferedImage finalImage = this.removePadding(scaled);

            this.setImage(finalImage);
        } catch (IOException e) {
            e.printStackTrace();
            LoadingImageToast toast = new LoadingImageToast();
            MinecraftClient.getInstance().getToastManager().add(toast);

            toast.setResponse(ImageStatus.UNEXPECTED_ERROR);
            this.setImage(null);
        }

        instance = null;
        Hud.remove(HUD_CAPTURE_SCREENSHOT);
        client.setScreen(this.previousScreen);
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
