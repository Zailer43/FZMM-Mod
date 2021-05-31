package fzmm.zailer.me.client.gui.converters;

import fzmm.zailer.me.client.gui.NumberFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import java.util.UUID;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class UuidScreen extends ConvertersBaseScreen {

	private TextFieldWidget uuidTextField;
	private final NumberFieldWidget[] intArrayFields = new NumberFieldWidget[4];
	protected ButtonWidget setUuidFromIntArrayButton,
		copyIntArrayTagButton;
	private boolean uuidErrorLength,
		uuidFormatError;

	//TODO: añadir que se pueda generar UUID a base de un nick y y un checkbox que diga si es premium o no
	public UuidScreen() {
		super(new TranslatableText("converters.uuid"));
	}

	protected void init() {
		super.init();

		this.uuidTextField = new TextFieldWidget(this.textRenderer, this.width / 2 - 150, LINE1, 300, NORMAL_TEXT_FIELD_HEIGHT, new LiteralText("UUID"));
		this.uuidTextField.setMaxLength(36); // 32 hex digits + 4 dash
		this.uuidTextField.setChangedListener(this::uuidListener);
		this.setInitialFocus(this.uuidTextField);

		for (short i = 0; i != 4; i++) {
			short intArrayWidth = 332 / 4;
			this.intArrayFields[i] = new NumberFieldWidget(this.textRenderer, this.width / 2 + 4 * i + intArrayWidth * i - ((4 * (4 - 1) + intArrayWidth * 4) / 2), LINE2, intArrayWidth, NORMAL_TEXT_FIELD_HEIGHT, new LiteralText("Int Array " + i), Integer.MIN_VALUE, Integer.MAX_VALUE);
			this.intArrayFields[i].setMaxLength(11); // -2147483648
			this.children.add(this.intArrayFields[i]);
		}
		this.children.add(this.uuidTextField);

		this.setUuidFromIntArrayButton = this.addButton(new ButtonWidget(this.width / 2 - 150, LINE3, 148, NORMAL_BUTTON_HEIGHT, new TranslatableText("uuid.setUuid"),
			(buttonWidget) -> this.setUuidFromIntArray()
		));
		this.copyIntArrayTagButton = this.addButton(new ButtonWidget(this.width / 2 + 2, LINE3, 148, NORMAL_BUTTON_HEIGHT, new TranslatableText("uuid.copyIntArray"),
			(buttonWidget) -> this.copyIntArrayTag()
		));
	}

	public void resize(MinecraftClient client, int width, int height) {
		String uuid = this.uuidTextField.getText();
		String[] intArrayString = new String[4];
		for (int i = 0; i != 4; i++)
			intArrayString[i] = this.intArrayFields[i].getText();

		this.init(client, width, height);

		this.uuidTextField.setText(uuid);
		for (int i = 0; i != 4; i++)
			this.intArrayFields[i].setText(intArrayString[i]);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		drawCenteredText(matrices, this.textRenderer, new LiteralText("UUID"), this.width / 2, LINE1 - 10, TEXT_COLOR);
		this.uuidTextField.render(matrices, mouseX, mouseY, delta);

		drawCenteredText(matrices, this.textRenderer, new TranslatableText("uuid.intArray"), this.width / 2, LINE2 - 10, TEXT_COLOR);
		for (int i = 0; i != 4; i++)
			this.intArrayFields[i].render(matrices, mouseX, mouseY, delta);

		if (this.uuidErrorLength)
			drawCenteredText(matrices, this.textRenderer, new TranslatableText("uuid.error.length"), this.width / 2, LINE4 - 10, TEXT_ERROR_COLOR);
		else if (this.uuidFormatError)
			drawCenteredText(matrices, this.textRenderer, new TranslatableText("uuid.error.format"), this.width / 2, LINE4 - 10, TEXT_ERROR_COLOR);

	}

	public void uuidListener(String text) {
		String uuidText = text.replaceAll("[^0-9a-f-]", "");
		if (!text.equals(uuidText)) {
			uuidTextField.setText(uuidText);
			return;
		}

		if (uuidText.length() == 32) {
			uuidText = uuidText.substring(0, 8) + "-" + uuidText.substring(8, 12) + "-" + uuidText.substring(12, 16) + "-" +
				uuidText.substring(16, 20) + "-" + uuidText.substring(20, 32);
		} else {
			this.uuidErrorLength = true;
		}

		try {
			if (uuidText.length() == 36) {
				this.uuidErrorLength = false;
				UUID uuid = UUID.fromString(uuidText);
				int[] i = new int[4];
				long msb = uuid.getMostSignificantBits(), lsb = uuid.getLeastSignificantBits();
				i[0] = (int) (msb >> 32);
				i[1] = (int) (msb);
				i[2] = (int) (lsb >> 32);
				i[3] = (int) (lsb);


				for (int a = 0; a != 4; a++)
					this.intArrayFields[a].setText(String.valueOf(i[a]));

				this.uuidFormatError = false;
			}
		} catch (IllegalArgumentException e) {
			this.uuidFormatError = true;
		}
	}

	public void setUuidFromIntArray() {
		int[] intArray = new int[4];
		for (int i = 0; i != 4; i++) {
			intArray[i] = this.intArrayFields[i].getNumber();
		}
		long msb = Integer.toUnsignedLong(intArray[0]), lsb = Integer.toUnsignedLong(intArray[2]);
		msb = (msb << 32) | Integer.toUnsignedLong(intArray[1]);
		lsb = (lsb << 32) | Integer.toUnsignedLong(intArray[3]);

		this.uuidTextField.setText(new UUID(msb, lsb).toString());
	}

	public void copyIntArrayTag() {
		assert this.client != null;

		int[] intArray = new int[4];
		for (int i = 0; i != 4; i++) {
			intArray[i] = this.intArrayFields[i].getNumber();
		}

		this.client.keyboard.setClipboard("[I;" + intArray[0] + "," + intArray[1] + "," + intArray[2] + "," + intArray[3] + "]");
	}
}
