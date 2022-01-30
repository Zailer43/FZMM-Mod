package fzmm.zailer.me.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.ArrayList;

import static fzmm.zailer.me.client.gui.ScreenConstants.TEXT_COLOR;

public abstract class AbstractFzmmScreen extends Screen {

	protected ButtonWidget backButton;
	public static ArrayList<Screen> previousScreen = new ArrayList<>();

	public AbstractFzmmScreen(Text title)  {
		super(NarratorManager.EMPTY);
//		this.setTitle(title.getString());
	}

	public void init() {
		assert this.client != null;

		this.backButton = this.addDrawableChild(new ButtonWidget(this.width - 120, this.height - 40, 100, 20, ScreenTexts.BACK,
			(buttonWidget) -> previousScreen()
		));
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 20, TEXT_COLOR);
		super.render(matrices, mouseX, mouseY, delta);
	}

	public static void previousScreen() {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc.currentScreen instanceof AbstractFzmmScreen) {
			int size = previousScreen.size();
			previousScreen.remove(--size);
			mc.setScreen(size <= 0 ? null : previousScreen.get(size - 1));
		}
	}
}
