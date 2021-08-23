package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.gui.converters.Base64Screen;
import fzmm.zailer.me.client.gui.converters.CoordinatesScreen;
import fzmm.zailer.me.client.gui.converters.UuidScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.TranslatableText;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class ConvertersScreen extends FzmmBaseScreen {

	protected ButtonWidget coordinatesButton,
		uuidButton,
		base64Button;

	public ConvertersScreen() {
		super(new TranslatableText("converters.title"));
	}

	protected void init() {
		super.init();
		assert this.client != null;

		this.coordinatesButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 199, LINE1, 130, NORMAL_BUTTON_HEIGHT, new TranslatableText("converters.coordinates"),
			(buttonWidget) -> this.client.setScreen(new CoordinatesScreen())
		));
		this.uuidButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 65, LINE1, 130, NORMAL_BUTTON_HEIGHT, new TranslatableText("converters.uuid"),
			(buttonWidget) -> this.client.setScreen(new UuidScreen())
		));
		this.base64Button = this.addDrawableChild(new ButtonWidget(this.width / 2 + 69, LINE1, 130, NORMAL_BUTTON_HEIGHT, new TranslatableText("converters.base64"),
			(buttonWidget) -> this.client.setScreen(new Base64Screen())
		));
	}

	public void resize(MinecraftClient client, int width, int height) {
		this.init(client, width, height);
	}
}
