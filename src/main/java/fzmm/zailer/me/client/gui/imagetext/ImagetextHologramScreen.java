package fzmm.zailer.me.client.gui.imagetext;

import fzmm.zailer.me.client.gui.widget.NumberFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class ImagetextHologramScreen extends AbstractImagetextScreen {
    private NumberFieldWidget xNumberField, yNumberField, zNumberField;

    public ImagetextHologramScreen() {
        super(new TranslatableText("imagetext.title.hologram"));
    }

    @Override
    protected void init() {
        super.init();
        assert this.client != null;
        assert this.client.player != null;
        this.xNumberField = this.addDrawableChild(new NumberFieldWidget(this.textRenderer, this.width / 2 - 154, LINE4, 100, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("axis.x"), -30000000, 30000000));
        this.xNumberField.setMaxLength(9);

        this.yNumberField = this.addDrawableChild(new NumberFieldWidget(this.textRenderer, this.width / 2 - 50, LINE4, 100, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("axis.y"), Short.MIN_VALUE, Short.MAX_VALUE));
        this.yNumberField.setMaxLength(6);

        this.zNumberField = this.addDrawableChild(new NumberFieldWidget(this.textRenderer, this.width / 2 + 54, LINE4, 100, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("axis.z"), -30000000, 30000000));
        this.zNumberField.setMaxLength(9);


        this.xNumberField.setText(String.valueOf(this.client.player.getBlockX()));
        this.yNumberField.setText(String.valueOf(this.client.player.getBlockY()));
        this.zNumberField.setText(String.valueOf(this.client.player.getBlockZ()));

        this.addSelectableChild(this.xNumberField);
        this.addSelectableChild(this.yNumberField);
        this.addSelectableChild(this.zNumberField);
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        int x = this.xNumberField.getNumber(),
                z = this.zNumberField.getNumber();
        short y = (short) this.yNumberField.getNumber();

        super.resize(client, width, height);

        this.xNumberField.setText(String.valueOf(x));
        this.yNumberField.setText(String.valueOf(y));
        this.zNumberField.setText(String.valueOf(z));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        drawCenteredText(matrices, this.textRenderer, new LiteralText("X"), this.width / 2 - 104, LINE4 - 10, TEXT_COLOR);
        drawCenteredText(matrices, this.textRenderer, new LiteralText("Y"), this.width / 2, LINE4 - 10, TEXT_COLOR);
        drawCenteredText(matrices, this.textRenderer, new LiteralText("Z"), this.width / 2 + 104, LINE4 - 10, TEXT_COLOR);
    }

    @Override
    protected void execute() {
        new ImagetextLogic(this.image,
                this.charTextField.getText(),
                (byte) this.widthSlider.getValue(),
                (byte) this.heightSlider.getValue(),
                this.smoothRescalingCheckbox.isChecked()
        ).giveAsHologram(this.xNumberField.getNumber(), this.yNumberField.getNumber(), this.zNumberField.getNumber());
        this.executed = true;
    }
}
