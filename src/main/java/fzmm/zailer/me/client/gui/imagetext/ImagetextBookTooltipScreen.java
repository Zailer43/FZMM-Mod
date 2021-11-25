package fzmm.zailer.me.client.gui.imagetext;

import fzmm.zailer.me.config.FzmmConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class ImagetextBookTooltipScreen extends AbstractImagetextScreen {
    private TextFieldWidget authorTextField, messageTextField;

    public ImagetextBookTooltipScreen() {
        super(new TranslatableText("imagetext.title.bookTooltip"));
    }

    @Override
    protected void init() {
        super.init();
        this.authorTextField = this.addDrawableChild(new TextFieldWidget(this.textRenderer, this.width / 2 - 154, LINE4, NORMAL_BUTTON_WIDTH, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("book.author")));
        this.authorTextField.setMaxLength(127);

        this.messageTextField = this.addDrawableChild(new TextFieldWidget(this.textRenderer, this.width / 2 - 50, LINE4, 208, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("book.message")));
        this.messageTextField.setMaxLength(255);

        assert this.client != null;
        assert this.client.player != null;
        this.authorTextField.setText(this.client.player.getName().asString());
        this.messageTextField.setText(FzmmConfig.get().general.defaultImagetextBookMessage);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        String author = this.authorTextField.getText(),
                message = this.messageTextField.getText();

        super.resize(client, width, height);

        this.authorTextField.setText(author);
        this.messageTextField.setText(message);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        drawCenteredText(matrices, this.textRenderer, new TranslatableText("book.author"), this.width / 2 - 102, LINE4 - 10, TEXT_COLOR);
        drawCenteredText(matrices, this.textRenderer, new TranslatableText("book.message"), this.width / 2 + 54, LINE4 - 10, TEXT_COLOR);

    }

    @Override
    protected void execute() {

        try {
            new ImagetextLogic(this.image,
                    this.charTextField.getText(),
                    (byte) this.widthSlider.getValue(),
                    (byte) this.heightSlider.getValue(),
                    this.smoothRescalingCheckbox.isChecked()
            ).giveBookTooltip(this.authorTextField.getText(), this.messageTextField.getText());
            this.executed = true;
        } catch (Exception ignored) {
            this.setImageError("bookNbtTooLong");
        }
    }
}
