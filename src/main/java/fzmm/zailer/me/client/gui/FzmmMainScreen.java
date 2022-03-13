package fzmm.zailer.me.client.gui;

import fi.dy.masa.malilib.gui.GuiBase;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fzmm.zailer.me.client.gui.enums.Buttons;
import net.minecraft.client.gui.screen.Screen;

import static fzmm.zailer.me.client.gui.ScreenConstants.*;

public class FzmmMainScreen extends AbstractFzmmScreen {

	public FzmmMainScreen(Screen parent) {
		super("fzmm.gui.title.main", parent);
	}

	@Override
	public void initGui() {
		super.initGui();
		int col1 = this.width / 2 - 154;
		int col2 = this.width / 2 - 50;
		int col3 = this.width / 2 + 54;

		this.createGenericButton(this.width - NORMAL_BUTTON_WIDTH - 20, 20, Buttons.MAIN_CONFIGURATION);
		this.createGenericButton(col1, LINE1, Buttons.MAIN_IMAGETEXT);
		this.createGenericButton(col2, LINE1, Buttons.MAIN_GRADIENT);
		this.createGenericButton(col3, LINE1, Buttons.MAIN_PLAYER_STATUE);
		this.createGenericButton(col1, LINE2, Buttons.MAIN_ENCRYPTBOOK);
		this.createGenericButton(col2, LINE2, Buttons.MAIN_ITEMS_EDITOR).setEnabled(false);
		this.createGenericButton(col3, LINE2, Buttons.MAIN_CONVERTERS);
	}

	@Override
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
			GuiBase guiBase = switch (this.button) {
				case MAIN_CONFIGURATION -> new ConfigScreen(parent);
				case MAIN_CONVERTERS -> new ConvertersScreen(parent);
				case MAIN_ENCRYPTBOOK -> new EncryptbookScreen(parent);
				case MAIN_GRADIENT -> new GradientScreen(parent);
				case MAIN_IMAGETEXT -> new ImagetextScreen(parent);
//				case MAIN_ITEMS_EDITOR -> null;
				case MAIN_PLAYER_STATUE -> new PlayerStatueScreen(parent);
				default -> null;
			};

			if (guiBase != null)
				openGui(guiBase);
		}

	}

}
