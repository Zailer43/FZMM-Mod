package fzmm.zailer.me.client.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.util.StringUtils;
import fzmm.zailer.me.client.gui.enums.Buttons;
import fzmm.zailer.me.client.gui.interfaces.IScreenTab;
import fzmm.zailer.me.client.gui.interfaces.ITabListener;
import fzmm.zailer.me.client.gui.wrapper.OptionWrapper;
import net.minecraft.client.Keyboard;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import java.nio.charset.StandardCharsets;
import java.util.*;

public class ConvertersScreen extends GuiOptionsBase {

	private static ConvertersGuiTab tab = ConvertersGuiTab.BASE64;
	private final ConfigString base64Message;
	private final ConfigString uuidValue;
	private final ConfigInteger[] arrayValues;
	//private final ConfigString arrayString;

	public ConvertersScreen(Screen parent) {
		super("fzmm.gui.title.converters", parent);

		this.base64Message = new ConfigString("message", "", "");
		this.uuidValue = new ConfigString("uuid", "", "");
		//this.arrayString = new ConfigString("arrayString", "", "");
		this.arrayValues = new ConfigInteger[4];
		for (int i = 0; i != 4; i++)
			this.arrayValues[i] = new ConfigInteger("array[" + i + "]", 0, Integer.MIN_VALUE, Integer.MAX_VALUE, "");
	}

	@Override
	public void initGui() {
		super.initGui();

		this.createTabs(ConvertersGuiTab.values(), new TabButtonListener(this));
	}

	private ButtonGeneric createButton(int x, int y, Buttons button) {
		ButtonGeneric buttonGeneric = button.get(x, y);
		this.addButton(buttonGeneric, new ButtonActionListener(button, this));

		return buttonGeneric;
	}

	@Override
	public List<OptionWrapper> getOptions() {
		List<IConfigBase> options = new ArrayList<>();
		int x = 20;
		int y = this.height - 40;

		switch (tab) {
			case BASE64 -> {
				options.add(this.base64Message);
				ButtonGeneric base64Decode = this.createButton(x, y, Buttons.CONVERTERS_COPY_DECODED);
				x += base64Decode.getWidth() + 2;
				this.createButton(x, y, Buttons.CONVERTERS_COPY_ENCODED);
			}
			case UUID_TO_ARRAY -> {
				options.add(this.uuidValue);
				ButtonGeneric uuidToArrayCopyButton = this.createButton(x, y, Buttons.CONVERTERS_COPY_ARRAY);
				x += uuidToArrayCopyButton.getWidth() + 2;
				this.createButton(x, y, Buttons.RANDOM);
			}
			case ARRAY_TO_UUID -> {
				options.addAll(Arrays.asList(this.arrayValues));
				//options.add(this.arrayString);
				ButtonGeneric arrayToUuidCopyButton = this.createButton(x, y, Buttons.CONVERTERS_COPY_UUID);
				x += arrayToUuidCopyButton.getWidth() + 2;
				this.createButton(x, y, Buttons.RANDOM);
			}
		}

		return OptionWrapper.createFor(options);
	}

	@Override
	public boolean isTab(IScreenTab tab) {
		return ConvertersScreen.tab == tab;
	}

	private static class TabButtonListener implements ITabListener {
		private final IScreenTab tab;
		private final ConvertersScreen parent;

		private TabButtonListener(IScreenTab tab, ConvertersScreen parent) {
			this.tab = tab;
			this.parent = parent;
		}

		private TabButtonListener(ConvertersScreen gui) {
			this(null, gui);
		}

		@Override
		public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
			if (this.tab == null)
				return;

			ConvertersScreen.tab = (ConvertersGuiTab) this.tab;

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

	private enum ConvertersGuiTab implements IScreenTab {
		BASE64("base64"),
		UUID_TO_ARRAY("uuidToArray"),
		ARRAY_TO_UUID("arrayToUuid");

		static final String BASE_KEY = "fzmm.gui.converters.";

		private final String translationKey;

		ConvertersGuiTab(String translationKey) {
			this.translationKey = BASE_KEY + translationKey;
		}

		public String getDisplayName() {
			return StringUtils.translate(this.translationKey);
		}
	}

	private record ButtonActionListener(Buttons button, ConvertersScreen parent) implements IButtonActionListener {

		@Override
		public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
			Keyboard keyboard = MinecraftClient.getInstance().keyboard;
			switch (this.button) {
				case CONVERTERS_COPY_DECODED -> {
					try {
						byte[] decodedValue = Base64.getDecoder().decode(this.parent.base64Message.getStringValue());
						String decodedMessage = new String(decodedValue, StandardCharsets.UTF_8);
						keyboard.setClipboard(decodedMessage);
					} catch(Exception ignored) {
					}
				}
				case CONVERTERS_COPY_ENCODED -> {
					try {
						byte[] messageByte = this.parent.base64Message.getStringValue().getBytes(StandardCharsets.UTF_8);
						String encodedMessage = Base64.getEncoder().encodeToString(messageByte);
						keyboard.setClipboard(encodedMessage);
					} catch(Exception ignored) {
					}
				}
				case CONVERTERS_COPY_ARRAY -> {
					String uuidString = this.parent.uuidValue.getStringValue().replaceAll("[^0-9a-f-]", "");
					int length = uuidString.length();
					if (!(length == 32 || length == 36)) {
						return;
					}

					try {
						int[] intArr = this.UUIDtoArray(UUID.fromString(uuidString));
						String arrayString = "[I; " + intArr[0] + ", " + intArr[1] + ", " + intArr[2] + ", " + intArr[3] + "]";

						keyboard.setClipboard(arrayString);

					} catch (Exception ignored) {
					}

				}
				case CONVERTERS_COPY_UUID -> {
					int[] intArray = new int[4];
					for (int i = 0; i != 4; i++)
						intArray[i] = this.parent.arrayValues[i].getIntegerValue();

					long msb = Integer.toUnsignedLong(intArray[0]);
					long lsb = Integer.toUnsignedLong(intArray[2]);
					msb = (msb << 32) | Integer.toUnsignedLong(intArray[1]);
					lsb = (lsb << 32) | Integer.toUnsignedLong(intArray[3]);

					keyboard.setClipboard(new UUID(msb, lsb).toString());
				}
				case RANDOM -> {
					UUID uuid = UUID.randomUUID();
					if (this.parent.isTab(ConvertersGuiTab.UUID_TO_ARRAY)) {
						this.parent.uuidValue.setValueFromString(uuid.toString());
					} else {
						int[] intArray = this.UUIDtoArray(uuid);

						for(int i = 0; i != 4; i++) {
							this.parent.arrayValues[i].setIntegerValue(intArray[i]);
						}
					}
					this.parent.reload();
				}
			}
		}

		private int[] UUIDtoArray(UUID uuid) {
			long msb = uuid.getMostSignificantBits();
			long lsb = uuid.getLeastSignificantBits();
			int[] intArray = new int[4];

			intArray[0] = (int) (msb >> 32);
			intArray[1] = (int) (msb);
			intArray[2] = (int) (lsb >> 32);
			intArray[3] = (int) (lsb);

			return intArray;
		}
	}

}
