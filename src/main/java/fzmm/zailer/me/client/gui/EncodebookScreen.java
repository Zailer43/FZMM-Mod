package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.gui.widget.NumberFieldWidget;
import fzmm.zailer.me.config.Configs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class EncodebookScreen extends AbstractFzmmScreen {

	private TextFieldWidget messageTextField,
		authorTextField,
		paddingCharsTextField,
		bookTitleTextField;
	private NumberFieldWidget seedNumberField,
		maxMsgLengthNumberField;

	public EncodebookScreen() {
		super(new TranslatableText("encodebook.title"));
	}

	public void init() {
		super.init();
		assert this.client != null;
		assert this.client.player != null;

		this.addDrawableChild(new ButtonWidget(20, this.height - 40, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("gui.execute"),
				(buttonWidget) -> this.execute()
		));
		this.addDrawableChild(new ButtonWidget(this.width / 2 - 50, LINE5, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("encodebook.getDecoder"),
				(buttonWidget) -> this.getDecoder()
		));

		this.messageTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, LINE1, 300, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("book.message"));
		this.messageTextField.setMaxLength(256);
//		this.setInitialFocus(this.messageTextField);

		this.paddingCharsTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, LINE2, 300, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("encodebook.paddingCharacters"));
		this.paddingCharsTextField.setMaxLength(256);

		this.seedNumberField = new NumberFieldWidget(this.textRenderer, this.width / 2 - 150, LINE3, 50, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("encodebook.seed"), 0, 10000000);
		this.seedNumberField.setMaxLength(8);

		this.authorTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 96, LINE3, 246, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("book.author"));
		this.authorTextField.setMaxLength(100);

		this.maxMsgLengthNumberField = new NumberFieldWidget(this.textRenderer, this.width / 2 - 150, LINE4, 50, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("encodebook.messageLength"), 0, 255);
		this.maxMsgLengthNumberField.setMaxLength(3);

		this.bookTitleTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 96, LINE4, 246, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("book.title"));
		this.bookTitleTextField.setMaxLength(100);

		this.addSelectableChild(this.messageTextField);
		this.addSelectableChild(this.seedNumberField);
		this.addSelectableChild(this.authorTextField);
		this.addSelectableChild(this.paddingCharsTextField);
		this.addSelectableChild(this.maxMsgLengthNumberField);
		this.addSelectableChild(this.bookTitleTextField);

		messageTextField.setText(Configs.Encodebook.DEFAULT_BOOK_MESSAGE.getDefaultStringValue());
		seedNumberField.setText("1");
		authorTextField.setText(this.client.player.getName().getString());
		paddingCharsTextField.setText(Configs.Encodebook.PADDING.getStringValue());
		maxMsgLengthNumberField.setText(String.valueOf(Configs.Encodebook.MESSAGE_MAX_LENGTH.getIntegerValue()));
		bookTitleTextField.setText(Configs.Encodebook.DEFAULT_BOOK_TITLE.getStringValue());
	}

	public void resize(MinecraftClient client, int width, int height) {
		String messageTextField2 = messageTextField.getText(),
			seedTextField2 = seedNumberField.getText(),
			authorTextField2 = authorTextField.getText(),
			paddingCharsTextField2 = paddingCharsTextField.getText(),
			maxMessageLengthTextField2 = maxMsgLengthNumberField.getText(),
			bookTitleTextField2 = bookTitleTextField.getText();

		this.init(client, width, height);

		messageTextField.setText(messageTextField2);
		seedNumberField.setText(seedTextField2);
		authorTextField.setText(authorTextField2);
		paddingCharsTextField.setText(paddingCharsTextField2);
		maxMsgLengthNumberField.setText(maxMessageLengthTextField2);
		bookTitleTextField.setText(bookTitleTextField2);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		drawCenteredText(matrices, this.textRenderer, new TranslatableText("book.message"), this.width / 2, LINE1 - 10, TEXT_COLOR);
		this.messageTextField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new TranslatableText("encodebook.paddingCharacters"), this.width / 2, LINE2 - 10, TEXT_COLOR);
		this.paddingCharsTextField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new TranslatableText("encodebook.seed"), this.width / 2 - 125, LINE3 - 10, TEXT_COLOR);
		this.seedNumberField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new TranslatableText("book.author"), this.width / 2 + 27, LINE3 - 10, TEXT_COLOR);
		this.authorTextField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new TranslatableText("encodebook.messageLength"), this.width / 2 - 125, LINE4 - 10, TEXT_COLOR);
		this.maxMsgLengthNumberField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new TranslatableText("book.title"), this.width / 2 + 27, LINE4 - 10, TEXT_COLOR);
		this.bookTitleTextField.render(matrices, mouseX, mouseY, delta);
	}

	public void execute() {
		String message = this.messageTextField.getText().isEmpty() ? "Hello world" : this.messageTextField.getText(),
			paddingChars = this.paddingCharsTextField.getText().isEmpty() ? "qwertyuiopasdfghjklzxcvbnm" : this.paddingCharsTextField.getText();

		EncodebookLogic.EncodeBook(this.seedNumberField.getNumber(), message, this.authorTextField.getText(), paddingChars, (short) this.maxMsgLengthNumberField.getNumber(), this.bookTitleTextField.getText());
	}

	public void getDecoder() {
		EncodebookLogic.showDecoderInChat(this.seedNumberField.getNumber(), (short) this.maxMsgLengthNumberField.getNumber());
	}
}
