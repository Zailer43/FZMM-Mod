package fzmm.zailer.me.client.gui.imagetext;

import fzmm.zailer.me.client.gui.AbstractFzmmScreen;
import fzmm.zailer.me.client.gui.widget.IntSliderWidget;
import fzmm.zailer.me.utils.FzmmUtils;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.Nullable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public abstract class AbstractImagetextScreen extends AbstractFzmmScreen {
    private static final byte MAX_IMG_SIZE = 127;
    private static final byte DEFAULT_IMG_SIZE = 32;
    private static final byte MIN_IMG_SIZE = 2;
    protected ButtonWidget executeButton, howGetImageButton, loadImageButton;
    protected TextFieldWidget imageTextField, charTextField;
    protected IntSliderWidget widthSlider, heightSlider;
    protected CheckboxWidget smoothRescalingCheckbox;
    protected BufferedImage image;
    protected TranslatableText executeMessage;
    protected boolean executed;

    private boolean imageError;
    private TranslatableText imageErrorMessage, getImageMessage;
    private GetImageFrom getImageFrom;

    public AbstractImagetextScreen(Text title) {
        super(title);
        this.imageError = false;
        this.getImageFrom = GetImageFrom.URL;
        this.executed = false;
        this.executeMessage = new TranslatableText("imagetext.execute.copied");
    }

    protected void init() {
        super.init();

        this.executeButton = this.addDrawableChild(new ButtonWidget(20, this.height - 40, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("gui.execute"),
                (buttonWidget) -> this.execute()
        ));

        this.imageTextField = this.addDrawableChild(new TextFieldWidget(this.textRenderer, this.width / 2 - 154, LINE1, 204, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("imagetext.imageUrl")));
        this.imageTextField.setMaxLength(1024);
        this.imageTextField.setChangedListener(this::imagetextListener);
        this.setInitialFocus(this.imageTextField);

        this.howGetImageButton = this.addDrawableChild(new ButtonWidget(this.width / 2 + 54, LINE1, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.url"), (buttonWidget) -> {
            this.cycleHowGetImage();
            this.updateGetImageMessage();
        }));

        this.loadImageButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 154, LINE2, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.loadImage"),
                (buttonWidget) -> this.loadImage()
        ));

        this.smoothRescalingCheckbox = this.addDrawableChild(new CheckboxWidget(this.width / 2 - 50, LINE2, 20, 20, new TranslatableText("imagetext.smoothRescaling"), false));

        this.charTextField = this.addDrawableChild(new TextFieldWidget(this.textRenderer, this.width / 2 + 54, LINE2, NORMAL_BUTTON_WIDTH, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("imagetext.charField")));
        this.charTextField.setMaxLength(MAX_IMG_SIZE);
        this.charTextField.setText(ImagetextLine.DEFAULT_TEXT);

        this.widthSlider = this.addDrawableChild(new IntSliderWidget(this.width / 2 - 154, LINE3, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, "imagetext.width", true,
                DEFAULT_IMG_SIZE, MIN_IMG_SIZE, MAX_IMG_SIZE));
        this.widthSlider.setChangedListener(value -> this.setProportions(true, value));

        this.heightSlider = this.addDrawableChild(new IntSliderWidget(this.width / 2 - 50, LINE3, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, "imagetext.height", true,
                DEFAULT_IMG_SIZE, MIN_IMG_SIZE, MAX_IMG_SIZE));
        this.heightSlider.setChangedListener(value -> this.setProportions(false, value));

        this.addSelectableChild(this.imageTextField);
        this.addSelectableChild(this.smoothRescalingCheckbox);
        this.addSelectableChild(this.widthSlider);
        this.addSelectableChild(this.heightSlider);

        this.updateGetImageMessage();
        this.executeButton.active = false;
    }

    public void resize(MinecraftClient client, int width, int height) {
        String imageTextField2 = this.imageTextField.getText();
        double widthRatio = this.widthSlider.getRatio(),
                heightRatio = this.heightSlider.getRatio();
        String string = this.charTextField.getText();
        boolean executeButton = this.executeButton.active,
                smoothRescaling = this.smoothRescalingCheckbox.isChecked();

        this.init(client, width, height);

        this.imageTextField.setText(imageTextField2);
        if (smoothRescaling) this.smoothRescalingCheckbox.onPress();
        this.widthSlider.setRatio(widthRatio);
        this.heightSlider.setRatio(heightRatio);
        this.charTextField.setText(string);
        this.executeButton.active = executeButton;
    }

    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        drawCenteredText(matrices, this.textRenderer, this.getImageMessage, this.width / 2 - 52, LINE1 - 10, TEXT_COLOR);
        drawCenteredText(matrices, this.textRenderer, new TranslatableText("imagetext.charField"), this.width / 2 + 104, LINE2 - 10, TEXT_COLOR);

        if (this.imageError) {
            drawCenteredText(matrices, this.textRenderer, this.imageErrorMessage, this.width / 2, LINE5 - 8, TEXT_ERROR_COLOR);
        } else  if (this.executed) {
            drawCenteredText(matrices, this.textRenderer, this.executeMessage, this.width / 2, LINE5 - 8, TEXT_COLOR);
        } else if (this.imageIsSafe()) {
            drawCenteredText(matrices, this.textRenderer, new TranslatableText("imagetext.loadedImage"), this.width / 2, LINE5 - 8, TEXT_COLOR);
        }
    }

    protected abstract void execute();

    private void imagetextListener(String text) {
        //TODO: add suggestions for when searching for path
        if (this.getImageFrom == GetImageFrom.PATH) {
            try {
                this.setImageError(this.isFile(text) ? null : "notAFile");
            } catch (InvalidPathException e) {
                this.setImageError("invalidPath");
            }
        } else {
            this.setImageError(null);
        }
    }

    private enum GetImageFrom {
        URL,
        PATH;

        GetImageFrom() {
        }
    }

    public void cycleHowGetImage() {
        this.getImageFrom = switch (this.getImageFrom) {
            case URL -> GetImageFrom.PATH;
            case PATH -> GetImageFrom.URL;
        };
    }

    public void updateGetImageMessage() {
        String translateKey = switch (this.getImageFrom) {
            case URL -> "url";
            case PATH -> "path";
        };
        this.howGetImageButton.setMessage(new TranslatableText("imagetext." + translateKey));
        this.getImageMessage = new TranslatableText("imagetext.getImageFrom." + translateKey);
        this.imagetextListener(this.imageTextField.getText());
    }

    private void loadImage() {
        String fileLocate = this.imageTextField.getText();

        switch (this.getImageFrom) {
            case URL -> {
                try {
                    this.image = FzmmUtils.getImageFromUrl(fileLocate);
                    this.setImageError(this.image == null ? "imageNotFound" : null);
                } catch (IOException e) {
                    this.setImageError("malformedUrl");
                }
            }
            case PATH -> {
                try {
                    if (this.isFile(fileLocate)) {
                        this.image = FzmmUtils.getImageFromPath(fileLocate);
                        this.setImageError(null);
                    }
                } catch (IOException e) {
                    this.setImageError("unexpectedGettingImage");
                }
            }
        }

        if (this.imageIsSafe()) {
            this.updateMax(this.image.getWidth(), this.image.getHeight());
            this.executed = false;
        }
    }

    private void updateMax(int width, int height) {
        if (width == height)
            return;

        width = Math.max(MAX_IMG_SIZE, width);
        height = Math.max(MAX_IMG_SIZE, height);

        boolean widthIsGreater = width > height;
        int newValue = this.getProportions(width, height, MAX_IMG_SIZE, widthIsGreater);

        (widthIsGreater ? this.heightSlider : this.widthSlider).setMax(Math.min(newValue, MAX_IMG_SIZE));
        (widthIsGreater ? this.widthSlider : this.heightSlider).setMax(MAX_IMG_SIZE);
        this.charTextField.setMaxLength(this.widthSlider.getMax());
    }

    private void setProportions(boolean setWidth, int newValue) {
        if (this.imageIsSafe()) {
            newValue = this.getProportions(this.image.getWidth(), this.image.getHeight(), newValue, setWidth);
            (setWidth ? heightSlider : widthSlider).setValue(newValue);
        }
    }

    private int getProportions(int x, int y, int newValue, boolean getWidth) {
        return Math.round((float) newValue / (getWidth ? x : y) * (getWidth ? y : x));
    }

    public void setImageError(@Nullable String translateKey) {
        if (translateKey == null) {
            this.imageError = false;
            if (this.image != null)
                this.executeButton.active = true;
        } else {
            this.imageErrorMessage = new TranslatableText("imagetext.error." + translateKey);
            this.imageError = true;
            this.executeButton.active = false;
        }
    }

    private boolean imageIsSafe() {
        return !this.imageError && this.image != null && this.executeButton.active;
    }

    private boolean isFile(String filePath) {
        Path path = Paths.get(filePath);
        return Files.exists(path) && !Files.isDirectory(path) && Files.isReadable(path);
    }
}
