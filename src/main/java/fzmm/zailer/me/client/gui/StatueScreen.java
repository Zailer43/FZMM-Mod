package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;
import static fzmm.zailer.me.client.gui.ScreenConstants.NORMAL_TEXT_FIELD_HEIGHT;

public class StatueScreen extends FzmmBaseScreen {
	protected ButtonWidget executeButton, updateButton, directionButton;
	private TextFieldWidget skinTextField, nameTextField;
	private NumberFieldWidget xNumberField, yNumberField, zNumberField;
	private CheckboxWidget nameCheckbox;
	private boolean errorImage;
	private TranslatableText errorImageMessage;
	protected static Text progress;
	private StatueLogic.Direction direction;

	protected StatueScreen() {
		super(new LiteralText("Player Statue"));
	}

	protected void init() {
		super.init();
		assert this.client != null;
		assert this.client.player != null;
		progress = new LiteralText("");

		this.executeButton = this.addDrawableChild(new ButtonWidget(20, this.height - 40, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("gui.execute"),
			(buttonWidget) -> this.execute()
		));

		this.skinTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, LINE1, 308, NORMAL_TEXT_FIELD_HEIGHT, new LiteralText("Ubicación de la imagen en la PC"));
		this.skinTextField.setMaxLength(512);
		this.skinTextField.setChangedListener(this::skinListener);
		this.setInitialFocus(this.skinTextField);

		this.xNumberField = new NumberFieldWidget(this.textRenderer, this.width / 2 - 154, LINE2, 100, NORMAL_TEXT_FIELD_HEIGHT, new LiteralText("X"), -30000000, 30000000);
		this.xNumberField.setMaxLength(9);

		this.yNumberField = new NumberFieldWidget(this.textRenderer, this.width / 2 - 50, LINE2, 100, NORMAL_TEXT_FIELD_HEIGHT, new LiteralText("Y"), Short.MIN_VALUE, Short.MAX_VALUE);
		this.yNumberField.setMaxLength(6);

		this.zNumberField = new NumberFieldWidget(this.textRenderer, this.width / 2 + 54, LINE2, 100, NORMAL_TEXT_FIELD_HEIGHT, new LiteralText("Z"), -30000000, 30000000);
		this.zNumberField.setMaxLength(9);

		this.xNumberField.setText(String.valueOf((int) this.client.player.getX()));
		this.yNumberField.setText(String.valueOf((int) this.client.player.getY()));
		this.zNumberField.setText(String.valueOf((int) this.client.player.getZ()));

		this.nameCheckbox = this.addDrawableChild(new CheckboxWidget(this.width / 2 + 80, LINE3, 20, 20, new LiteralText("Name"), false));

		this.nameTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, LINE3, 224, NORMAL_TEXT_FIELD_HEIGHT, new LiteralText("Statue Name"));
		this.nameTextField.setMaxLength(1024);

		this.addSelectableChild(this.skinTextField);
		this.addSelectableChild(this.xNumberField);
		this.addSelectableChild(this.yNumberField);
		this.addSelectableChild(this.zNumberField);
		this.addSelectableChild(this.nameTextField);

		this.directionButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 50, LINE4, 100, NORMAL_BUTTON_HEIGHT, new LiteralText("North"),
				(buttonWidget) -> {
					this.cycleDirection();
					this.updateDirection();
				}
		));

		this.updateButton = this.addDrawableChild(new ButtonWidget(124, this.height - 40, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new LiteralText("Update"),
			(buttonWidget) -> this.setCoordinates()
		));

		this.direction = StatueLogic.Direction.NORTH;
	}

	public void resize(MinecraftClient client, int width, int height) {
		String skin = this.skinTextField.getText(),
			name = this.nameTextField.getText();
		Text progress2 = progress;
		int x = this.xNumberField.getNumber(),
		z = this.zNumberField.getNumber();
		short y = (short) this.yNumberField.getNumber();
		boolean nameBool = nameCheckbox.isChecked();
		StatueLogic.Direction direction2 = this.direction;

		this.init(client, width, height);

		this.skinTextField.setText(skin);
		progress = progress2;
		this.xNumberField.setText(String.valueOf(x));
		this.yNumberField.setText(String.valueOf(y));
		this.zNumberField.setText(String.valueOf(z));
		if (this.nameCheckbox.isChecked() != nameBool)
			this.nameCheckbox.onPress();
		this.nameTextField.setText(name);
		this.direction = direction2;
		this.updateDirection();
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		drawCenteredText(matrices, this.textRenderer, new LiteralText("Ubicación de la imagen en la PC"), this.width / 2, LINE1 - 10, TEXT_COLOR);
		this.skinTextField.render(matrices, mouseX, mouseY, delta);

		drawCenteredText(matrices, this.textRenderer, new LiteralText("X"), this.width / 2 - 104, LINE2 - 10, TEXT_COLOR);
		this.xNumberField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new LiteralText("Y"), this.width / 2, LINE2 - 10, TEXT_COLOR);
		this.yNumberField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new LiteralText("Z"), this.width / 2 + 104, LINE2 - 10, TEXT_COLOR);
		this.zNumberField.render(matrices, mouseX, mouseY, delta);

		drawCenteredText(matrices, this.textRenderer, new LiteralText("Statue Name"), this.width / 2 - 40, LINE3 - 10, TEXT_COLOR);
		this.nameTextField.render(matrices, mouseX, mouseY, delta);

		if (errorImage) {
			drawCenteredText(matrices, this.textRenderer, this.errorImageMessage, this.width / 2, LINE5 - 10, TEXT_ERROR_COLOR);
			this.executeButton.active = false;
		} else {
			 drawCenteredText(matrices, this.textRenderer, progress, this.width / 2, LINE5 - 10, TEXT_COLOR);
			 this.executeButton.active = true;
		}
	}

	public void execute() {
		String name = null;
		if (this.nameCheckbox.isChecked()) {
			name = this.nameTextField.getText();
		}
		StatueLogic.generateStatue(this.skinTextField.getText(), this.xNumberField.getNumber(), (short) this.yNumberField.getNumber(), this.zNumberField.getNumber(), name, this.direction);
	}

	public void setCoordinates() {
		assert this.client != null;
		assert this.client.player != null;
		ItemStack statue = StatueLogic.updateStatue(this.client.player.getMainHandStack(), this.xNumberField.getNumber(), (short) this.yNumberField.getNumber(), this.zNumberField.getNumber(), this.direction);
		FzmmUtils.giveItem(statue);
	}

	public void skinListener(String text) {
		try {
			if (Files.exists(Paths.get(text)))
				this.errorImage = false;
			else {
				this.errorImage = true;
				this.errorImageMessage = new TranslatableText("imagetext.error.pathNotExists");
			}
		} catch (InvalidPathException e) {
			this.errorImage = true;
			this.errorImageMessage = new TranslatableText("imagetext.error.invalidPath");
		}
	}

	public void cycleDirection() {
		switch (this.direction) {
			case NORTH -> this.direction = StatueLogic.Direction.EAST;
			case EAST -> this.direction = StatueLogic.Direction.SOUTH;
			case SOUTH -> this.direction = StatueLogic.Direction.WEST;
			case WEST -> this.direction = StatueLogic.Direction.NORTH;

		}
	}

	public void updateDirection() {
		switch (this.direction) {
			case NORTH -> this.directionButton.setMessage(new LiteralText("North"));
			case EAST -> this.directionButton.setMessage(new LiteralText("East"));
			case SOUTH -> this.directionButton.setMessage(new LiteralText("South"));
			case WEST -> this.directionButton.setMessage(new LiteralText("West"));
		}
	}
}
