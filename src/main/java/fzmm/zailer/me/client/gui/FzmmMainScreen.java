package fzmm.zailer.me.client.gui;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fzmm.zailer.me.client.gui.enums.Buttons;
import fzmm.zailer.me.client.gui.headgenerator.HeadGeneratorScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class FzmmMainScreen extends GuiBase {

	public FzmmMainScreen(Screen parent) {
		super();
		this.setParent(parent);
		this.setTitle(Text.translatable("fzmm.gui.title.main").getString());
	}

	@Override
	public void initGui() {
		super.initGui();
		// the width of each column is obtained to prevent translations from being left out of the buttons
		List<Buttons> buttonColumn1 = List.of(Buttons.MAIN_IMAGETEXT, Buttons.MAIN_ENCRYPTBOOK);
		List<Buttons> buttonColumn2 = List.of(Buttons.MAIN_GRADIENT, Buttons.MAIN_HEAD_GENERATOR);
		List<Buttons> buttonColumn3 = List.of(Buttons.MAIN_PLAYER_STATUE, Buttons.MAIN_CONVERTERS);

		int column1Width = this.getMaxWidth(buttonColumn1);
		int column2Width = this.getMaxWidth(buttonColumn2);
		int column3Width = this.getMaxWidth(buttonColumn3);
		int halfColumn2Width = column2Width / 2;
		int halfWidth = this.width / 2;

		int col1 = halfWidth - halfColumn2Width - column1Width - 4;
		int col2 = halfWidth - halfColumn2Width;
		int col3 = halfWidth + halfColumn2Width + 4;

		this.createGenericButton(this.width - this.textRenderer.getWidth(Buttons.MAIN_CONFIGURATION.getText()) - 20, 20, Buttons.MAIN_CONFIGURATION, -1);
		this.addColumn(buttonColumn1, col1, column1Width);
		this.addColumn(buttonColumn2, col2, column2Width);
		this.addColumn(buttonColumn3, col3, column3Width);

		this.addButton(Buttons.BACK.getToLeft(this.width - 30, this.height - 40),
				(button, mouseButton) -> GuiBase.openGui(this.getParent()));
	}

	private void addColumn(List<Buttons> buttons, int x, int width) {
		int y = LINE1;
		for (var button : buttons) {
			this.createGenericButton(x, y, button, width);
			y += 40;
		}
	}

	private void createGenericButton(int x, int y, Buttons button, int width) {
		ButtonGeneric buttonGeneric = button.get(x, y, width);
		this.addButton(buttonGeneric, this.getActionListener(button));
	}

	public int getMaxWidth(List<Buttons> buttons) {
		int max = 0;

		for (var button : buttons)
			max = Math.max(this.textRenderer.getWidth(button.getText()), max);

		return max + 8;
	}

	protected IButtonActionListener getActionListener(Buttons button) {
		return new ButtonActionListener(button);
	}

	private class ButtonActionListener implements IButtonActionListener {
		private final Buttons button;

		private ButtonActionListener(Buttons button) {
			this.button = button;
		}

		@Override
		public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
			Screen parent = FzmmMainScreen.this;
			Screen guiBase = switch (this.button) {
				case MAIN_CONFIGURATION -> new ConfigScreen(parent);
				case MAIN_CONVERTERS -> new ConvertersScreen(parent);
				case MAIN_ENCRYPTBOOK -> new EncryptbookScreen(parent);
				case MAIN_GRADIENT -> new GradientScreen(parent);
				case MAIN_IMAGETEXT -> new ImagetextScreen(parent);
//				case MAIN_ITEMS_EDITOR -> null;
				case MAIN_PLAYER_STATUE -> new PlayerStatueScreen(parent);
				case MAIN_HEAD_GENERATOR -> new HeadGeneratorScreen();
				default -> null;
			};

			if (guiBase != null)
				openGui(guiBase);
		}

	}

}
