package fzmm.zailer.me.client.gui;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class FzmmMainScreen extends AbstractFzmmScreen {
	protected ButtonWidget imagetextButton,
		encodebookButton,
		gradientButton,
		convertersButton,
		itemsButton,
		statueButton,
		nbtButton;

	public FzmmMainScreen() {
		super(NarratorManager.EMPTY);
	}

	protected void init() {
		super.init();
		assert this.client != null;
		assert this.client.player != null;
		ItemStack stack = this.client.player.getMainHandStack();
		int col1 = this.width / 2 - 154,
			col2 = this.width / 2 - 50,
			col3 = this.width / 2 + 54;

		this.backButton.setMessage(new TranslatableText("gui.leave"));

		this.imagetextButton = this.addDrawableChild(new ButtonWidget(col1, LINE1, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.title"),
			(buttonWidget) -> this.client.setScreen(new ImagetextScreen())
		));
		this.encodebookButton = this.addDrawableChild(new ButtonWidget(col2, LINE1, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("encodebook.title"),
			(buttonWidget) -> this.client.setScreen(new EncodebookScreen())
		));
		this.gradientButton = this.addDrawableChild(new ButtonWidget(col3, LINE1, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("gradient.title"),
			(buttonWidget) -> this.client.setScreen(new GradientScreen())
		));

		this.convertersButton = this.addDrawableChild(new ButtonWidget(col1, LINE2, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("converters.title"),
			(buttonWidget) -> this.client.setScreen(new ConvertersScreen())
		));
		this.itemsButton = this.addDrawableChild(new ButtonWidget(col2, LINE2, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new LiteralText("Items (Pronto)"), null/*
			(buttonWidget) -> this.client.setScreen(new ItemsScreen(stack))*/
		));
		this.itemsButton.active = false;
		this.statueButton = this.addDrawableChild(new ButtonWidget(col3, LINE2, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("playerStatue.title"),
			(buttonWidget) -> this.client.setScreen(new StatueScreen())
		));

	public void resize(MinecraftClient client, int width, int height) {
		this.init(client, width, height);
	}

}
