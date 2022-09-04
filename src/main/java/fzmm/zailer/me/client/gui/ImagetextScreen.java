package fzmm.zailer.me.client.gui;

import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.config.options.ConfigOptionList;
import fi.dy.masa.malilib.config.options.ConfigString;
import fi.dy.masa.malilib.gui.Message;
import fi.dy.masa.malilib.gui.button.ButtonBase;
import fi.dy.masa.malilib.gui.button.ButtonGeneric;
import fi.dy.masa.malilib.gui.button.IButtonActionListener;
import fi.dy.masa.malilib.interfaces.IValueChangeCallback;
import fi.dy.masa.malilib.util.InfoUtils;
import fi.dy.masa.malilib.util.StringUtils;
import fzmm.zailer.me.client.gui.enums.Buttons;
import fzmm.zailer.me.client.gui.enums.options.BookOption;
import fzmm.zailer.me.client.gui.enums.options.ImageModeOption;
import fzmm.zailer.me.client.gui.enums.options.LoreOption;
import fzmm.zailer.me.client.gui.interfaces.IScreenTab;
import fzmm.zailer.me.client.gui.interfaces.ITabListener;
import fzmm.zailer.me.client.gui.options.ImageOption;
import fzmm.zailer.me.client.gui.wrapper.OptionWrapper;
import fzmm.zailer.me.client.logic.ImagetextLogic;
import fzmm.zailer.me.config.Configs;
import fzmm.zailer.me.exceptions.BookNbtOverflow;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec2f;
import net.minecraft.world.World;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ImagetextScreen extends GuiOptionsBase {
    private static final String DEFAULT_CHARACTER = "█";
    private static final int DEFAULT_SIZE_VALUE = 32;
    private static final int MAX_SIZE_VALUE = 127;
    private static final int MIN_SIZE_VALUE = 2;
    private static ImagetextGuiTab tab = ImagetextGuiTab.LORE;
    private final ImageOption configImage;
    private final ConfigInteger configWidth;
    private final ConfigInteger configHeight;
    private final ConfigString configCharacters;
    private final ConfigBoolean configSmoothImage;
    private final ConfigBoolean configShowResolution;
    private final ConfigOptionList configLoreOption;
    private final ConfigOptionList configBookOption;
    private final ConfigString configBookAuthor;
    private final ConfigString configBookMessage;
    private final ConfigInteger configPosX;
    private final ConfigInteger configPosY;
    private final ConfigInteger configPosZ;
    private final ImagetextLogic imagetextLogic;
    private ButtonGeneric previewButton;

    public ImagetextScreen(Screen parent) {
        super("imagetext", parent);

        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity player = mc.player;
        assert player != null;

        this.configImage = new ImageOption("image", "", ImageModeOption.URL, this.commentBase + "image");
        this.configWidth = new ConfigInteger("width", DEFAULT_SIZE_VALUE, MIN_SIZE_VALUE, MAX_SIZE_VALUE, this.commentBase + "resolution");
        this.configHeight = new ConfigInteger("height", DEFAULT_SIZE_VALUE, MIN_SIZE_VALUE, MAX_SIZE_VALUE, this.commentBase + "resolution");
        this.configCharacters = new ConfigString("characters", DEFAULT_CHARACTER, this.commentBase + "characters");
        this.configSmoothImage = new ConfigBoolean("smoothImage", true, this.commentBase + "smoothImage");
        this.configShowResolution = new ConfigBoolean("showResolution", false, this.commentBase + "showResolution");
        this.configLoreOption = new ConfigOptionList("loreMode", LoreOption.ADD, this.commentBase + "loreMode");
        this.configBookOption = new ConfigOptionList("bookMode", BookOption.ADD_PAGE, this.commentBase + "bookMode");
        this.configBookAuthor = new ConfigString("author", player.getName().getString(), this.commentBase + "bookAuthor");
        this.configBookMessage = new ConfigString("message", Configs.Generic.DEFAULT_IMAGETEXT_BOOK_MESSAGE.getStringValue(), this.commentBase + "bookMessage");
        this.configPosX = new ConfigInteger("x", player.getBlockX(), -World.HORIZONTAL_LIMIT, World.HORIZONTAL_LIMIT, this.commentBase + "hologramCoordinates");
        this.configPosY = new ConfigInteger("y", player.getBlockY(), -0xffff, 0xffff, this.commentBase + "hologramCoordinates");
        this.configPosZ = new ConfigInteger("z", player.getBlockZ(), -World.HORIZONTAL_LIMIT, World.HORIZONTAL_LIMIT, this.commentBase + "hologramCoordinates");

        if (Configs.Generic.PRESERVE_IMAGE_ASPECT_RATIO_IN_IMAGETEXT.getBooleanValue()) {
            this.configWidth.setValueChangeCallback(new ChangeSizeCallback(this, this.configHeight, true));
            this.configHeight.setValueChangeCallback(new ChangeSizeCallback(this, this.configWidth, false));
        }
        this.configWidth.toggleUseSlider();
        this.configHeight.toggleUseSlider();
        this.imagetextLogic = new ImagetextLogic();
    }

    @Override
    public void initGui() {
        super.initGui();

        this.createTabs(ImagetextGuiTab.values(), new TabButtonListener(this));

        ButtonGeneric executeButton = Buttons.EXECUTE.get(20, this.height - 40, ScreenConstants.NORMAL_BUTTON_WIDTH);
        // Malilib does not support text hover with Text class
        this.previewButton = Buttons.PREVIEW.get(24 + ScreenConstants.NORMAL_BUTTON_WIDTH, this.height - 40, ScreenConstants.NORMAL_BUTTON_WIDTH);

        this.addButton(executeButton, this::execute);
        this.addButton(this.previewButton, new PreviewButtonListener());
    }

    @Override
    public List<ConfigOptionWrapper> getConfigs() {
        List<IConfigBase> options = new ArrayList<>();

        options.add(this.configImage);
        options.add(this.configWidth);
        options.add(this.configHeight);
        options.add(this.configCharacters);
        options.add(this.configSmoothImage);
        options.add(this.configShowResolution);

        List<ConfigOptionWrapper> optionsWrapper = OptionWrapper.createFor(options);
        this.addTabOptions(optionsWrapper);

        return optionsWrapper;
    }

    @Override
    public boolean isTab(IScreenTab tab) {
        return ImagetextScreen.tab == tab;
    }

    private void addTabOptions(List<ConfigOptionWrapper> list) {
        List<IConfigBase> options = new ArrayList<>();

        switch (tab) {
            case LORE -> options.add(this.configLoreOption);
            case BOOK_PAGE -> options.add(this.configBookOption);
            case BOOK_TOOLTIP -> {
                options.add(this.configBookOption);
                options.add(this.configBookAuthor);
                options.add(this.configBookMessage);
            }
            case HOLOGRAM -> {
                options.add(this.configPosX);
                options.add(this.configPosY);
                options.add(this.configPosZ);
            }
        }

        if (tab != ImagetextGuiTab.JSON) {
            list.add(new OptionWrapper(""));
            list.add(new OptionWrapper(tab.translationKey));
        }

        list.addAll(OptionWrapper.createFor(options));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);

        if (this.previewButton.isMouseOver() && !this.imagetextLogic.isEmpty())
            this.renderTooltip(matrixStack, this.imagetextLogic.getTextList(), mouseX, mouseY);
    }

    private static class TabButtonListener implements ITabListener {
        private final IScreenTab tab;
        private final ImagetextScreen parent;

        private TabButtonListener(IScreenTab tab, ImagetextScreen parent) {
            this.tab = tab;
            this.parent = parent;
        }

        private TabButtonListener(ImagetextScreen gui) {
            this(null, gui);
        }

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            if (this.tab == null)
                return;

            ImagetextScreen.tab = (ImagetextGuiTab) this.tab;

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

    private enum ImagetextGuiTab implements IScreenTab {
        LORE("lore"),
        BOOK_PAGE("bookPage"),
        BOOK_TOOLTIP("bookTooltip"),
        HOLOGRAM("hologram"),
        //SIGN("sign"),
        JSON("json");

        static final String BASE_KEY = "fzmm.gui.imagetext.";

        private final String translationKey;

        ImagetextGuiTab(String translationKey) {
            this.translationKey = BASE_KEY + translationKey;
        }

        public String getDisplayName() {
            return StringUtils.translate(this.translationKey);
        }
    }

    public void execute(ButtonBase button, int mouseButton) {
        if (ImagetextScreen.this.configImage.hasNoImage())
            return;

        MinecraftClient client = MinecraftClient.getInstance();
        assert client != null;
        assert client.player != null;
        ItemStack stack = client.player.getMainHandStack();

        ImagetextScreen.this.updateImagetext();

        LoreOption loreOption = (LoreOption) ImagetextScreen.this.configLoreOption.getOptionListValue();
        BookOption bookOption = (BookOption) ImagetextScreen.this.configBookOption.getOptionListValue();
        String author = ImagetextScreen.this.configBookAuthor.getStringValue();
        String bookMessage = ImagetextScreen.this.configBookMessage.getStringValue();
        int x = ImagetextScreen.this.configPosX.getIntegerValue();
        int y = ImagetextScreen.this.configPosY.getIntegerValue();
        int z = ImagetextScreen.this.configPosZ.getIntegerValue();


        switch (tab) {
            case LORE -> ImagetextScreen.this.imagetextLogic.giveInLore(stack, loreOption == LoreOption.ADD);
            case BOOK_PAGE -> {
                if (bookOption == BookOption.ADD_PAGE)
                    ImagetextScreen.this.imagetextLogic.addBookPage();
                else
                    ImagetextScreen.this.imagetextLogic.giveBookPage();
            }
            case BOOK_TOOLTIP -> {
                try {
                    ImagetextScreen.this.imagetextLogic.giveBookTooltip(author, bookMessage);
                } catch (BookNbtOverflow ignored) {
                    InfoUtils.showGuiOrInGameMessage(Message.MessageType.ERROR, "fzmm.gui.imagetext.bookTooltip.overflow");
                }
            }
            case HOLOGRAM -> ImagetextScreen.this.imagetextLogic.giveAsHologram(x, y, z);
            case JSON -> client.keyboard.setClipboard(ImagetextScreen.this.imagetextLogic.getImagetextString());
        }
    }

    private class PreviewButtonListener implements IButtonActionListener {

        @Override
        public void actionPerformedWithButton(ButtonBase button, int mouseButton) {
            ImagetextScreen.this.updateImagetext();
        }
    }

    public void updateImagetext() {
        if (this.configImage.hasNoImage())
            return;

        BufferedImage image = this.configImage.getImage();
        String characters = this.configCharacters.getStringValue();
        int width = this.configWidth.getIntegerValue();
        int height = this.configHeight.getIntegerValue();
        boolean smoothScaling = this.configSmoothImage.getBooleanValue();
        boolean showResolution = this.configShowResolution.getBooleanValue();

        if (tab == ImagetextGuiTab.BOOK_PAGE) {
            width = this.getMaxImageWidthForBookPage(characters);
            height = 15;
        }

        this.imagetextLogic.generateImagetext(image, characters, width, height, smoothScaling);

        if (showResolution)
            this.imagetextLogic.addResolution();
    }

    private int getMaxImageWidthForBookPage(String characters) {
        int maxTextWidth = 113; //BookScreen.MAX_TEXT_WIDTH = 114;
        int width = 0;
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;

        if (characters.length() == 1)
            width = maxTextWidth / textRenderer.getWidth(characters);
        else {
            String message = "";
            int length = characters.length();
            do {
                message += characters.charAt(width % length);
                width++;
            } while (textRenderer.getWidth(message) < maxTextWidth);
        }

        return width;
    }

    private record ChangeSizeCallback(ImagetextScreen parent, ConfigInteger configToChange,
                                      boolean isWidth) implements IValueChangeCallback<ConfigInteger> {

        @Override
        public void onValueChanged(ConfigInteger config) {
            if (this.parent.configImage.hasNoImage())
                return;

            BufferedImage image = this.parent.configImage.getImage();
            assert image != null;
            int configValue = config.getIntegerValue();
            Vec2f rescaledSize = ImagetextLogic.changeResolutionKeepingAspectRatio(image.getWidth(), image.getHeight(), configValue, this.isWidth);

            int newValue = (int) (this.isWidth ? rescaledSize.y : rescaledSize.x);

            this.configToChange.setValueChangeCallback(null);
            this.configToChange.setIntegerValue(newValue);
            this.configToChange.setValueChangeCallback(new ChangeSizeCallback(this.parent, config, !this.isWidth));
        }
    }
}
