package fzmm.zailer.me.client.gui.imagetext;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

import static fzmm.zailer.me.client.gui.ScreenConstants.NORMAL_BUTTON_HEIGHT;
import static fzmm.zailer.me.client.gui.ScreenConstants.NORMAL_BUTTON_WIDTH;
import static fzmm.zailer.me.client.gui.ScreenConstants.LINE3;

public class ImagetextBookPageScreen extends AbstractImagetextScreen {
    private static final byte MAX_TEXT_WIDTH = 114;
    private static final byte MAX_LINES_PER_PAGE = 15 + 1;
    private ButtonWidget modeButton;
    private Mode mode;

    public ImagetextBookPageScreen() {
        super(new TranslatableText("imagetext.title.bookPage"));
    }

    @Override
    protected void init() {
        super.init();

        this.modeButton = this.addDrawableChild(new ButtonWidget(this.width / 2 + 54, LINE3, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.book.createBook"),
                (buttonWidget) -> {
                    this.cycleMode();
                    this.updateModeMessage();
                }
        ));

        this.mode = Mode.CREATE_BOOK;
        this.updateModeMessage();

        this.updateMax();
        this.widthSlider.setValue(this.widthSlider.getMax());
        this.heightSlider.setValue(this.heightSlider.getMax());
        this.widthSlider.visible = false;
        this.heightSlider.visible = false;

        this.charTextField.setMaxLength(this.widthSlider.getMax());
    }

    @Override
    public void resize(MinecraftClient client, int width, int height) {
        Mode mode2 = this.mode;
        super.resize(client, width, height);
        this.mode = mode2;
        this.updateModeMessage();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

    }

    @Override
    protected void updateMax(int width, int height) {
        this.widthSlider.setMax(this.getMaxPixelsPerLine());
        this.heightSlider.setMax(MAX_LINES_PER_PAGE);
    }

    protected void updateMax() {
        this.updateMax(0, 0);
    }

    private int getMaxPixelsPerLine() {
        assert this.client != null;
        String text = this.charTextField.getText();
        if (text.isEmpty())
            text = ImagetextLine.DEFAULT_TEXT;
        int messageWidth = this.client.textRenderer.getWidth(text);
        System.out.println(messageWidth);
        return MAX_TEXT_WIDTH / messageWidth + 1;
    }

    private enum Mode {
        ADD_PAGE,
        CREATE_BOOK
    }

    private void cycleMode() {
        this.mode = switch (this.mode) {
            case ADD_PAGE -> Mode.CREATE_BOOK;
            case CREATE_BOOK -> Mode.ADD_PAGE;
        };
    }

    private void updateModeMessage() {
        this.modeButton.setMessage(new TranslatableText(switch (this.mode) {
            case ADD_PAGE -> "imagetext.book.addPage";
            case CREATE_BOOK -> "imagetext.book.createBook";
        }));
    }

    @Override
    protected void execute() {
        if (mode == Mode.ADD_PAGE) {
            new ImagetextLogic(this.image,
                    this.charTextField.getText(),
                    (byte) this.widthSlider.getValue(),
                    (byte) this.heightSlider.getValue(),
                    this.smoothRescalingCheckbox.isChecked()
            ).addBookPage();
        } else {
            new ImagetextLogic(this.image,
                    this.charTextField.getText(),
                    (byte) this.widthSlider.getValue(),
                    (byte) this.heightSlider.getValue(),
                    this.smoothRescalingCheckbox.isChecked()
            ).giveBookPage();
        }
        this.executeMessage = new TranslatableText("imagetext.execute.successful");
        this.executed = true;
    }
}
