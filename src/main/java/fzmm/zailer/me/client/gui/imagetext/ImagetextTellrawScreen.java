package fzmm.zailer.me.client.gui.imagetext;

import net.minecraft.text.TranslatableText;

public class ImagetextTellrawScreen extends AbstractImagetextScreen {

    public ImagetextTellrawScreen() {
        super(new TranslatableText("imagetext.title.tellraw"));
    }

    @Override
    public void init() {
        super.init();
        this.executeButton.setMessage(new TranslatableText("chat.copy"));
    }

    @Override
    protected void execute() {
        assert this.client != null;
        String message = new ImagetextLogic(this.image,
                this.charTextField.getText(),
                (byte) this.widthSlider.getValue(),
                (byte) this.heightSlider.getValue(),
                this.smoothRescalingCheckbox.isChecked()
        ).getImagetextString();
        this.client.keyboard.setClipboard(message);
        if (message.length() > 32500) {
            this.executeMessage = new TranslatableText("imagetext.execute.copied.warning");
        } else {
            this.executeMessage = new TranslatableText("imagetext.execute.copied");
        }
        this.executed = true;
    }
}
