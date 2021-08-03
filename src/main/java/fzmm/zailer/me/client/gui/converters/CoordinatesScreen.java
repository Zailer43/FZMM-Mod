package fzmm.zailer.me.client.gui.converters;

import fzmm.zailer.me.client.gui.widget.NumberFieldWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class CoordinatesScreen extends ConvertersBaseScreen {

	private NumberFieldWidget netherXNumberField,
		netherZNumberField,
		overworldXNumberField,
		overworldZNumberField;

	public CoordinatesScreen() {
		super(new TranslatableText("converters.coordinates"));
	}

	protected void init() {
		super.init();

		this.netherXNumberField = new NumberFieldWidget(this.textRenderer, this.width / 2 - 84, LINE1, 100, NORMAL_TEXT_FIELD_HEIGHT, new LiteralText("Nether X"), -30000000, 30000000);
		this.netherXNumberField.setMaxLength(9);
		this.netherXNumberField.setChangedListener(this::netherXListener);
		this.setInitialFocus(this.netherXNumberField);

		this.netherZNumberField = new NumberFieldWidget(this.textRenderer, this.width / 2 + 20, LINE1, 100, NORMAL_TEXT_FIELD_HEIGHT, new LiteralText("Nether Z"), -30000000, 30000000);
		this.netherZNumberField.setMaxLength(9);
		this.netherZNumberField.setChangedListener(this::netherZListener);

		this.overworldXNumberField = new NumberFieldWidget(this.textRenderer, this.width / 2 - 84, LINE2, 100, NORMAL_TEXT_FIELD_HEIGHT, new LiteralText("Overworld X"), -240000000, 240000000);
		this.overworldXNumberField.setMaxLength(10);
		this.overworldXNumberField.setChangedListener(this::overworldXListener);

		this.overworldZNumberField = new NumberFieldWidget(this.textRenderer, this.width / 2 + 20, LINE2, 100, NORMAL_TEXT_FIELD_HEIGHT, new LiteralText("Overworld Z"), -240000000, 240000000);
		this.overworldZNumberField.setMaxLength(10);
		this.overworldZNumberField.setChangedListener(this::overworldZListener);

		this.addSelectableChild(this.netherXNumberField);
		this.addSelectableChild(this.netherZNumberField);
		this.addSelectableChild(this.overworldXNumberField);
		this.addSelectableChild(this.overworldZNumberField);
	}

	public void resize(MinecraftClient client, int width, int height) {
		String netherX = this.netherXNumberField.getText(),
			netherZ = this.netherZNumberField.getText(),
			overworldX = this.overworldXNumberField.getText(),
			overworldZ = this.overworldZNumberField.getText();

		this.init(client, width, height);

		this.netherXNumberField.setText(netherX);
		this.netherZNumberField.setText(netherZ);
		this.overworldXNumberField.setText(overworldX);
		this.overworldZNumberField.setText(overworldZ);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		super.render(matrices, mouseX, mouseY, delta);

		drawCenteredText(matrices, this.textRenderer, new TranslatableText("coordinates.nether"), this.width / 2 - 120, LINE1 + 10, TEXT_COLOR);
		drawCenteredText(matrices, this.textRenderer, new LiteralText("X"), this.width / 2 - 34, LINE1 - 10, TEXT_COLOR);
		this.netherXNumberField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new LiteralText("Z"), this.width / 2 + 70, LINE1 - 10, TEXT_COLOR);
		this.netherZNumberField.render(matrices, mouseX, mouseY, delta);

		drawCenteredText(matrices, this.textRenderer, new TranslatableText("coordinates.overworld"), this.width / 2 - 120, LINE2 + 10, TEXT_COLOR);
		drawCenteredText(matrices, this.textRenderer, new LiteralText("X"), this.width / 2 - 34, LINE2 - 10, TEXT_COLOR);
		this.overworldXNumberField.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, new LiteralText("Z"), this.width / 2 + 70, LINE2 - 10, TEXT_COLOR);
		this.overworldZNumberField.render(matrices, mouseX, mouseY, delta);
	}

	public static void changeCoordinates(NumberFieldWidget currentDimension, NumberFieldWidget oppositeDimension, boolean isNether) {
		int currentCoordinate = currentDimension.getNumber(),
			oppositeOldCoordinate = oppositeDimension.getNumber(),
			oppositeCoordinate = isNether ? currentCoordinate * 8 : currentCoordinate / 8;

		if ((isNether && oppositeOldCoordinate / 8 != currentCoordinate) || (!isNether && oppositeOldCoordinate != oppositeCoordinate)) {

			oppositeDimension.setText(String.valueOf(oppositeCoordinate));
		}

	}

	public void netherXListener(String text) {
		changeCoordinates(this.netherXNumberField, this.overworldXNumberField, true);
	}

	public void netherZListener(String text) {
		changeCoordinates(this.netherZNumberField, this.overworldZNumberField, true);
	}

	public void overworldXListener(String text) {
		changeCoordinates(this.overworldXNumberField, this.netherXNumberField, false);
	}

	public void overworldZListener(String text) {
		changeCoordinates(this.overworldZNumberField, this.netherZNumberField, false);
	}
}
