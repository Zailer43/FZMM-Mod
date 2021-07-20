/*package fzmm.zailer.me.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerServerListWidget;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import java.util.List;

import static fzmm.zailer.me.client.gui.ScreenConstants.TEXT_COLOR;

public class ItemOptionsListWidget extends AlwaysSelectedEntryListWidget<MultiplayerServerListWidget.Entry> {
	protected final List<ItemOptionsListWidget.Option> options = Lists.newArrayList();

	public ItemOptionsListWidget(MinecraftClient client, int width, int height, int top, int bottom, int entryHeight) {
		super(client, width, height, top, bottom, entryHeight);
		options.add(new Option(new LiteralText("test"), new FzmmMainScreen()));
		options.add(new Option(new LiteralText("Probando 1 2 3"), new FzmmMainScreen()));
		options.add(new Option(new LiteralText("Ola"), new FzmmMainScreen()));
		this.setRenderHorizontalShadows(false);
		this.options.forEach(this::addEntry);
	}

	private static class Option extends MultiplayerServerListWidget.Entry {
		Text message;
		Screen screen;

		public Option(Text message, Screen screen) {
			this.message = message;
			this.screen = screen;
		}

		public Text getMessage() {
			return message;
		}

		public Screen getScreen() {
			return screen;
		}

		@Override
		public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
			MinecraftClient.getInstance().textRenderer.draw(matrices, this.message, x + entryWidth / 2.0f, y + entryHeight / 2.0f, TEXT_COLOR);
		}

		@Override
		public Text method_37006() {
			return null;
		}
	}
}*/
