package fzmm.zailer.me.client.gui.converters;

import fzmm.zailer.me.client.gui.ConvertersScreen;
import fzmm.zailer.me.client.gui.FzmmBaseScreen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class ConvertersBaseScreen extends FzmmBaseScreen {
	protected ConvertersBaseScreen(Text title) {
		super(title);
	}

	protected void init() {
		assert this.client != null;

		this.backButton = this.addDrawableChild(new ButtonWidget(this.width - 120, this.height - 40, 100, 20, ScreenTexts.BACK,
			(buttonWidget) -> this.client.setScreen(new ConvertersScreen())
		));
	}
}
