package fzmm.zailer.me.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.text.*;

import java.util.Date;
import java.util.Random;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class GradientScreen extends FzmmBaseScreen {
	protected GradientScreen() {
		super(new TranslatableText("gradient.title"));
	}

	protected ButtonWidget copyButton,
		randomColorButton;
	private TextFieldWidget messageTextField,
		initialColorTextField,
		finalColorTextField;
	private CheckboxWidget obfuscatedCheckbox,
		boldCheckbox,
		strikethroughCheckbox,
		underlineCheckbox,
		italicCheckbox;
	private MutableText preview;

	protected void init() {
		super.init();
		final short CHECKBOX_ROW = (short) (this.width / 2 + 80);
		assert this.client != null;
		assert this.client.player != null;

		this.copyButton = this.addButton(new ButtonWidget(20, this.height - 40, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("chat.copy"),
			(buttonWidget) -> this.copyExecute()
		));

		this.messageTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, LINE1, 220, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("gradient.message"));
		this.messageTextField.setMaxLength(256);
		this.messageTextField.setChangedListener((text) -> updatePreview());
		this.setInitialFocus(this.messageTextField);

		this.initialColorTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, LINE2, 220, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("gradient.initialColor"));
		this.initialColorTextField.setMaxLength(6);
		this.initialColorTextField.setChangedListener(this::initialColorListener);

		this.finalColorTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, LINE3, 220, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("gradient.finalColor"));
		this.finalColorTextField.setMaxLength(6);
		this.finalColorTextField.setChangedListener(this::finalColorListener);

		this.randomColorButton = this.addButton(new ButtonWidget(this.width / 2 - 150, LINE4, 220, NORMAL_BUTTON_HEIGHT, new TranslatableText("gradient.randomColors"),
			(buttonWidget) -> this.randomColors()
		));

		obfuscatedCheckbox = this.addButton(new CheckboxWidget(CHECKBOX_ROW, 50, 20, 20, new LiteralText("Obfuscated"), false));
		boldCheckbox = this.addButton(new CheckboxWidget(CHECKBOX_ROW, 80, 20, 20, new LiteralText("Bold"), false));
		strikethroughCheckbox = this.addButton(new CheckboxWidget(CHECKBOX_ROW, 110, 20, 20, new LiteralText("Strikethrough"), false));
		underlineCheckbox = this.addButton(new CheckboxWidget(CHECKBOX_ROW, 140, 20, 20, new LiteralText("Underline"), false));
		italicCheckbox = this.addButton(new CheckboxWidget(CHECKBOX_ROW, 170, 20, 20, new LiteralText("Italic"), false));

		this.children.add(this.messageTextField);
		this.children.add(this.initialColorTextField);
		this.children.add(this.finalColorTextField);

		this.messageTextField.setText("Hello world");
		this.initialColorTextField.setText("ff0000");
		this.finalColorTextField.setText("0000ff");
		updatePreview();
	}

	public void resize(MinecraftClient client, int width, int height) {
		String messageTextField2 = messageTextField.getText(),
			initialColorTextField2 = initialColorTextField.getText(),
			finalColorTextField2 = finalColorTextField.getText();
		boolean obfuscated = obfuscatedCheckbox.isChecked(),
			bold = boldCheckbox.isChecked(),
			strikethrough = strikethroughCheckbox.isChecked(),
			underline = underlineCheckbox.isChecked(),
			italic = italicCheckbox.isChecked();
		MutableText preview2 = this.preview;

		this.init(client, width, height);

		this.messageTextField.setText(messageTextField2);
		this.initialColorTextField.setText(initialColorTextField2);
		this.finalColorTextField.setText(finalColorTextField2);

		if (this.obfuscatedCheckbox.isChecked() != obfuscated)
			this.obfuscatedCheckbox.onPress();
		if (this.boldCheckbox.isChecked() != bold)
			this.boldCheckbox.onPress();
		if (this.strikethroughCheckbox.isChecked() != strikethrough)
			this.strikethroughCheckbox.onPress();
		if (this.underlineCheckbox.isChecked() != underline)
			this.underlineCheckbox.onPress();
		if (this.italicCheckbox.isChecked() != italic)
			this.italicCheckbox.onPress();
		this.preview = preview2;
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		drawCenteredText(matrices, this.textRenderer, new TranslatableText("gradient.message"), this.width / 2 - 40, LINE1 - 10, TEXT_COLOR);
		this.messageTextField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new TranslatableText("gradient.initialColor"), this.width / 2 - 40, LINE2 - 10, TEXT_COLOR);
		this.initialColorTextField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new TranslatableText("gradient.finalColor"), this.width / 2 - 40, LINE3 - 10, TEXT_COLOR);
		this.finalColorTextField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, this.preview, this.width / 2 - 40, LINE5 - 10, TEXT_COLOR);
	}

	public void copyExecute() {
		assert this.client != null;
		ListTag listTag = new ListTag();
		MutableText[] gradient = (MutableText[]) this.verifyAndGetGradient();
		MutableText message = new LiteralText("");
		String gradientTag;

		for (int i = 0; i != gradient.length; i++) {
			listTag.add(StringTag.of(Text.Serializer.toJson(gradient[i])));
			message.append(gradient[i]);
		}
		gradientTag = listTag.asString().replaceAll("'", "");

		this.client.keyboard.setClipboard(gradientTag);
	}

	public void initialColorListener(String text) {
		String textHexDigits = text.replaceAll("[^\\da-f]", "");
		if (!text.equals(textHexDigits))
			this.initialColorTextField.setText(textHexDigits);
		updatePreview();
	}

	public void finalColorListener(String text) {
		String textHexDigits = text.replaceAll("[^\\da-f]", "");
		if (!text.equals(textHexDigits))
			this.finalColorTextField.setText(textHexDigits);
		updatePreview();
	}

	public void updatePreview() {
		Object gradientObject = this.verifyAndGetGradient();
		this.preview = gradientObject instanceof MutableText[] ? GradientLogic.getGradientMessage((MutableText[]) gradientObject) : (MutableText) gradientObject;
	}

	public Object verifyAndGetGradient() {
		String initialColor = this.initialColorTextField.getText(),
			finalColor = this.finalColorTextField.getText(),
			message = this.messageTextField.getText();

		if (initialColor.length() != 6 || finalColor.length() != 6) {
			this.copyButton.active = false;
			return new TranslatableText("gradient.error.hexColorsLength").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_ERROR_COLOR)));
		} else if (message.length() <= 2) {
			this.copyButton.active = false;
			return new TranslatableText("gradient.error.messageLength").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_ERROR_COLOR)));
		} else {
			this.copyButton.active = true;
			return GradientLogic
				.hexRgbToMutableText(
					message, initialColor, finalColor,
					this.obfuscatedCheckbox.isChecked(), this.boldCheckbox.isChecked(), this.strikethroughCheckbox.isChecked(), this.underlineCheckbox.isChecked(), this.italicCheckbox.isChecked()
				);
		}
	}

	public void randomColors() {
		Random random = new Random(new Date().getTime());
		String initialColor = Integer.toString(random.nextInt(16777215), 16),
			finalColor = Integer.toString(random.nextInt(16777215), 16);

		while (initialColor.length() < 6)
			initialColor = "0".concat(initialColor);

		while (finalColor.length() < 6)
			finalColor = "0".concat(finalColor);

		this.initialColorTextField.setText(initialColor);
		this.finalColorTextField.setText(finalColor);
	}
}
