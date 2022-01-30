package fzmm.zailer.me.client.gui.converters;

import fzmm.zailer.me.client.gui.AbstractFzmmScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class Base64Screen extends AbstractFzmmScreen {
	private TextFieldWidget stringTextField,
		base64TextField;
	private boolean base64Error;

	public Base64Screen() {
		super(new TranslatableText("converters.base64"));
	}

	public void init() {
		super.init();

		this.stringTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 200, LINE1, 400, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("base64.text"));
		this.stringTextField.setMaxLength(2048);
		this.stringTextField.setChangedListener(this::textListener);
//		this.setInitialFocus(this.stringTextField);

		this.base64TextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 200, LINE2, 400, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("base64.encode"));
		this.base64TextField.setMaxLength(4096);
		this.base64TextField.setChangedListener(this::base64Listener);

		this.addSelectableChild(this.stringTextField);
		this.addSelectableChild(this.base64TextField);

	}

	public void resize(MinecraftClient client, int width, int height) {
		String text = this.stringTextField.getText(),
			base64 = this.base64TextField.getText();

		this.init(client, width, height);

		this.stringTextField.setText(text);
		this.base64TextField.setText(base64);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		drawCenteredText(matrices, this.textRenderer, new TranslatableText("base64.text"), this.width / 2, LINE1 - 10, TEXT_COLOR);
		this.stringTextField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new TranslatableText("base64.encode"), this.width / 2, LINE2 - 10, TEXT_COLOR);
		this.base64TextField.render(matrices, mouseX, mouseY, delta);
		if (base64Error)
			drawCenteredText(matrices, this.textRenderer, new TranslatableText("base64.error.invalid"), this.width / 2, LINE3, TEXT_ERROR_COLOR);
	}

	public void textListener(String text) {
		try {
			String base64 = Base64.getEncoder().encodeToString(text.getBytes(StandardCharsets.UTF_8.toString()));
			if (!base64.equals(this.base64TextField.getText())) {
				this.base64TextField.setText(base64);
			}
		} catch(UnsupportedEncodingException ex) {
			base64Error = true;
		}
	}

	public void base64Listener(String base64) {
		try {
			byte[] decodedValue = Base64.getDecoder().decode(base64);
			String text = new String(decodedValue, StandardCharsets.UTF_8.toString());
			if (!text.equals(this.stringTextField.getText())) {
				this.stringTextField.setText(text.replaceAll("\n", ""));
			}
		} catch(UnsupportedEncodingException | IllegalArgumentException ex) {
			base64Error = true;
		}
	}
}
