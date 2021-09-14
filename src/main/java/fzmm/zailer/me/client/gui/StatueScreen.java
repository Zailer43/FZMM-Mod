package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.gui.widget.NumberFieldWidget;
import fzmm.zailer.me.utils.FzmmUtils;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmChatLinkScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.util.math.Direction;

import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.regex.Pattern;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class StatueScreen extends AbstractFzmmScreen {
	protected ButtonWidget executeButton, updateButton, directionButton,
		skinButton, containerButton, faqButton;
	protected static Text progress;
	protected static boolean active, errorImage;
	private TextFieldWidget skinTextField, nameTextField;
	private NumberFieldWidget xNumberField, yNumberField, zNumberField;
	private boolean isShulker;
	private TranslatableText errorImageMessage;
	private Direction direction;
	private SkinMode skinMode;

	protected StatueScreen() {
		super(new TranslatableText("playerStatue.title"));
	}

	protected void init() {
		super.init();
		assert this.client != null;
		assert this.client.player != null;
		progress = new LiteralText("");

		this.executeButton = this.addDrawableChild(new ButtonWidget(20, this.height - 40, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("gui.execute"),
			(buttonWidget) -> this.execute()
		));

		this.faqButton = this.addDrawableChild(new ButtonWidget(this.width - 80, 20, 60, NORMAL_BUTTON_HEIGHT, new TranslatableText("gui.faq"),
				(buttonWidget) -> new UrlOpener(this.client, "https://github.com/Zailer43/FZMM-Mod/wiki/FAQ-Player-Statue").run()
		));

		this.skinTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, LINE1, 200, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("playerStatue.skin.fromPath"));
		this.skinTextField.setMaxLength(512);
		this.skinTextField.setChangedListener(this::skinListener);
		this.setInitialFocus(this.skinTextField);

		this.skinButton = this.addDrawableChild(new ButtonWidget(this.width / 2 + 50, LINE1, 104, NORMAL_BUTTON_HEIGHT, new LiteralText(""),
				(buttonWidget) -> {
					this.cycleSkin();
					this.updateSkin();
				}
		));

		this.xNumberField = new NumberFieldWidget(this.textRenderer, this.width / 2 - 154, LINE2, 100, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("axis.x"), -30000000, 30000000);
		this.xNumberField.setMaxLength(9);

		this.yNumberField = new NumberFieldWidget(this.textRenderer, this.width / 2 - 50, LINE2, 100, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("axis.y"), Short.MIN_VALUE, Short.MAX_VALUE);
		this.yNumberField.setMaxLength(6);

		this.zNumberField = new NumberFieldWidget(this.textRenderer, this.width / 2 + 54, LINE2, 100, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("axis.z"), -30000000, 30000000);
		this.zNumberField.setMaxLength(9);

		this.xNumberField.setText(String.valueOf(this.client.player.getBlockX()));
		this.yNumberField.setText(String.valueOf(this.client.player.getBlockY()));
		this.zNumberField.setText(String.valueOf(this.client.player.getBlockZ()));

		this.nameTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 154, LINE3, 308, NORMAL_TEXT_FIELD_HEIGHT, new TranslatableText("playerStatue.name"));
		this.nameTextField.setMaxLength(1024);

		this.addSelectableChild(this.skinTextField);
		this.addSelectableChild(this.xNumberField);
		this.addSelectableChild(this.yNumberField);
		this.addSelectableChild(this.zNumberField);
		this.addSelectableChild(this.nameTextField);

		this.directionButton = this.addDrawableChild(new ButtonWidget(this.width / 2 - 102, LINE4, 100, NORMAL_BUTTON_HEIGHT, new LiteralText(""),
				(buttonWidget) -> {
					this.cycleDirection();
					this.updateDirection();
				}
		));

		this.containerButton = this.addDrawableChild(new ButtonWidget(this.width / 2 + 2, LINE4, 100, NORMAL_BUTTON_HEIGHT, new LiteralText(""),
				(buttonWidget) -> {
					this.isShulker = !this.isShulker;
					this.updateContainer();
				}
		));

		this.updateButton = this.addDrawableChild(new ButtonWidget(124, this.height - 40, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("playerStatue.update"),
			(buttonWidget) -> this.updateStatue()
		));

		this.nameTextField.setText(StatueLogic.getStatueName());

		this.direction = this.client.player.getHorizontalFacing();
		this.skinMode = SkinMode.NICK;
		this.isShulker = true;

		this.updateDirection();
		this.updateSkin();
		this.updateContainer();

	}

	public void resize(MinecraftClient client, int width, int height) {
		String skin = this.skinTextField.getText(),
			name = this.nameTextField.getText();
		Text progress2 = progress;
		int x = this.xNumberField.getNumber(),
		z = this.zNumberField.getNumber();
		short y = (short) this.yNumberField.getNumber();
		Direction direction2 = this.direction;
		SkinMode skinMode2 = this.skinMode;
		boolean isShulker2 = this.isShulker;

		this.init(client, width, height);

		this.skinTextField.setText(skin);
		progress = progress2;
		this.xNumberField.setText(String.valueOf(x));
		this.yNumberField.setText(String.valueOf(y));
		this.zNumberField.setText(String.valueOf(z));
		this.nameTextField.setText(name);
		this.direction = direction2;
		this.updateDirection();
		skinMode = skinMode2;
		this.updateSkin();
		this.isShulker = isShulker2;
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		drawCenteredText(matrices, this.textRenderer, new TranslatableText("playerStatue.skin"), this.width / 2 - 54, LINE1 - 10, TEXT_COLOR);
		this.skinTextField.render(matrices, mouseX, mouseY, delta);

		drawCenteredText(matrices, this.textRenderer, new LiteralText("X"), this.width / 2 - 104, LINE2 - 10, TEXT_COLOR);
		this.xNumberField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new LiteralText("Y"), this.width / 2, LINE2 - 10, TEXT_COLOR);
		this.yNumberField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new LiteralText("Z"), this.width / 2 + 104, LINE2 - 10, TEXT_COLOR);
		this.zNumberField.render(matrices, mouseX, mouseY, delta);

		drawCenteredText(matrices, this.textRenderer, new TranslatableText("playerStatue.name"), this.width / 2, LINE3 - 10, TEXT_COLOR);
		this.nameTextField.render(matrices, mouseX, mouseY, delta);

		if (errorImage) {
			drawCenteredText(matrices, this.textRenderer, this.errorImageMessage, this.width / 2, LINE5 - 10, TEXT_ERROR_COLOR);
		} else {
			drawCenteredText(matrices, this.textRenderer, progress, this.width / 2, LINE5 - 10, TEXT_COLOR);
			this.executeButton.active = true;
		}
		if (active || errorImage) {
			this.executeButton.active = false;
		}
	}

	public void execute() {
		active = true;
		String name = null;
		if (!this.nameTextField.getText().isEmpty()) {
			name = this.nameTextField.getText();
		}
		StatueLogic.generateStatue(this.skinTextField.getText(),
				this.xNumberField.getNumber(),
				(short) this.yNumberField.getNumber(),
				this.zNumberField.getNumber(),
				name,
				this.direction,
				this.skinMode,
				this.isShulker);
	}

	public void updateStatue() {
		assert this.client != null;
		assert this.client.player != null;
		ItemStack statue = StatueLogic.updateStatue(this.client.player.getMainHandStack(),
				this.xNumberField.getNumber(),
				(short) this.yNumberField.getNumber(),
				this.zNumberField.getNumber(),
				this.direction,
				this.nameTextField.getText()
				);
		FzmmUtils.giveItem(statue);
	}

	public void skinListener(String text) {
		if (text.isEmpty()) {
			return;
		}
		if (skinMode == SkinMode.FROM_PC) {
			try {
				if (Files.exists(Paths.get(text)))
					errorImage = false;
				else {
					errorImage = true;
					this.errorImageMessage = new TranslatableText("imagetext.error.pathNotExists");
				}
			} catch (InvalidPathException e) {
				errorImage = true;
				this.errorImageMessage = new TranslatableText("imagetext.error.invalidPath");
			}
		} else {
			String string = Pattern.compile("[^a-zA-Z0-9_]").matcher(text).replaceAll("");
			if (!string.equals(text)) {
				this.skinTextField.setText(string);
			}
		}
	}

	public void cycleDirection() {
		switch (this.direction) {
			case NORTH -> this.direction = Direction.EAST;
			case EAST -> this.direction = Direction.SOUTH;
			case SOUTH -> this.direction = Direction.WEST;
			case WEST -> this.direction = Direction.NORTH;

		}
	}

	public void updateDirection() {
		switch (this.direction) {
			case NORTH -> this.directionButton.setMessage(new TranslatableText("playerStatue.direction.north"));
			case EAST -> this.directionButton.setMessage(new TranslatableText("playerStatue.direction.east"));
			case SOUTH -> this.directionButton.setMessage(new TranslatableText("playerStatue.direction.south"));
			case WEST -> this.directionButton.setMessage(new TranslatableText("playerStatue.direction.west"));
		}
	}

	public void cycleSkin() {
		switch (this.skinMode) {
			case NICK -> this.skinMode = SkinMode.FROM_PC;
			case FROM_PC -> {
				this.skinMode = SkinMode.NICK;
				errorImage = false;
			}

		}
	}

	public void updateSkin() {
		switch (this.skinMode) {
			case NICK -> {
				this.skinButton.setMessage(new TranslatableText("playerStatue.skin.fromUsername"));
				this.skinTextField.setMaxLength(16);
			}
			case FROM_PC -> {
				this.skinButton.setMessage(new TranslatableText("playerStatue.skin.fromPath"));
				this.skinTextField.setMaxLength(512);
			}
		}
	}

	protected enum SkinMode {
		NICK,
		FROM_PC;

		SkinMode() {
		}
	}

	public void updateContainer() {
		if (isShulker) {
			this.containerButton.setMessage(new TranslatableText("block.minecraft.shulker_box"));
		} else {
			this.containerButton.setMessage(new TranslatableText("block.minecraft.barrel"));
		}
	}

	private record UrlOpener(MinecraftClient client, String url) implements Runnable, BooleanConsumer {
		@Override
		public void run() {
			this.client.setScreen(new ConfirmChatLinkScreen(this, url, true));
		}

		@Override
		public void accept(boolean bl) {
			if (bl) {
				Util.getOperatingSystem().open(url);
			}

			this.client.setScreen(null);
		}
	}
}
