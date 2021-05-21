package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.config.FzmmConfig;
import fzmm.zailer.me.utils.FzmmUtils;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class EncodebookScreen extends FzmmBaseScreen {

	protected ButtonWidget executeButton,
		getDecoderButton;
	private TextFieldWidget messageTextField,
		seedTextField,
		authorTextField,
		paddingCharsTextField,
		maxMessageLengthTextField,
		bookTitleTextField;

	protected EncodebookScreen() {
		super(new LiteralText("Encodebook"));
	}

	protected void init() {
		super.init();
		assert this.client != null;
		assert this.client.player != null;

		this.executeButton = this.addButton(new ButtonWidget(20, this.height - 40, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("gui.execute"),
			(buttonWidget) -> this.execute()
		));
		this.getDecoderButton = this.addButton(new ButtonWidget(this.width / 2 - 50, LINE5, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new LiteralText("encodebook.getDecoder"),
			(buttonWidget) -> this.getDecoder()
		));

		this.messageTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, LINE1, 300, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("book.message"));
		this.messageTextField.setMaxLength(256);
		this.setInitialFocus(this.messageTextField);

		this.paddingCharsTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, LINE2, 300, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("encodebook.paddingCharacters"));
		this.paddingCharsTextField.setMaxLength(256);

		this.seedTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, LINE3, 50, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("encodebook.seed"));
		this.seedTextField.setMaxLength(8);
		this.seedTextField.setChangedListener(this::seedListener);

		this.authorTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 96, LINE3, 246, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("book.author"));
		this.authorTextField.setMaxLength(100);

		this.maxMessageLengthTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, LINE4, 50, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("encodebook.messageLength"));
		this.maxMessageLengthTextField.setMaxLength(3);
		this.maxMessageLengthTextField.setChangedListener(this::messageLengthListener);

		this.bookTitleTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 96, LINE4, 246, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("book.title"));
		this.bookTitleTextField.setMaxLength(100);

		this.children.add(this.messageTextField);
		this.children.add(this.seedTextField);
		this.children.add(this.authorTextField);
		this.children.add(this.paddingCharsTextField);
		this.children.add(this.maxMessageLengthTextField);
		this.children.add(this.bookTitleTextField);

		FzmmConfig.Encodebook config = AutoConfig.getConfigHolder(FzmmConfig.class).getConfig().encodebook;

		messageTextField.setText(config.defaultBookMessage);
		seedTextField.setText("1");
		authorTextField.setText(this.client.player.getName().getString());
		paddingCharsTextField.setText(config.myRandom);
		maxMessageLengthTextField.setText(String.valueOf(config.messageLength));
		bookTitleTextField.setText(config.bookTitle);
	}

	public void resize(MinecraftClient client, int width, int height) {
		String messageTextField2 = messageTextField.getText(),
			seedTextField2 = seedTextField.getText(),
			authorTextField2 = authorTextField.getText(),
			paddingCharsTextField2 = paddingCharsTextField.getText(),
			maxMessageLengthTextField2 = maxMessageLengthTextField.getText(),
			bookTitleTextField2 = bookTitleTextField.getText();

		this.init(client, width, height);

		messageTextField.setText(messageTextField2);
		seedTextField.setText(seedTextField2);
		authorTextField.setText(authorTextField2);
		paddingCharsTextField.setText(paddingCharsTextField2);
		maxMessageLengthTextField.setText(maxMessageLengthTextField2);
		bookTitleTextField.setText(bookTitleTextField2);
		messageLengthListener(maxMessageLengthTextField2);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		drawCenteredText(matrices, this.textRenderer, new TranslatableText("book.message"), this.width / 2, LINE1 - 10, TEXT_COLOR);
		this.messageTextField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new TranslatableText("encodebook.paddingCharacters"), this.width / 2, LINE2 - 10, TEXT_COLOR);
		this.paddingCharsTextField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new TranslatableText("encodebook.seed"), this.width / 2 - 125, LINE3 - 10, TEXT_COLOR);
		this.seedTextField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new TranslatableText("book.author"), this.width / 2 + 32, LINE3 - 10, TEXT_COLOR);
		this.authorTextField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new TranslatableText("encodebook.messageLength"), this.width / 2 - 125, LINE4 - 10, TEXT_COLOR);
		this.maxMessageLengthTextField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new TranslatableText("book.title"), this.width / 2 + 32, LINE4 - 10, TEXT_COLOR);
		this.bookTitleTextField.render(matrices, mouseX, mouseY, delta);
	}

	public void execute() {
		int seed = this.seedTextField.getText().isEmpty() ? 1 : Integer.parseInt(this.seedTextField.getText());
		short maxMessageLength = this.maxMessageLengthTextField.getText().isEmpty() ? 255 : (short) Integer.parseInt(this.maxMessageLengthTextField.getText());
		String message = this.messageTextField.getText().isEmpty() ? "Hello world" : this.messageTextField.getText(),
			paddingChars = this.paddingCharsTextField.getText().isEmpty() ? "qwertyuiopasdfghjklzxcvbnm" : this.paddingCharsTextField.getText();

		EncodebookLogic.EncodeBook(seed, message, this.authorTextField.getText(), paddingChars, maxMessageLength, this.bookTitleTextField.getText());
	}

	public void getDecoder() {
		int seed = this.seedTextField.getText().isEmpty() ? 1 : Integer.parseInt(this.seedTextField.getText());
		short maxMessageLength = this.maxMessageLengthTextField.getText().isEmpty() ? 255 : (short) Integer.parseInt(this.maxMessageLengthTextField.getText());
		EncodebookLogic.showDecoderInChat(seed, maxMessageLength);
	}

	private void seedListener(String text) {
		String seedStringNumber = FzmmUtils.StringNumber(text);
		if (!text.equals(seedStringNumber))
			this.seedTextField.setText(seedStringNumber);
	}

	private void messageLengthListener(String text) {
		String messageLengthStringNumber = FzmmUtils.StringNumber(text);
		if (!text.equals(messageLengthStringNumber))
			this.maxMessageLengthTextField.setText(messageLengthStringNumber);
		this.messageTextField.setMaxLength(messageLengthStringNumber.isEmpty() ? 255 : Integer.parseInt(messageLengthStringNumber));
	}
}
