package fzmm.zailer.me.client.gui.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class NumberFieldWidget extends TextFieldWidget {
	private final int minValue,
		maxValue;

	public NumberFieldWidget(TextRenderer textRenderer, int x, int y, int width, int height, Text text, int minValue, int maxValue) {
		super(textRenderer, x, y, width, height, text);
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public int getNumber() {
		String text = super.getText();
		int number;
		if (text.matches("^-?[\\d]{1,10}$")) {
			number = Integer.parseInt(text.matches("-") || text.isEmpty() ? "0" : text);

			if (number < this.minValue) {
				number = this.minValue;
			} else if (number > this.maxValue) {
				number = this.maxValue;
			}
		} else {
			number = Math.max(this.minValue, 0);
		}

		return number;
	}

	public void write(String string) {
		string = string.replaceAll("[^\\d-]", "");
		super.write(string);
		this.verifyNumberRange();
	}

	public void eraseWords(int wordOffset) {
		super.eraseWords(wordOffset);
		this.verifyNumberRange();
	}

	public void eraseCharacters(int characterOffset) {
		super.eraseCharacters(characterOffset);
		this.verifyNumberRange();
	}

	public void verifyNumberRange() {
		String string = this.getText();
		int errorColor = 16733525;
		if (string.matches("^-?[\\d]{1,10}$")) {
			long number = Long.parseLong(string);

			if (number < this.minValue) {
				this.setEditableColor(errorColor);
			} else if (number > this.maxValue) {
				this.setEditableColor(errorColor);
			} else {
				this.setEditableColor(14737632);
			}
		} else {
			super.setEditableColor(errorColor);
		}
	}
}
