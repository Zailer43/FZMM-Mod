package fzmm.zailer.me.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class FzmmMainScreen extends Screen {
	protected ButtonWidget closeButton;
	protected ButtonWidget imagetextButton,
		encodebookButton,
		gradientButton,
		convertersButton,
		itemsButton;

	protected FzmmMainScreen() {
		super(NarratorManager.EMPTY);
	}

	protected void init() {
		assert this.client != null;
		this.closeButton = this.addButton(new ButtonWidget(this.width - 120, this.height - 40, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new LiteralText("Salir"),
			(buttonWidget) -> this.client.openScreen(null)
		));
		this.imagetextButton = this.addButton(new ButtonWidget(this.width / 2 - 154, LINE1, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new LiteralText("Imagetext"),
			(buttonWidget) -> this.client.openScreen(new ImagetextScreen())
		));
		this.encodebookButton = this.addButton(new ButtonWidget(this.width / 2 - 50, LINE1, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new LiteralText("Encodebook"),
			(buttonWidget) -> this.client.openScreen(new EncodebookScreen())
		));
		this.gradientButton = this.addButton(new ButtonWidget(this.width / 2 + 54, LINE1, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new LiteralText("Gradient"),
			(buttonWidget) -> this.client.openScreen(new GradientScreen())
		));
		this.convertersButton = this.addButton(new ButtonWidget(this.width / 2 - 154, LINE2, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new LiteralText("Conversores (Pronto)"), null/*
			(buttonWidget) -> this.client.openScreen(new ConvertersScreen())*/
		));
		this.convertersButton.active = false;
		this.itemsButton = this.addButton(new ButtonWidget(this.width / 2 - 50, LINE2, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new LiteralText("Items (Pronto)"), null/*
			(buttonWidget) -> this.client.openScreen(new ItemsScreen(this.client.player.getMainHandStack()))*/
		));
		this.itemsButton.active = false;
	}

	public void resize(MinecraftClient client, int width, int height) {
		this.init(client, width, height);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
	}
}
