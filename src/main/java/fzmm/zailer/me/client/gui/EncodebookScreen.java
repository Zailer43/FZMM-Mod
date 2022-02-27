package fzmm.zailer.me.client.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fzmm.zailer.me.client.gui.enums.Buttons;
import fzmm.zailer.me.client.gui.interfaces.IScreenTab;
import fzmm.zailer.me.client.gui.wrapper.OptionWrapper;
import fzmm.zailer.me.client.guiLogic.EncodebookLogic;
import fzmm.zailer.me.config.Configs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.util.ArrayList;
import java.util.List;

public class EncodebookScreen extends GuiOptionsBase {

	private static final ConfigInteger seed = new ConfigInteger("seed", 0, 0, 0xFFFFFF, "");
	private final ConfigString message;
    private final ConfigString author;
    private final ConfigString paddingChars;
    private final ConfigString bookTitle;
    private final ConfigInteger maxMsgLength;

	public EncodebookScreen(Screen parent) {
		super("fzmm.gui.title.encodebook", parent);
		MinecraftClient client = MinecraftClient.getInstance();
        assert client.player != null;

        this.message = new ConfigString("message", Configs.Encodebook.DEFAULT_BOOK_MESSAGE.getStringValue(), "");
        this.author = new ConfigString("author", client.player.getName().getString(), "");
        this.paddingChars = new ConfigString("paddingCharacters", Configs.Encodebook.PADDING.getStringValue(), "");
        this.bookTitle = new ConfigString("bookTitle", Configs.Encodebook.DEFAULT_BOOK_TITLE.getStringValue(), "");
        this.maxMsgLength = new ConfigInteger("maxMessageLength", Configs.Encodebook.MESSAGE_MAX_LENGTH.getIntegerValue(), 0, 0x1ff, "");
	}

    @Override
	public void initGui() {
		super.initGui();

		int x = 20;
		int y = this.height - 40;

		x += this.createButton(x, y, Buttons.GIVE);
		this.createButton(x, y, Buttons.ENCODEBOOK_GET_DECODER);
	}

	private int createButton(int x, int y, Buttons button) {
		ButtonGeneric buttonGeneric = button.get(x, y);
		this.addButton(buttonGeneric, new ButtonActionListener(button, this));

		return buttonGeneric.getWidth() + 2;
	}

	@Override
	public List<OptionWrapper> getOptions() {
		List<IConfigBase> options = new ArrayList<>();

		options.add(this.message);
		options.add(seed);
		options.add(this.author);
		options.add(this.paddingChars);
		options.add(this.maxMsgLength);
		options.add(this.bookTitle);

		return OptionWrapper.createFor(options);
	}

	@Override
	public boolean isTab(IScreenTab tab) {
		return false;
	}

	private record ButtonActionListener(Buttons button, EncodebookScreen parent) implements IButtonActionListener {

		@Override
		public void actionPerformedWithButton(ButtonBase button, int mouseButton) {

			int seed = EncodebookScreen.seed.getIntegerValue();
			int maxMsgLength = this.parent.maxMsgLength.getIntegerValue();

			switch (this.button) {
				case GIVE -> {
					String message = this.parent.message.getStringValue();
					if (message.isEmpty())
						message = this.parent.message.getDefaultStringValue();

					String paddingChars = this.parent.paddingChars.getStringValue();
					if (paddingChars.isEmpty())
						paddingChars = this.parent.paddingChars.getDefaultStringValue();

					String author = this.parent.author.getStringValue();
					String title = this.parent.bookTitle.getStringValue();

					EncodebookLogic.EncodeBook(seed, message, author, paddingChars, maxMsgLength, title);
				}
				case ENCODEBOOK_GET_DECODER -> EncodebookLogic.showDecoderInChat(seed, maxMsgLength);
			}
		}
	}
}
