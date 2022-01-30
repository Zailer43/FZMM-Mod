package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.gui.imagetext.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.TranslatableText;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class ImagetextScreen extends AbstractFzmmScreen {


    public ImagetextScreen() {
        super(new TranslatableText("imagetext.title"));
    }

    public void init() {
        super.init();
        assert this.client != null;
        final int[] buttonX = {this.width / 2 - 199, this.width / 2 - 65, this.width / 2 + 69};

        this.addDrawableChild(new ButtonWidget(buttonX[0], LINE1, 130, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.lore"),
                (buttonWidget) -> this.client.setScreen(new ImagetextLoreScreen())
        ));
        this.addDrawableChild(new ButtonWidget(buttonX[1], LINE1, 130, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.bookPage"),
                (buttonWidget) -> this.client.setScreen(new ImagetextBookPageScreen())
        ));
        this.addDrawableChild(new ButtonWidget(buttonX[2], LINE1, 130, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.bookTooltip"),
                (buttonWidget) -> this.client.setScreen(new ImagetextBookTooltipScreen())
        ));

        this.addDrawableChild(new ButtonWidget(buttonX[0], LINE2, 130, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.hologram"),
                (buttonWidget) -> this.client.setScreen(new ImagetextHologramScreen())
        ));
        this.addDrawableChild(new ButtonWidget(buttonX[1], LINE2, 130, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.sign"), null
                //(buttonWidget) -> this.client.setScreen(new ImagetextSignScreen())
        )).active = false;
        this.addDrawableChild(new ButtonWidget(buttonX[2], LINE2, 130, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.tellraw"),
                (buttonWidget) -> this.client.setScreen(new ImagetextTellrawScreen())
        ));
    }

    public void resize(MinecraftClient client, int width, int height) {
        this.init(client, width, height);
    }
}
