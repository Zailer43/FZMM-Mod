package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.utils.DisplayUtils;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.*;

import java.util.Date;
import java.util.Random;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class GradientScreen extends AbstractFzmmScreen {
	public GradientScreen() {
		super(new TranslatableText("gradient.title"));
	}

	private ButtonWidget copyButton;
	private ButtonWidget setItemNameButton;
	private ButtonWidget addLoreButton;
	private TextFieldWidget messageTextField;
	private TextFieldWidget initialColorTextField;
	private TextFieldWidget finalColorTextField;
	private CheckboxWidget obfuscatedCheckbox;
	private CheckboxWidget boldCheckbox;
	private CheckboxWidget strikethroughCheckbox;
	private CheckboxWidget underlineCheckbox;
	private CheckboxWidget italicCheckbox;
	private Text preview;

	public void init() {
		super.init();
		final short CHECKBOX_ROW = (short) (this.width / 2 + 80);
		assert this.client != null;
		assert this.client.player != null;

		this.copyButton = this.addDrawableChild(new ButtonWidget(20, this.height - 40, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("chat.copy"),
			(buttonWidget) -> this.copyExecute()
		));

		this.setItemNameButton = this.addDrawableChild(new ButtonWidget(130, this.height - 40, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("gradient.setItemName"),
				(buttonWidget) -> this.setItemName()
		));

		this.addLoreButton = this.addDrawableChild(new ButtonWidget(240, this.height - 40, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("gradient.addLore"),
				(buttonWidget) -> this.addLore()
		));

		this.messageTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, LINE1, 220, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("gradient.message"));
		this.messageTextField.setMaxLength(256);
		this.messageTextField.setChangedListener((text) -> this.preview = this.verifyAndGetGradient());
//		this.setInitialFocus(this.messageTextField);

		this.initialColorTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, LINE2, 220, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("gradient.initialColor"));
		this.initialColorTextField.setMaxLength(6);
		this.initialColorTextField.setChangedListener(this::initialColorListener);

		this.finalColorTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, LINE3, 220, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("gradient.finalColor"));
		this.finalColorTextField.setMaxLength(6);
		this.finalColorTextField.setChangedListener(this::finalColorListener);

		this.addDrawableChild(new ButtonWidget(this.width / 2 - 150, LINE4, 220, NORMAL_BUTTON_HEIGHT, new TranslatableText("gradient.randomColors"),
				(buttonWidget) -> this.randomColors()
		));

		obfuscatedCheckbox = this.addDrawableChild(new CheckboxWidget(CHECKBOX_ROW, 50, 20, 20, new LiteralText("Obfuscated"), false));
		boldCheckbox = this.addDrawableChild(new CheckboxWidget(CHECKBOX_ROW, 80, 20, 20, new LiteralText("Bold"), false));
		strikethroughCheckbox = this.addDrawableChild(new CheckboxWidget(CHECKBOX_ROW, 110, 20, 20, new LiteralText("Strikethrough"), false));
		underlineCheckbox = this.addDrawableChild(new CheckboxWidget(CHECKBOX_ROW, 140, 20, 20, new LiteralText("Underline"), false));
		italicCheckbox = this.addDrawableChild(new CheckboxWidget(CHECKBOX_ROW, 170, 20, 20, new LiteralText("Italic"), false));

		this.addSelectableChild(this.messageTextField);
		this.addSelectableChild(this.initialColorTextField);
		this.addSelectableChild(this.finalColorTextField);

		this.messageTextField.setText("Hello world");
		this.initialColorTextField.setText("ff0000");
		this.finalColorTextField.setText("0000ff");
		this.preview = this.verifyAndGetGradient();
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
		Text preview2 = this.preview;

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
		Text gradient = this.verifyAndGetGradient();

		this.client.keyboard.setClipboard(Text.Serializer.toJson(gradient));
	}

	public void setItemName() {
		FzmmUtils.renameHandItem(this.verifyAndGetGradient());
	}

	public void addLore() {
		DisplayUtils.addLoreToHandItem(this.verifyAndGetGradient());
	}

	public void initialColorListener(String text) {
		String textHexDigits = text.replaceAll("[^\\da-f]", "");
		if (!text.equals(textHexDigits))
			this.initialColorTextField.setText(textHexDigits);
		this.preview = this.verifyAndGetGradient();
	}

	public void finalColorListener(String text) {
		String textHexDigits = text.replaceAll("[^\\da-f]", "");
		if (!text.equals(textHexDigits))
			this.finalColorTextField.setText(textHexDigits);
		this.preview = this.verifyAndGetGradient();
	}

	public Text verifyAndGetGradient() {
		String initialColor = this.initialColorTextField.getText(),
			finalColor = this.finalColorTextField.getText(),
			message = this.messageTextField.getText();

		if (initialColor.length() != 6 || finalColor.length() != 6) {
			this.toggleExecuteButtons(false);
			return new TranslatableText("gradient.error.hexColorsLength").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_ERROR_COLOR)));
		} else if (message.length() <= 2) {
			this.toggleExecuteButtons(false);
			return new TranslatableText("gradient.error.messageLength").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(TEXT_ERROR_COLOR)));
		} else {
			this.toggleExecuteButtons(true);
			return GradientLogic
				.hexRgbToMutableText(
					message, initialColor, finalColor,
					this.obfuscatedCheckbox.isChecked(), this.boldCheckbox.isChecked(), this.strikethroughCheckbox.isChecked(), this.underlineCheckbox.isChecked(), this.italicCheckbox.isChecked()
				);
		}
	}

	public void toggleExecuteButtons(boolean bl) {
		this.copyButton.active = bl;
		this.setItemNameButton.active = bl;
		this.addLoreButton.active = bl;
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
