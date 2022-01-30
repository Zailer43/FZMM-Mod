package fzmm.zailer.me.client.gui;

import fzmm.zailer.me.client.gui.playerStatue.StatueScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class FzmmMainScreen extends AbstractFzmmScreen {

	public FzmmMainScreen() {
		super(new TranslatableText("fzmm.gui.title.fzmm"));
	}

	public void init() {
		super.init();
		assert this.client != null;
		assert this.client.player != null;
		int col1 = this.width / 2 - 154,
			col2 = this.width / 2 - 50,
			col3 = this.width / 2 + 54;

		this.backButton.setMessage(new TranslatableText("gui.leave"));

		this.addDrawableChild(new ButtonWidget(col1, LINE1, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("imagetext.title"),
				(buttonWidget) -> this.client.setScreen(new ImagetextScreen())
		));
		this.addDrawableChild(new ButtonWidget(col2, LINE1, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("encodebook.title"),
				(buttonWidget) -> this.client.setScreen(new EncodebookScreen())
		));
		this.addDrawableChild(new ButtonWidget(col3, LINE1, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("gradient.title"),
				(buttonWidget) -> this.client.setScreen(new GradientScreen())
		));

		this.addDrawableChild(new ButtonWidget(col1, LINE2, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("converters.title"),
				(buttonWidget) -> this.client.setScreen(new ConvertersScreen())
		));
		ButtonWidget itemsButton = this.addDrawableChild(new ButtonWidget(col2, LINE2, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new LiteralText("Items (Soon)"), null/*
			(buttonWidget) -> this.client.setScreen(new ItemsScreen(stack))*/
		));
		itemsButton.active = false;
		this.addDrawableChild(new ButtonWidget(col3, LINE2, NORMAL_BUTTON_WIDTH, NORMAL_BUTTON_HEIGHT, new TranslatableText("playerStatue.title"),
				(buttonWidget) -> this.client.setScreen(new StatueScreen())
		));

	}

}
