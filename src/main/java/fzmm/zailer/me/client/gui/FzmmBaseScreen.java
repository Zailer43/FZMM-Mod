package fzmm.zailer.me.client.gui;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import static fzmm.zailer.me.client.gui.ScreenConstants.TEXT_COLOR;

public class FzmmBaseScreen extends Screen {

	protected ButtonWidget backButton;
	protected Text title;

	protected FzmmBaseScreen(Text title)  {
		super(NarratorManager.EMPTY);
		this.title = title;
	}

	protected void init() {
		assert this.client != null;

		this.backButton = this.addDrawableChild(new ButtonWidget(this.width - 120, this.height - 40, 100, 20, ScreenTexts.BACK,
			(buttonWidget) -> this.client.openScreen(new FzmmMainScreen())
		));
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, TEXT_COLOR);
		super.render(matrices, mouseX, mouseY, delta);
	}
}
