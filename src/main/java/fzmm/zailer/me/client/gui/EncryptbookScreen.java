//package fzmm.zailer.me.client.gui;
//
//import fi.dy.masa.malilib.config.IConfigBase;
//import fi.dy.masa.malilib.config.options.ConfigInteger;
//import fi.dy.masa.malilib.config.options.ConfigString;
//import fi.dy.masa.malilib.gui.GuiBase;
//import fi.dy.masa.malilib.gui.button.ButtonBase;
//import fi.dy.masa.malilib.gui.button.ButtonGeneric;
//import fi.dy.masa.malilib.gui.button.IButtonActionListener;
//import fzmm.zailer.me.client.gui.enums.Buttons;
//import fzmm.zailer.me.client.gui.interfaces.IScreenTab;
//import fzmm.zailer.me.client.gui.wrapper.OptionWrapper;
//import fzmm.zailer.me.client.logic.EncryptbookLogic;
//import fzmm.zailer.me.config.FzmmConfigModel;
//import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.gui.screen.ConfirmLinkScreen;
//import net.minecraft.client.gui.screen.Screen;
//import net.minecraft.util.Util;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class EncryptbookScreen extends GuiOptionsBase {
//
//	private static final ConfigInteger seed = new ConfigInteger("seed", 0, 0, 0xFFFFFF, "");
//	private final ConfigString detailsId;
//    private final ConfigString author;
//    private final ConfigString paddingChars;
//    private final ConfigString bookTitle;
//    private final ConfigInteger maxMsgLength;
//
//	public EncryptbookScreen(Screen parent) {
//		super("encryptbook", parent);
//		MinecraftClient client = MinecraftClient.getInstance();
//        assert client.player != null;
//
//		seed.setComment(this.commentBase + "seed");
//        this.detailsId = new ConfigString("detailsId", FzmmConfigModel.Encryptbook.DEFAULT_BOOK_MESSAGE.getStringValue(), this.commentBase + "detailsId");
//        this.author = new ConfigString("author", client.player.getName().getString(), this.commentBase + "author");
//        this.paddingChars = new ConfigString("paddingCharacters", FzmmConfigModel.Encryptbook.PADDING.getStringValue(), this.commentBase + "paddingCharacters");
//        this.bookTitle = new ConfigString("bookTitle", FzmmConfigModel.Encryptbook.DEFAULT_BOOK_TITLE.getStringValue(), this.commentBase + "bookTitle");
//        this.maxMsgLength = new ConfigInteger("maxMessageLength", FzmmConfigModel.Encryptbook.MESSAGE_MAX_LENGTH.getIntegerValue(), 0, 0x1ff, this.commentBase + "maxMessageLength");
//	}
//
//    @Override
//	public void initGui() {
//		super.initGui();
//
//		int x = 20;
//		int y = this.height - 40;
//
//		x += this.createButton(x, y, Buttons.GIVE);
//		this.createButton(x, y, Buttons.ENCRYPTBOOK_GET_DECODER);
//
//		ButtonGeneric faqButton = Buttons.FAQ.getToLeft(this.width - 20, 20);
//		this.addButton(faqButton, new FaqButtonListener());
//	}
//
//	private int createButton(int x, int y, Buttons button) {
//		ButtonGeneric buttonGeneric = button.get(x, y);
//		this.addButton(buttonGeneric, new ButtonActionListener(button, this));
//
//		return buttonGeneric.getWidth() + 2;
//	}
//
//	@Override
//	public List<ConfigOptionWrapper> getConfigs() {
//		List<IConfigBase> options = new ArrayList<>();
//
//		options.add(this.detailsId);
//		options.add(seed);
//		options.add(this.author);
//		options.add(this.paddingChars);
//		options.add(this.maxMsgLength);
//		options.add(this.bookTitle);
//
//		return OptionWrapper.createFor(options);
//	}
//
//	@Override
//	public boolean isTab(IScreenTab tab) {
//		return false;
//	}
//
//	private record ButtonActionListener(Buttons button, EncryptbookScreen parent) implements IButtonActionListener {
//
//		@Override
//		public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
//
//			int seed = EncryptbookScreen.seed.getIntegerValue();
//			int maxMsgLength = this.parent.maxMsgLength.getIntegerValue();
//
//			switch (this.button) {
//				case GIVE -> {
//					String detailsId = this.parent.detailsId.getStringValue();
//					if (detailsId.isEmpty())
//						detailsId = this.parent.detailsId.getDefaultStringValue();
//
//					String paddingChars = this.parent.paddingChars.getStringValue();
//					if (paddingChars.isEmpty())
//						paddingChars = this.parent.paddingChars.getDefaultStringValue();
//
//					String author = this.parent.author.getStringValue();
//					String title = this.parent.bookTitle.getStringValue();
//
//					EncryptbookLogic.give(seed, detailsId, author, paddingChars, maxMsgLength, title);
//				}
//				case ENCRYPTBOOK_GET_DECODER -> EncryptbookLogic.showDecryptorInChat(seed, maxMsgLength);
//			}
//		}
//	}
//
//	private class FaqButtonListener implements IButtonActionListener, BooleanConsumer {
//		private static final String FAQ_URL = "https://github.com/Zailer43/FZMM-Mod/wiki/FAQ-Encryptbook";
//
//		@Override
//		public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
//			GuiBase.openGui(new ConfirmLinkScreen(this, FAQ_URL, true));
//		}
//
//		@Override
//		public void accept(boolean bl) {
//			if (bl) {
//				Util.getOperatingSystem().open(FAQ_URL);
//			}
//
//			GuiBase.openGui(EncryptbookScreen.this);
//		}
//	}
//}
