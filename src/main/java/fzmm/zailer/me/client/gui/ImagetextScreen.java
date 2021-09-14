package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.gui.imagetext.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.TranslatableText;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class ImagetextScreen extends AbstractFzmmScreen {

    protected ButtonWidget loreButton, bookButton, hologramButton, signButton, tellrawButton;

    protected ImagetextScreen() {
        super(new TranslatableText("imagetext.title"));
    }

    protected void init() {
        super.init();
        assert this.client != null;

        this.loreButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 199, LINE1, 130, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.lore"),
                (buttonWidget) -> this.client.setScreen(new ImagetextLoreScreen())
        ));
        this.bookButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 65, LINE1, 130, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.book"),
                (buttonWidget) -> this.client.setScreen(new ImagetextBookScreen())
        ));
        this.hologramButton = this.addDrawableChild(new ButtonWidget(this.width / 2 + 69, LINE1, 130, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.hologram"),
                (buttonWidget) -> this.client.setScreen(new ImagetextHologramScreen())
        ));

        this.signButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 199, LINE2, 130, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.sign"), null
                //(buttonWidget) -> this.client.setScreen(new ImagetextSignScreen())
        ));
        this.signButton.active = false;
        this.tellrawButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 65, LINE2, 130, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.tellraw"),
                (buttonWidget) -> this.client.setScreen(new ImagetextTellrawScreen())
        ));
    }

    public void resize(MinecraftClient client, int width, int height) {
        this.init(client, width, height);
    }
}
