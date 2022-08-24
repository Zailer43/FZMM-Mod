package fzmm.zailer.me.client.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigColor;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.Color4f;
import fi.dy.masa.malilib.util.StringUtils;
import fzmm.zailer.me.client.gui.enums.Buttons;
import fzmm.zailer.me.client.gui.interfaces.IScreenTab;
import fzmm.zailer.me.client.gui.interfaces.ITabListener;
import fzmm.zailer.me.client.gui.wrapper.OptionWrapper;
import fzmm.zailer.me.client.logic.GradientLogic;
import fzmm.zailer.me.config.Configs;
import fzmm.zailer.me.utils.DisplayUtils;
import fzmm.zailer.me.utils.FzmmUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class GradientScreen extends GuiOptionsBase {

	public static final int MAX_MESSAGE_LENGTH = 0xFF;
	private static GradientGuiTab tab = GradientGuiTab.TWO_COLORS;
	private final ConfigString message;
	private final ConfigBoolean obfuscated;
	private final ConfigBoolean bold;
	private final ConfigBoolean strikethrough;
	private final ConfigBoolean underline;
	private final ConfigBoolean italic;
	private final ConfigColor initialColor;
	private final ConfigColor finalColor;
	private final ConfigDouble hue;
	private final ConfigDouble saturation;
	private final ConfigDouble brightness;
	private final ConfigDouble hueStep;
	private ButtonGeneric addLoreButton;
	private ButtonGeneric setNameButton;
	private ButtonGeneric copyButton;

	public GradientScreen(Screen parent) {
		super("gradient", parent);

		this.message = new ConfigString("message", "Hello world", this.commentBase + "message");
		this.obfuscated = new ConfigBoolean("obfuscated", false, this.commentBase + "obfuscated");
		this.bold = new ConfigBoolean("bold", false, this.commentBase + "bold");
		this.strikethrough = new ConfigBoolean("strikethrough", false, this.commentBase + "strikethrough");
		this.underline = new ConfigBoolean("underline", false, this.commentBase + "underline");
		this.italic = new ConfigBoolean("italic", false, this.commentBase + "italic");

		this.initialColor = new ConfigColor("initialColor", "0xFFFF0000", this.commentBase + "initialColor");
		this.finalColor = new ConfigColor("finalColor", "0xFF0000FF", this.commentBase + "finalColor");

		this.hue = new ConfigDouble("hue", 1f, 0f, 1f, this.commentBase + "hue");
		this.hue.toggleUseSlider();
		this.saturation = new ConfigDouble("saturation", 1f, 0f, 1f, this.commentBase + "saturation");
		this.saturation.toggleUseSlider();
		this.brightness = new ConfigDouble("brightness", 0.8f, 0f, 1f, this.commentBase + "brightness");
		this.brightness.toggleUseSlider();
		this.hueStep = new ConfigDouble("hueStep", 0.05f, 0.005f, 0.15f, this.commentBase + "hueStep");
		this.hueStep.toggleUseSlider();
	}

	@Override
	public void initGui() {
		super.initGui();

		this.createTabs(GradientGuiTab.values(), new TabButtonListener(this));

		int x = 20;
		int y = this.height - 40;

		x += this.createButton(x, y, Buttons.GRADIENT_RANDOM_COLOR).getWidth() + 2;
		this.addLoreButton = this.createButton(x, y, Buttons.GRADIENT_ADD_LORE);
		x += this.addLoreButton.getWidth() + 2;
		this.setNameButton = this.createButton(x, y, Buttons.GRADIENT_SET_NAME);
		x += this.setNameButton.getWidth() + 2;
		this.copyButton = this.createButton(x, y, Buttons.GRADIENT_COPY);

	}

	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		Text preview = this.getGradient();
		int x = this.width / 2 - this.textRenderer.getWidth(preview) / 2;
		this.textRenderer.draw(matrixStack, preview, x, 26, ScreenConstants.TEXT_COLOR);
	}

	private ButtonGeneric createButton(int x, int y, Buttons button) {
		ButtonGeneric buttonGeneric = button.get(x, y);
		this.addButton(buttonGeneric, new ButtonActionListener(button, this));

		return buttonGeneric;
	}

	public void toggleExecuteButtons(boolean bl) {
		this.addLoreButton.setEnabled(bl);
		this.copyButton.setEnabled(bl);
		this.setNameButton.setEnabled(bl);
	}

	@Override
	public List<ConfigOptionWrapper> getConfigs() {
		List<IConfigBase> options = new ArrayList<>();

		options.add(this.message);
		options.add(this.obfuscated);
		options.add(this.bold);
		options.add(this.strikethrough);
		options.add(this.underline);
		options.add(this.italic);

		List<ConfigOptionWrapper> optionsWrapper = OptionWrapper.createFor(options);
		this.addTabOptions(optionsWrapper);

		return optionsWrapper;
	}

	@Override
	public boolean isTab(IScreenTab tab) {
		return GradientScreen.tab == tab;
	}

	private void addTabOptions(List<ConfigOptionWrapper> list) {
		List<IConfigBase> options = new ArrayList<>();

		switch (tab) {
			case TWO_COLORS -> {
				options.add(this.initialColor);
				options.add(this.finalColor);
			}
			case HSB -> {
				options.add(this.hue);
				options.add(this.saturation);
				options.add(this.brightness);
				options.add(this.hueStep);
			}
		}

		list.addAll(OptionWrapper.createFor(options));
	}

	private static class TabButtonListener implements ITabListener {
		private final IScreenTab tab;
		private final GradientScreen parent;

		private TabButtonListener(IScreenTab tab, GradientScreen parent) {
			this.tab = tab;
			this.parent = parent;
		}

		private TabButtonListener(GradientScreen gui) {
			this(null, gui);
		}

		@Override
		public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
			if (this.tab == null)
				return;

			GradientScreen.tab = (GradientGuiTab) this.tab;

			this.parent.reload();
		}

		@Override
		public ITabListener of(IScreenTab tab) {
			return new TabButtonListener(tab, this.parent);
		}

		@Override
		public GuiOptionsBase getParent() {
			return this.parent;
		}
	}

	private enum GradientGuiTab implements IScreenTab {
		TWO_COLORS("twoColors"),
		HSB("hsb");

		static final String BASE_KEY = "fzmm.gui.gradient.";

		private final String translationKey;

		GradientGuiTab(String translationKey) {
			this.translationKey = BASE_KEY + translationKey;
		}

		public String getDisplayName() {
			return StringUtils.translate(this.translationKey);
		}
	}

	private Text getGradient() {
		String message = this.message.getStringValue();
		if (message.length() < 2) {
			this.toggleExecuteButtons(false);
			return Text.translatable("fzmm.gui.gradient.error.messageLength").setStyle(Style.EMPTY.withColor(ScreenConstants.TEXT_ERROR_COLOR));
		}
		this.toggleExecuteButtons(true);

		boolean obfuscated = this.obfuscated.getBooleanValue();
		boolean bold = this.bold.getBooleanValue();
		boolean strikethrough = this.strikethrough.getBooleanValue();
		boolean underline = this.underline.getBooleanValue();
		boolean italic = this.italic.getBooleanValue();
		GradientLogic gradient = new GradientLogic(message, obfuscated, bold, strikethrough, underline, italic);

		 if (this.isTab(GradientGuiTab.TWO_COLORS)) {
			Color4f initialColor = this.initialColor.getColor();
			Color4f finalColor = this.finalColor.getColor();

			return gradient.getGradient(initialColor, finalColor);
		 } else {
			 float hue = (float) this.hue.getDoubleValue();
			 float saturation = (float) this.saturation.getDoubleValue();
			 float brightness = (float) this.brightness.getDoubleValue();
			 float hueStep = (float) this.hueStep.getDoubleValue();

			 return gradient.getHsb(hue, saturation, brightness, hueStep);
		 }
	}

	private record ButtonActionListener(Buttons button, GradientScreen parent) implements IButtonActionListener {

		@Override
		public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
			Text gradient = this.parent.getGradient();
			MinecraftClient client = MinecraftClient.getInstance();
			assert client.player != null;
			boolean handItemIsEmpty = client.player.getInventory().getMainHandStack().isEmpty();

			switch (this.button) {
				case GRADIENT_RANDOM_COLOR -> {
					Random random = new Random(new Date().getTime());

					if (this.parent.isTab(GradientGuiTab.TWO_COLORS)) {
						int initialColor = random.nextInt(0xFFFFFF);
						int finalColor = random.nextInt(0xFFFFFF);

						this.parent.initialColor.setIntegerValue(initialColor);
						this.parent.finalColor.setIntegerValue(finalColor);
					} else {
						float hue = random.nextFloat();
						float saturation = random.nextFloat();
						float brightness = random.nextFloat();
						float hueStep = random.nextFloat(0.005f, 0.15f);

						this.parent.hue.setDoubleValue(hue);
						this.parent.saturation.setDoubleValue(saturation);
						this.parent.brightness.setDoubleValue(brightness);
						this.parent.hueStep.setDoubleValue(hueStep);
					}

					this.parent.reload();
				}
				case GRADIENT_ADD_LORE -> {
					if (handItemIsEmpty) {
						ItemStack stack = new DisplayUtils(Configs.getConfigItem(Configs.Generic.DEFAULT_GRADIENT_ITEM))
								.addLore(gradient)
								.get();
						FzmmUtils.giveItem(stack);
					} else {
						DisplayUtils.addLoreToHandItem(gradient);
					}
				}
				case GRADIENT_SET_NAME -> {
					if (handItemIsEmpty) {
						ItemStack stack = new DisplayUtils(Configs.getConfigItem(Configs.Generic.DEFAULT_GRADIENT_ITEM))
								.setName(gradient)
								.get();
						FzmmUtils.giveItem(stack);
					} else {
						FzmmUtils.renameHandItem(gradient);
					}
				}
				case GRADIENT_COPY -> client.keyboard.setClipboard(Text.Serializer.toJson(gradient));
			}
		}
	}

}
