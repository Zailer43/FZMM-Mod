package fzmm.zailer.me.client.gui.imagetext;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class ImagetextLoreScreen extends AbstractImagetextScreen {
    private ButtonWidget loreButton;
    private PutLoreWith howToPutLore;

    public ImagetextLoreScreen() {
        super(new TranslatableText("imagetext.title.lore"));
        this.howToPutLore = PutLoreWith.ADD;
    }

    @Override
    protected void init() {
        super.init();

        this.loreButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 154, LINE4, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.lore.add"),
                (buttonWidget) -> {
                    this.cycleLore();
                    this.updateLoreButtonMessage();
                }
        ));

        this.updateLoreButtonMessage();
    }

    @Override
    protected void execute() {
        assert this.client != null;
        assert this.client.player != null;
        ItemStack stack = this.client.player.getMainHandStack();
        ImagetextLogic imagetext = new ImagetextLogic(this.image,
                this.charTextField.getText(),
                (byte) this.widthSlider.getValue(),
                (byte) this.heightSlider.getValue(),
                this.smoothRescalingCheckbox.isChecked()
        );

        if (this.howToPutLore == PutLoreWith.ADD) {
            imagetext.addToLore(stack);
        } else {
            imagetext.setToLore(stack);
        }
        this.executed = true;
    }

    public enum PutLoreWith {
        ADD(),
        SET();

        PutLoreWith() {
        }
    }

    public void cycleLore() {
        this.howToPutLore = switch (this.howToPutLore) {
            case ADD -> PutLoreWith.SET;
            case SET -> PutLoreWith.ADD;
        };
    }

    public void updateLoreButtonMessage() {
        String translateKey = switch (this.howToPutLore) {
            case ADD -> "add";
            case SET -> "set";
        };
        this.loreButton.setMessage(new TranslatableText("imagetext.lore." + translateKey));
    }
}
